/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.riding.behaviour.types.air

import com.bedrockk.molang.Expression
import com.cobblemon.mod.common.api.riding.RidingStyle
import com.cobblemon.mod.common.api.riding.behaviour.*
import com.cobblemon.mod.common.api.riding.posing.PoseOption
import com.cobblemon.mod.common.api.riding.posing.PoseProvider
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.util.*
import com.cobblemon.mod.common.util.math.geometry.toRadians
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.Mth
import net.minecraft.util.SmoothDouble
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.phys.Vec2
import net.minecraft.world.phys.Vec3
import net.minecraft.world.phys.shapes.Shapes
import org.joml.Matrix3f
import kotlin.math.*

class HoverBehaviour : RidingBehaviour<HoverSettings, HoverState> {
    companion object {
        val KEY = cobblemonResource("air/hover")
    }

    override val key = KEY

    override fun getRidingStyle(settings: HoverSettings, state: HoverState): RidingStyle {
        return RidingStyle.AIR
    }

    val poseProvider = PoseProvider<HoverSettings, HoverState>(PoseType.STAND)
        .with(PoseOption(PoseType.WALK) { _, state, _ ->
            return@PoseOption abs(state.rideVelocity.get().z) > 0.2
        })

    override fun isActive(
        settings: HoverSettings,
        state: HoverState,
        vehicle: PokemonEntity
    ): Boolean {
        return Shapes.create(vehicle.boundingBox).blockPositionsAsListRounded().any {
            //Need to check other fluids
            if (vehicle.isInWater || vehicle.isUnderWater) {
                return@any false
            }
            //This might not actually work, depending on what the yPos actually is. yPos of the middle of the entity? the feet?
            if (it.y.toDouble() == (vehicle.position().y)) {
                val blockState = vehicle.level().getBlockState(it.below())
                return@any !blockState.isAir && blockState.fluidState.isEmpty
            }
            true
        }
    }

    override fun pose(
        settings: HoverSettings,
        state: HoverState,
        vehicle: PokemonEntity
    ): PoseType {
        return this.poseProvider.select(settings, state, vehicle)
    }

    override fun speed(
        settings: HoverSettings,
        state: HoverState,
        vehicle: PokemonEntity,
        driver: Player
    ): Float {
        return state.rideVelocity.get().length().toFloat()
    }

    override fun updatePassengerRotation(
        settings: HoverSettings,
        state: HoverState,
        vehicle: PokemonEntity,
        driver: LivingEntity
    ) {

        //Take the inverse so that it cancels out how
        //much the entity rotates.
        val turnAmount = calcRotAmount(settings, state, vehicle, driver)

        val topSpeed = vehicle.runtime.resolveDouble(settings.speedExpr)
        val maxYawDiff = 90.0f

        //Normalize the current rotation diff
        val rotMod = Mth.wrapDegrees(driver.yRot - vehicle.yRot) / maxYawDiff

        val rotAmount = 10.0f

        //Take the inverse so that you turn more at higher speeds
        val normSpeed = 1.0f - 0.5f*normalizeVal(state.rideVelocity.get().length(), 0.0, topSpeed).toFloat()

        //driver.yRot += (entity.riding.deltaRotation.y - turnAmount)
        //driver.setYHeadRot(driver.yHeadRot + (entity.riding.deltaRotation.y) - turnAmount)
    }

    override fun clampPassengerRotation(
        settings: HoverSettings,
        state: HoverState,
        vehicle: PokemonEntity,
        driver: LivingEntity
    ) {
        val f = Mth.wrapDegrees(driver.yRot - vehicle.yRot)
        val lookYawLimit = 90.0f
        val g = Mth.clamp(f, -lookYawLimit, lookYawLimit)
        driver.yRotO += g - f
        driver.yRot = driver.yRot + g - f
        driver.setYHeadRot(driver.yRot)
    }


    override fun rotation(
        settings: HoverSettings,
        state: HoverState,
        vehicle: PokemonEntity,
        driver: LivingEntity
    ): Vec2 {
        val turnAmount =  calcRotAmount(settings, state, vehicle, driver)

        return Vec2(driver.xRot, vehicle.yRot + turnAmount )

    }

    /*
   *  Calculates the rotation amount given the difference between
   *  the current player y rot and entity y rot. This gives the
   *  affect of influencing a rides rotation through the mouse
   *  without it being instant.
   */
    fun calcRotAmount(
        settings: HoverSettings,
        state: HoverState,
        vehicle: PokemonEntity,
        driver: LivingEntity
    ): Float {
        val topSpeed = vehicle.runtime.resolveDouble(settings.speedExpr)
        //In degrees per second
        val handling = 220.0
        val maxYawDiff = 90.0f

        //Normalize the current rotation diff
        val rotDiff = Mth.wrapDegrees(driver.yRot - vehicle.yRot)
        val rotDiffNorm = rotDiff / maxYawDiff

        //Take the square root so that the ride levels out quicker when at lower differences between entity
        //y and driver y
        //This influences the speed of the turn based on how far in one direction you're looking
        val rotDiffMod = (sqrt(abs(rotDiffNorm)) * rotDiffNorm.sign)

        //Take the inverse so that you turn less at higher speeds
        val normSpeed = 1.0f // = 1.0f - 0.5f*normalizeVal(state.rideVelocityocity.length(), 0.0, topSpeed).toFloat()

        val turnRate = (handling.toFloat() / 20.0f)

        //Ensure you only ever rotate as much difference as there is between the angles.
        val turnSpeed = turnRate  * rotDiffMod * normSpeed
        val rotAmount = turnSpeed.coerceIn(-abs(rotDiff), abs(rotDiff))

        return rotAmount
    }

    override fun velocity(
        settings: HoverSettings,
        state: HoverState,
        vehicle: PokemonEntity,
        driver: Player,
        input: Vec3
    ): Vec3 {

//        if (Minecraft.getInstance().options.keySprint.isDown) {
//            val belowBox = vehicle.boundingBox.expandTowards(0.0,-1.5, 0.0).inflate(5.0, 10.0,5.0)
//
//            val level = vehicle.level()
//            val toBeSucked = level.getEntities(vehicle, belowBox)
//            for (suckee in toBeSucked) {
//                if (suckee != null) {
//                    abductify(suckee, vehicle.position().add(0.0,-2.0,0.0))
//                    suckee.deltaMovement = suckee.deltaMovement.add(0.0, 0.5,0.0)
//
//                }
//            }
//        }

//
//        if (firstEntity != null) {
//            // Example action: print the entity name
//            println("Found nearby entity: ${firstEntity.name.string}")
//
//            firstEntity.deltaMovement = firstEntity.deltaMovement.add(0.0, 0.1,0.0)
//        }
        val retVel = calculateRideSpaceVel(settings, state, vehicle, driver)
        //state.rideVelocity.set(retVel)
        return retVel
    }

    private fun abductify(abductee: Entity, pos: Vec3) {
        val diff = pos.subtract(abductee.position()).scale(1.0)
        abductee.deltaMovement = abductee.deltaMovement.add(diff)
    }

    private fun calculateRideSpaceVel(
        settings: HoverSettings,
        state: HoverState,
        vehicle: PokemonEntity,
        driver: Player
    ): Vec3 {
        val topSpeed = vehicle.runtime.resolveDouble(settings.speedExpr)
        val accel = vehicle.runtime.resolveDouble(settings.accelerationExpr)
        val speed = vehicle.deltaMovement.length()

        //Flag for determining if player is actively inputting
        var activeInput = false

        val minSpeed = 0.0

        var newVelocity = vehicle.deltaMovement

        val yawAligned = Matrix3f().rotateY(vehicle.yRot.toRadians())
        newVelocity = (newVelocity.toVector3f().mul(yawAligned)).toVec3d()

        //air Resistance
        //newVelocity = newVelocity.lerp(Vec3.ZERO,(((topSpeed - speed) / topSpeed))*0.001)


        //speed up and slow down based on input
        if (driver.zza != 0.0f && state.stamina.get() > 0.0) {
            //make sure it can't exceed top speed
            val forwardInput = when {
                speed > topSpeed && (driver.zza.sign == newVelocity.z.sign.toFloat()) -> 0.0
                else -> driver.zza.sign
            }

            newVelocity = Vec3(
                newVelocity.x,
                newVelocity.y,
                (newVelocity.z + (accel * forwardInput.toDouble())))

        } else {
//            newVelocity = Vec3(
//                newVelocity.x,
//                newVelocity.y,
//                newVelocity.z - (((topSpeed - speed) / topSpeed)*0.001)
//            )
        }

        //speed up and slow down based on input
        if (driver.xxa != 0.0f && state.stamina.get() > 0.0) {
            //make sure it can't exceed top speed
            val lateralInput = when {
                speed > topSpeed && (driver.xxa.sign == newVelocity.x.sign.toFloat()) -> 0.0
                else -> driver.xxa.sign
            }

            newVelocity = Vec3(
                newVelocity.x + (accel * lateralInput.toDouble()),
                newVelocity.y,
                (newVelocity.z ))

            //air resistance
//            if (lateralInput == 0.0) {
//                newVelocity = Vec3(
//                    newVelocity.x  - (((topSpeed - speed) / topSpeed)*0.001),
//                    newVelocity.y,
//                    newVelocity.z
//                )
//            }
        }

        //Vertical movement based on driver input.
        val vertInput = when {
            driver.jumping && !(speed > topSpeed && (newVelocity.y.sign.toFloat() > 0.0))-> 1.0
            driver.isShiftKeyDown  && !(speed > topSpeed && (newVelocity.y.sign.toFloat() < 0.0))-> -1.0
            else -> 0.0
        }

        //air resistance
//        if (vertInput == 0.0) {
//            newVelocity = Vec3(
//                newVelocity.x,
//                newVelocity.y  - (((topSpeed - speed) / topSpeed)*0.001),
//                newVelocity.z
//            )
//        }

        if (state.stamina.get() > 0.0) {
            newVelocity = Vec3(
                newVelocity.x,
                (newVelocity.y + (accel * vertInput)), //.coerceIn(-vertTopSpeed, vertTopSpeed),
                newVelocity.z)
        }

        return newVelocity
    }

    /*
    *  Normalizes the current speed between minSpeed and maxSpeed.
    *  The result is clamped between 0.0 and 1.0, where 0.0 represents minSpeed and 1.0 represents maxSpeed.
    */
    private fun normalizeVal(currSpeed: Double, minSpeed: Double, maxSpeed: Double): Double {
        require(maxSpeed > minSpeed) { "maxSpeed must be greater than minSpeed" }
        return ((currSpeed - minSpeed) / (maxSpeed - minSpeed)).coerceIn(0.0, 1.0)
    }

    override fun angRollVel(
        settings: HoverSettings,
        state: HoverState,
        vehicle: PokemonEntity,
        driver: Player,
        deltaTime: Double
    ): Vec3 {
        return Vec3.ZERO
    }

    override fun rotationOnMouseXY(
        settings: HoverSettings,
        state: HoverState,
        vehicle: PokemonEntity,
        driver: Player,
        mouseY: Double,
        mouseX: Double,
        mouseYSmoother: SmoothDouble,
        mouseXSmoother: SmoothDouble,
        sensitivity: Double,
        deltaTime: Double
    ): Vec3 {
        //Smooth out mouse input.
        val smoothingSpeed = 4
        val xInput = mouseXSmoother.getNewDeltaValue(mouseX * 0.1, deltaTime * smoothingSpeed);
        val yInput = mouseYSmoother.getNewDeltaValue(mouseY * 0.1, deltaTime * smoothingSpeed);

        //yaw, pitch, roll
        return Vec3(0.0, yInput, xInput)
    }

    override fun canJump(
        settings: HoverSettings,
        state: HoverState,
        vehicle: PokemonEntity,
        driver: Player
    ): Boolean {
        return false
    }

    override fun setRideBar(
        settings: HoverSettings,
        state: HoverState,
        vehicle: PokemonEntity,
        driver: Player
    ): Float {
        //Retrieve stamina from state and use it to set the "stamina bar"
        return (state.stamina.get() / 1.0f)
    }

    override fun jumpForce(
        settings: HoverSettings,
        state: HoverState,
        vehicle: PokemonEntity,
        driver: Player,
        jumpStrength: Int
    ): Vec3 {
        return Vec3.ZERO
    }

    override fun gravity(
        settings: HoverSettings,
        state: HoverState,
        vehicle: PokemonEntity,
        regularGravity: Double
    ): Double {
        return 0.0
    }

    override fun rideFovMultiplier(
        settings: HoverSettings,
        state: HoverState,
        vehicle: PokemonEntity,
        driver: Player
    ): Float {
        return 1.0f
    }

    override fun useAngVelSmoothing(
        settings: HoverSettings,
        state: HoverState,
        vehicle: PokemonEntity
    ): Boolean {
        return false
    }

    override fun useRidingAltPose(
        settings: HoverSettings,
        state: HoverState,
        vehicle: PokemonEntity,
        driver: Player
    ): ResourceLocation {
        return cobblemonResource("no_pose")
    }

    override fun inertia(
        settings: HoverSettings,
        state: HoverState,
        vehicle: PokemonEntity
    ): Double {
        return 1.0
    }

    override fun shouldRoll(
        settings: HoverSettings,
        state: HoverState,
        vehicle: PokemonEntity
    ): Boolean {
        return false
    }

    override fun turnOffOnGround(
        settings: HoverSettings,
        state: HoverState,
        vehicle: PokemonEntity
    ): Boolean {
        return false
    }

    override fun dismountOnShift(
        settings: HoverSettings,
        state: HoverState,
        vehicle: PokemonEntity
    ): Boolean {
        return false
    }

    override fun shouldRotatePokemonHead(
        settings: HoverSettings,
        state: HoverState,
        vehicle: PokemonEntity
    ): Boolean {
        return false
    }

    override fun shouldRotatePlayerHead(
        settings: HoverSettings,
        state: HoverState,
        vehicle: PokemonEntity
    ): Boolean {
        return false
    }

    override fun createDefaultState(settings: HoverSettings) = HoverState()
}

class HoverSettings : RidingBehaviourSettings {
    override val key = HoverBehaviour.KEY

    var canJump = "true".asExpression()
        private set

    var speedExpr: Expression = "q.get_ride_stats('SPEED', 'AIR', 0.65, 0.3)".asExpression()
        private set

    // Max accel is a whole 1.0 in 1 second. The conversion in the function below is to convert seconds to ticks
    var accelerationExpr: Expression =
        "q.get_ride_stats('ACCELERATION', 'AIR', (1.0 / (20.0 * 1.5)), (1.0 / (20.0 * 3.0)))".asExpression()
        private set

    // Between 30 seconds and 10 seconds at the lowest when at full speed.
    var staminaExpr: Expression = "q.get_ride_stats('STAMINA', 'AIR', 30.0, 10.0)".asExpression()
        private set

    //Between a one block jump and a ten block jump
    var jumpExpr: Expression = "q.get_ride_stats('JUMP', 'AIR', 10.0, 1.0)".asExpression()
        private set

    var handlingExpr: Expression = "q.get_ride_stats('SKILL', 'AIR', 140.0, 20.0)".asExpression()
        private set

    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeResourceLocation(key)
        buffer.writeExpression(speedExpr)
        buffer.writeExpression(accelerationExpr)
        buffer.writeExpression(staminaExpr)
        buffer.writeExpression(jumpExpr)
        buffer.writeExpression(handlingExpr)
    }

    override fun decode(buffer: RegistryFriendlyByteBuf) {
        speedExpr = buffer.readExpression()
        accelerationExpr = buffer.readExpression()
        staminaExpr = buffer.readExpression()
        jumpExpr = buffer.readExpression()
        handlingExpr = buffer.readExpression()
    }

}

class HoverState : RidingBehaviourState() {

    override fun copy() = HoverState().also {
        it.rideVelocity.set(this.rideVelocity.get(), forced = true)
        it.stamina.set(this.stamina.get(), forced = true)
    }

    override fun shouldSync(previous: RidingBehaviourState): Boolean {
        if (previous !is HoverState) return false
        return super.shouldSync(previous)
    }
}
