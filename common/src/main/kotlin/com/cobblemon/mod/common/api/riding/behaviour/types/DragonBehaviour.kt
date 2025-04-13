/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.riding.behaviour.types

import com.bedrockk.molang.Expression
import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.OrientationControllable
import com.cobblemon.mod.common.api.riding.RidingStyle
import com.cobblemon.mod.common.api.riding.behaviour.*
import com.cobblemon.mod.common.api.riding.posing.PoseOption
import com.cobblemon.mod.common.api.riding.posing.PoseProvider
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.util.*
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.SmoothDouble
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.phys.Vec2
import net.minecraft.world.phys.Vec3
import net.minecraft.world.phys.shapes.Shapes
import kotlin.math.*

class DragonBehaviour : RidingBehaviour<DragonSettings, DragonState> {
    companion object {
        val KEY = cobblemonResource("air/dragon")
    }

    override val key = KEY
    override val style = RidingStyle.AIR

    val poseProvider = PoseProvider<DragonSettings, DragonState>(PoseType.HOVER)
        .with(PoseOption(PoseType.FLY) { _, state, _ -> state.rideVelocity.get().z > 0.1 })

    override fun isActive(settings: DragonSettings, state: DragonState, vehicle: PokemonEntity): Boolean {
        return Shapes.create(vehicle.boundingBox).blockPositionsAsListRounded().any {
            //Need to check other fluids
            if (vehicle.isInWater || vehicle.isUnderWater) {
                return@any false
            }
            //This might not actually work, depending on what the yPos actually is. yPos of the middle of the entity? the feet?
            if (it.y.toDouble() == (vehicle.position().y)) {
                val blockState = vehicle.level().getBlockState(it.below())
                return@any !(!blockState.isAir && blockState.fluidState.isEmpty)
            }
            true
        }
    }

    override fun pose(settings: DragonSettings, state: DragonState, vehicle: PokemonEntity): PoseType {
        return poseProvider.select(settings, state, vehicle)
    }

    override fun speed(settings: DragonSettings, state: DragonState, vehicle: PokemonEntity, driver: Player): Float {
        //retrieve stats
        val topSpeed = vehicle.runtime.resolveDouble(settings.speedExpr)
        val staminaStat = vehicle.runtime.resolveDouble(settings.staminaExpr)

        //retrieve minSpeed
        val minSpeed = vehicle.runtime.resolveDouble(settings.minSpeed)

        //TODO: Reintroduce stamina drain once stats start to be polished and tweaked
        state.stamina.set(1.0f)
//        //Reduce stamina unless stamina is infinite
//        if (!vehicle.runtime.resolveBoolean(settings.infiniteStamina)) {
//            //Calculate stamina loss due to speed
//            //At max speed it will tick down 0.1 a second so the stamina will last ten seconds
//            //There has got to be a better way to express this equation. It interpolates between 0.5 and 1.0
//            var staminaRate = (normalizeSpeed(state.rideVelocity.get().length(), minSpeed, topSpeed))
//
//            //interpolate between 0.25 and 1.0 so that you always have at least a min of 0.25 stam loss
//            staminaRate = 0.25 + (0.75 * staminaRate.pow(3))
//
//            //Calculate stamina loss in seconds achievable at top speed
//            val staminaLoss = staminaRate * (1.0 / (20.0 * staminaStat))
//            state.stamina.set(max(state.stamina.get() - staminaLoss, 0.0).toFloat())
//        } else {
//            state.stamina.set(1.0f)
//        }

        return state.rideVelocity.get().length().toFloat()
    }

    //TODO: Move these functions to a riding util class.
    /*
    *  Normalizes the current speed between minSpeed and maxSpeed.
    *  The result is clamped between 0.0 and 1.0, where 0.0 represents minSpeed and 1.0 represents maxSpeed.
    */
    private fun normalizeSpeed(currSpeed: Double, minSpeed: Double, maxSpeed: Double): Double {
        require(maxSpeed > minSpeed) { "maxSpeed must be greater than minSpeed" }
        return ((currSpeed - minSpeed) / (maxSpeed - minSpeed)).coerceIn(0.0, 1.0)
    }

    override fun rotation(
        settings: DragonSettings,
        state: DragonState,
        vehicle: PokemonEntity,
        driver: LivingEntity
    ): Vec2 {
        return Vec2(driver.xRot * 0.5f, driver.yRot)
    }

    override fun velocity(
        settings: DragonSettings,
        state: DragonState,
        vehicle: PokemonEntity,
        driver: Player,
        input: Vec3
    ): Vec3 {
        var upForce = 0.0
        var forwardForce = 0.0

        val controller = (driver as? OrientationControllable)?.orientationController

        //Calculate ride space velocity
        calculateRideSpaceVel(settings, state, vehicle, driver)

        //Translate ride space velocity to world space velocity.
        if (controller != null) {
            upForce = -1.0 * sin(Math.toRadians(controller.pitch.toDouble())) * state.rideVelocity.get().z
            forwardForce = cos(Math.toRadians(controller.pitch.toDouble())) * state.rideVelocity.get().z
        }

        //If stamina has run out then initiate forced glide down.
        upForce = if (state.stamina.get() > 0.0) upForce else -0.7

        val altitudeLimit = vehicle.runtime.resolveDouble(settings.jumpExpr)

        //Only limit altitude if altitude is not infinite
        if (!vehicle.runtime.resolveBoolean(settings.infiniteAltitude)) {
            //Provide a hard limit on altitude
            upForce = if (vehicle.y >= altitudeLimit && upForce > 0) 0.0 else upForce
        }

        val velocity = Vec3(0.0, upForce, forwardForce)

        return velocity
    }

    /*
    *  Calculates the change in the ride space vector due to player input and ride state
    */
    private fun calculateRideSpaceVel(
        settings: DragonSettings,
        state: DragonState,
        vehicle: PokemonEntity,
        driver: Player
    ) {
        val topSpeed = vehicle.runtime.resolveDouble(settings.speedExpr)
        val accel = vehicle.runtime.resolveDouble(settings.accelerationExpr)
        val altitudeLimit = vehicle.runtime.resolveDouble(settings.jumpExpr)
        val minSpeed = vehicle.runtime.resolveDouble(settings.minSpeed)
        val speed = state.rideVelocity.get().length()

        //Give no altitude limit if at max jump stat.
        val pushingHeightLimit = if (vehicle.runtime.resolveBoolean(settings.infiniteStamina)) false
        else (vehicle.y >= altitudeLimit && vehicle.xRot <= 0)


        //speed up and slow down based on input
        if (driver.zza > 0.0 && speed < topSpeed && state.stamina.get() > 0.0f && !pushingHeightLimit) {
            //modify acceleration to be slower when at closer speeds to top speed
            val accelMod = max(-(normalizeSpeed(speed, minSpeed, topSpeed)) + 1, 0.0)
            state.rideVelocity.set(Vec3(state.rideVelocity.get().x, state.rideVelocity.get().y, min(state.rideVelocity.get().z + (accel * accelMod), topSpeed)))
        } else if (driver.zza >= 0.0 && (state.stamina.get() == 0.0f || pushingHeightLimit)) {
            state.rideVelocity.set(Vec3(state.rideVelocity.get().x, state.rideVelocity.get().y, max(state.rideVelocity.get().z - ((accel) / 4), minSpeed)))
        } else if (driver.zza < 0.0 && speed > minSpeed) {
            //modify deccel to be slower when at closer speeds to minimum speed
            val deccelMod = max((normalizeSpeed(speed, minSpeed, topSpeed) - 1).pow(2) * 8, 0.1)

            //Decelerate currently always a constant half of max acceleration.
            state.rideVelocity.set(Vec3(state.rideVelocity.get().x, state.rideVelocity.get().y, max(state.rideVelocity.get().z - ((accel * deccelMod) / 2), minSpeed)))
        }
    }

    override fun angRollVel(
        settings: DragonSettings,
        state: DragonState,
        vehicle: PokemonEntity,
        driver: Player,
        deltaTime: Double
    ): Vec3 {
        return Vec3(0.0, 0.0, 0.0)
    }

    override fun rotationOnMouseXY(
        settings: DragonSettings,
        state: DragonState,
        vehicle: PokemonEntity,
        driver: Player,
        mouseY: Double,
        mouseX: Double,
        mouseYSmoother: SmoothDouble,
        mouseXSmoother: SmoothDouble,
        sensitivity: Double,
        deltaTime: Double
    ): Vec3 {
        if (driver !is OrientationControllable) return Vec3.ZERO
        val controller = (driver as OrientationControllable).orientationController

        val handling = vehicle.runtime.resolveDouble(settings.handlingExpr)
        val topSpeed = vehicle.runtime.resolveDouble(settings.speedExpr)

        //Smooth out mouse input.
        val smoothingSpeed = 4.0
        val invertRoll = if (Cobblemon.config.invertRoll) -1 else 1
        val invertPitch = if (Cobblemon.config.invertPitch) -1 else 1
        //TODO: tie this to handling.
        val mouseXc = (mouseX).coerceIn(-60.0, 60.0)
        val mouseYc = (mouseY).coerceIn(-60.0, 60.0)
        val xInput = mouseXSmoother.getNewDeltaValue(mouseXc * 0.1 * invertRoll, deltaTime * smoothingSpeed);
        val yInput = mouseYSmoother.getNewDeltaValue(mouseYc * 0.1 * invertPitch, deltaTime * smoothingSpeed);

        //TODO: Figure out where and how I actually have to be using this.
        //convert it to delta time
        //handling *= (cappedDeltaTime)

        val pitchDelta = yInput
        val yawDelta = xInput

        //Apply yaw globally as we don't want roll or pitch changes due to local yaw when looking up or down.
        controller.applyGlobalYaw(yawDelta.toFloat())

        return Vec3(0.0, pitchDelta, 0.0)
    }

    override fun canJump(
        settings: DragonSettings,
        state: DragonState,
        vehicle: PokemonEntity,
        driver: Player
    ): Boolean {
        return false
    }

    override fun setRideBar(
        settings: DragonSettings,
        state: DragonState,
        vehicle: PokemonEntity,
        driver: Player
    ): Float {
        return (state.stamina.get() / 1.0f)
    }

    override fun jumpForce(
        settings: DragonSettings,
        state: DragonState,
        vehicle: PokemonEntity,
        driver: Player,
        jumpStrength: Int
    ): Vec3 {
        return Vec3.ZERO
    }

    override fun gravity(
        settings: DragonSettings,
        state: DragonState,
        vehicle: PokemonEntity,
        regularGravity: Double
    ): Double {
        return 0.0
    }

    override fun rideFovMultiplier(
        settings: DragonSettings,
        state: DragonState,
        vehicle: PokemonEntity,
        driver: Player
    ): Float {
        val topSpeed = vehicle.runtime.resolveDouble(settings.speedExpr)
        val minSpeed = vehicle.runtime.resolveDouble(settings.minSpeed)

        //Must I ensure that topspeed is greater than minimum?
        val normalizedSpeed = normalizeSpeed(state.rideVelocity.get().length(), minSpeed, topSpeed)

        //TODO: Determine if this should be based on max possible speed instead of top speed.
        //Only ever want the fov change to be a max of 0.2 and for it to have non linear scaling.
        return 1.0f + normalizedSpeed.pow(2).toFloat() * 0.2f
    }

    override fun useAngVelSmoothing(settings: DragonSettings, state: DragonState, vehicle: PokemonEntity): Boolean {
        return true
    }

    override fun useRidingAltPose(
        settings: DragonSettings,
        state: DragonState,
        vehicle: PokemonEntity,
        driver: Player
    ): Boolean {
        return false
    }

    override fun inertia(settings: DragonSettings, state: DragonState, vehicle: PokemonEntity): Double {
        return 0.5
    }

    override fun shouldRoll(settings: DragonSettings, state: DragonState, vehicle: PokemonEntity): Boolean {
        return true
    }

    override fun turnOffOnGround(settings: DragonSettings, state: DragonState, vehicle: PokemonEntity): Boolean {
        return false
    }

    override fun dismountOnShift(settings: DragonSettings, state: DragonState, vehicle: PokemonEntity): Boolean {
        return false
    }

    override fun shouldRotatePokemonHead(
        settings: DragonSettings,
        state: DragonState,
        vehicle: PokemonEntity
    ): Boolean {
        return false
    }

    override fun shouldRotatePlayerHead(settings: DragonSettings, state: DragonState, vehicle: PokemonEntity): Boolean {
        return false
    }

    override fun createDefaultState(settings: DragonSettings) = DragonState()
}

class DragonSettings : RidingBehaviourSettings {
    override val key = DragonBehaviour.KEY

    var gravity: Expression = "0".asExpression()
        private set

    //TODO: get rid of this in as the behaviour should not have a minimum speed other than zero.
    var minSpeed: Expression = "0.0".asExpression()
        private set

    var handlingYawExpr: Expression = "q.get_ride_stats('SKILL', 'AIR', 50.0, 25.0)".asExpression()
        private set

    // Make configurable by json
    var infiniteStamina: Expression = "false".asExpression()
        private set

    var infiniteAltitude: Expression = "false".asExpression()
        private set

    var jumpExpr: Expression = "q.get_ride_stats('JUMP', 'AIR', 300.0, 128.0)".asExpression()
        private set
    var handlingExpr: Expression =  "q.get_ride_stats('SKILL', 'AIR', 140.0, 20.0)".asExpression()
        private set
    var speedExpr: Expression =  "q.get_ride_stats('SPEED', 'AIR', 1.8, 1.0)".asExpression()
        private set
    var accelerationExpr: Expression = "q.get_ride_stats('ACCELERATION', 'AIR', (1.0 / (20.0 * 1.0)), (1.0 / (20.0 * 5.0)))".asExpression()
        private set
    var staminaExpr: Expression = "q.get_ride_stats('STAMINA', 'AIR', 60.0, 10.0)".asExpression()
        private set

    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeResourceLocation(key)
        buffer.writeExpression(gravity)
        buffer.writeExpression(minSpeed)
        buffer.writeExpression(handlingYawExpr)
        buffer.writeExpression(infiniteStamina)
        buffer.writeExpression(infiniteAltitude)
        buffer.writeExpression(jumpExpr)
        buffer.writeExpression(handlingExpr)
        buffer.writeExpression(speedExpr)
        buffer.writeExpression(accelerationExpr)
        buffer.writeExpression(staminaExpr)
    }

    override fun decode(buffer: RegistryFriendlyByteBuf) {
        gravity = buffer.readExpression()
        minSpeed = buffer.readExpression()
        handlingYawExpr = buffer.readExpression()
        infiniteStamina = buffer.readExpression()
        infiniteAltitude = buffer.readExpression()
        jumpExpr = buffer.readExpression()
        handlingExpr = buffer.readExpression()
        speedExpr = buffer.readExpression()
        accelerationExpr = buffer.readExpression()
        staminaExpr = buffer.readExpression()
    }
}

class DragonState : RidingBehaviourState() {

    override fun encode(buffer: FriendlyByteBuf) {
        super.encode(buffer)
        buffer.writeFloat(stamina.get())
    }

    override fun decode(buffer: FriendlyByteBuf) {
        super.decode(buffer)
    }

    override fun reset() {
        super.reset()
    }

    override fun copy() = DragonState().also {
        it.stamina.set(stamina.get(), forced = true)
        it.rideVelocity.set(rideVelocity.get(), forced = true)
    }

    override fun shouldSync(previous: RidingBehaviourState): Boolean {
        if (previous !is DragonState) return false
        return super.shouldSync(previous)
    }
}
