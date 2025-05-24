/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.riding.util

import com.cobblemon.mod.common.OrientationControllable
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.util.math.geometry.toRadians
import net.minecraft.util.Mth
import net.minecraft.world.phys.Vec3
import org.joml.Matrix3f
import org.joml.Quaternionf
import org.joml.Vector3f

/**
 * Class to handle the updating and containment of ride data used in animation.
 *
 * @author Jackowes
 * @since April 7th, 2025
 */


class RidingAnimationData
{
    var prevOrientation: Matrix3f = Matrix3f()
    var prevRot: Vec3 = Vec3.ZERO
    var velocitySpring: Vec3Spring = Vec3Spring()
    var rotSpring: Vec3Spring = Vec3Spring()
    var rotDeltaSpring: Vec3Spring = Vec3Spring()
    var localVelocitySpring: Vec3Spring = Vec3Spring()

    fun update(entity: PokemonEntity) {

        var currRot = Vec3.ZERO
        var angDelta = Vec3.ZERO

        val stiffness = 90.0
        val damping = 18.0
//        val stiffness = 16.0
//        val damping = 8.0

        // Update velocity first
        val currentVelocity = Vec3(
            entity.x - entity.xOld,
            entity.y - entity.yOld,
            entity.z - entity.zOld
        )

        val r = entity.ifRidingAvailableSupply(false) { behaviour, settings, state ->
            behaviour.shouldRoll(settings, state, entity);
        }
        if( r ) {
            //TODO: should this return?
            val controller = (entity.firstPassenger as? OrientationControllable)?.orientationController ?: return
            val orientation = controller.orientation ?: Matrix3f()

            val currPitch = controller.pitch
            val currYaw = controller.yaw
            val currRoll = controller.roll
            currRot = Vec3(currPitch.toDouble(), currYaw.toDouble(), currRoll.toDouble())

            val currOrientation = orientation

            /******************************************************
             * Calculate current 3d euler rotation deltas in the
             * entities local space
             *****************************************************/

            // Get current and previous orientation as quaternions
            val currQuat = Quaternionf().setFromUnnormalized(currOrientation)
            val prevQuat = Quaternionf().setFromUnnormalized(prevOrientation)

            // Compute deltaQuat = current * inverse(previous)
            val deltaQuat = Quaternionf(currQuat).mul(Quaternionf(prevQuat).invert())

            // Transform delta into the local space of the current orientation
            val localDeltaQuat = Quaternionf(currQuat).invert().mul(deltaQuat).mul(currQuat)

            // Convert delta rotation to Euler angles
            val euler = Vector3f()
            localDeltaQuat.getEulerAnglesXYZ(euler) // Angles are in radians

            // Convert to degrees
            val pitchDegrees = Math.toDegrees(euler.x.toDouble()) // Rotation around X axis = pitch
            val yawDegrees = Math.toDegrees(euler.y.toDouble())   // Rotation around Y axis = yaw
            val rollDegrees = Math.toDegrees(euler.z.toDouble())  // Rotation around Z axis = roll
            //Scale rotation delta by 20 to represent degree per second instead of tick
            angDelta = Vec3(pitchDegrees, yawDegrees, rollDegrees).scale(20.0)

            /******************************************************
             * Calculate ride local velocity
             *****************************************************/
            val velocityWorldF = Vector3f(currentVelocity.x.toFloat(), currentVelocity.y.toFloat(), currentVelocity.z.toFloat())

            val localVelocityF = Matrix3f(orientation).transpose().transform(velocityWorldF)
            val localVelocity = Vec3(localVelocityF.x.toDouble(), localVelocityF.y.toDouble(), localVelocityF.z.toDouble())

            localVelocitySpring.update(localVelocity, stiffness, damping)

            // Update previous values
            prevOrientation.set(currOrientation)
        } else {
            val currPitch = entity.xRot
            val currYaw = entity.yRot
            val currRoll = 0.0f
            currRot = Vec3(currPitch.toDouble(), currYaw.toDouble(), currRoll.toDouble())

            //Scale rotation delta by 20 to represent degree per second instead of tick
            angDelta = Vec3(
                (currRot.x - prevRot.x),
                Mth.degreesDifference(currRot.y.toFloat(), prevRot.y.toFloat()).toDouble(),
                Mth.degreesDifference(currRot.z.toFloat(), prevRot.z.toFloat()).toDouble()
            ).scale(20.0)

            /******************************************************
             * Calculate ride local velocity
             *****************************************************/
            val yaw360 = -1.0 * (entity.yRot + 360.0f) % 360.0f
            val orientationNonRollable = Matrix3f().rotateY(yaw360.toRadians())

            //Convert to Vector3f
            var velocityWorldF = Vector3f(currentVelocity.x.toFloat(), currentVelocity.y.toFloat(), currentVelocity.z.toFloat())

            val localVelocityF = Matrix3f(orientationNonRollable).transpose().transform(velocityWorldF)
            val localVelocity = Vec3(localVelocityF.x.toDouble(), localVelocityF.y.toDouble(), localVelocityF.z.toDouble())

            localVelocitySpring.update(localVelocity, stiffness, damping)
        }

        /******************************************************
         * Update springs
         *****************************************************/
        rotDeltaSpring.update(angDelta, stiffness, damping)
        velocitySpring.update(currentVelocity, stiffness, damping)
        rotSpring.update(currRot, stiffness, damping)
        prevRot = currRot
    }
}