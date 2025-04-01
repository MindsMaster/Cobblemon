package com.cobblemon.mod.common.api.riding.behaviour.types

import com.bedrockk.molang.Expression
import com.bedrockk.molang.runtime.MoLangMath.lerp
import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.OrientationControllable
import com.cobblemon.mod.common.api.riding.behaviour.RidingBehaviour
import com.cobblemon.mod.common.api.riding.behaviour.RidingBehaviourSettings
import com.cobblemon.mod.common.api.riding.behaviour.RidingBehaviourState
import com.cobblemon.mod.common.api.riding.controller.posing.PoseOption
import com.cobblemon.mod.common.api.riding.controller.posing.PoseProvider
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.util.asExpression
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.resolveBoolean
import com.cobblemon.mod.common.util.resolveDouble
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.util.SmoothDouble
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.phys.Vec2
import net.minecraft.world.phys.Vec3
import kotlin.math.*

class BirdAirBehaviour : RidingBehaviour<BirdAirSettings, BirdAirState> {
    companion object {
        val KEY = cobblemonResource("air/bird")
    }

    val poseProvider = PoseProvider(PoseType.HOVER)
        .with(PoseOption(PoseType.FLY) { it.entityData.get(PokemonEntity.MOVING) })

    override fun isActive(settings: BirdAirSettings, state: BirdAirState, vehicle: PokemonEntity): Boolean {
        return true
    }

    override fun pose(settings: BirdAirSettings, state: BirdAirState, vehicle: PokemonEntity): PoseType {
        return poseProvider.select(vehicle)
    }

    override fun speed(
        settings: BirdAirSettings,
        state: BirdAirState,
        vehicle: PokemonEntity,
        driver: Player
    ): Float {
        return state.rideVel.length().toFloat()
    }

    override fun rotation(
        settings: BirdAirSettings,
        state: BirdAirState,
        vehicle: PokemonEntity,
        driver: LivingEntity
    ): Vec2 {
        return Vec2(driver.xRot * 0.5f, driver.yRot)
    }

    override fun velocity(
        settings: BirdAirSettings,
        state: BirdAirState,
        vehicle: PokemonEntity,
        driver: Player,
        input: Vec3
    ): Vec3 {
        var yVel = 0.0
        var leftForce = 0.0
        var upForce = 0.0
        var forwardForce = 0.0

        //Perform ride velocity update
        calculateRideSpaceVel(settings, state, vehicle, driver)

        //Translate ride space velocity to world space velocity.
        val controller = (driver as? OrientationControllable)?.orientationController
        if (controller != null) {
            //Need to deadzone this when straight up or down

            upForce += -1.0 * sin(Math.toRadians(controller.pitch.toDouble())) * state.rideVel.z
            forwardForce += cos(Math.toRadians(controller.pitch.toDouble())) * state.rideVel.z

            upForce += cos(Math.toRadians(controller.pitch.toDouble())) * state.rideVel.y
            forwardForce += sin(Math.toRadians(controller.pitch.toDouble())) * state.rideVel.y
        }


        //Bring the ride out of the sky when stamina is depleted.
        if (state.stamina <= 0.0) {
            upForce -= 0.3
        }

        val altitudeLimit = vehicle.runtime.resolveDouble(settings.altitudeExpr)

        //Only limit altitude if altitude is not infinite
        if (!vehicle.runtime.resolveBoolean(settings.infiniteAltitude)) {
            //Provide a hard limit on altitude
            upForce = if (vehicle.y >= altitudeLimit && upForce > 0) 0.0 else upForce
        }

        val velocity = Vec3(state.rideVel.x , upForce, forwardForce)
        return velocity
    }

    /*
    *  Calculates the change in the ride space vector due to player input and ride state
    */
    fun calculateRideSpaceVel(settings: BirdAirSettings, state: BirdAirState, vehicle: PokemonEntity, driver: Player) {
        //retrieve stats
        val topSpeed = vehicle.runtime.resolveDouble(settings.topSpeedExpr)
        val glideTopSpeed = vehicle.runtime.resolveDouble(settings.glideTopSpeedExpr)
        val accel = vehicle.runtime.resolveDouble(settings.accelExpr)
        val staminaStat = vehicle.runtime.resolveDouble(settings.staminaExpr)

        var glideSpeedChange = 0.0

        val currSpeed = state.rideVel.length()

        //Flag for determining if player is actively inputting
        var activeInput = false

        //speed up and slow down based on input
        if (driver.zza != 0.0f && state.stamina > 0.0) {
            //make sure it can't exceed top speed
            val forwardInput = when {
                driver.zza > 0 && state.rideVel.z > topSpeed -> 0.0
                driver.zza < 0 && state.rideVel.z < (-topSpeed / 3.0) -> 0.0
                else -> driver.zza.sign
            }

            state.rideVel = Vec3(
                state.rideVel.x,
                state.rideVel.y,
                (state.rideVel.z + (accel * forwardInput.toDouble())))

            activeInput = true
        }

        val controller = (driver as? OrientationControllable)?.orientationController
        if (controller != null) {
            //Base glide speed change on current pitch of the ride.
            glideSpeedChange = sin(Math.toRadians(controller.pitch.toDouble()))
            glideSpeedChange = glideSpeedChange.pow(3) * 0.5

            //TODO: Possibly create a deadzone around parallel where glide doesn't affect speed?
            if (glideSpeedChange <= 0.0) {
                //Ensures that a propelling force is still able to be applied when
                //climbing in height
                if (driver.zza <= 0) {
                    //speed decrease should be 2x speed increase?
                    //state.currSpeed = max(state.currSpeed + (0.0166) * glideSpeedChange, 0.0 )
                    state.rideVel = Vec3(
                        state.rideVel.x,
                        state.rideVel.y,
                        lerp( state.rideVel.z, 0.0,glideSpeedChange * -0.0166 * 2 )
                    )
                }
            } else {
                // only add to the speed if it hasn't exceeded the current
                //glide angles maximum amount of speed that it can give.
                //state.currSpeed = min(state.currSpeed + ((0.0166 * 2) * glideSpeedChange), maxGlideSpeed)
                state.rideVel = Vec3(
                    state.rideVel.x,
                    state.rideVel.y,
                    min(state.rideVel.z + ((0.0166 * 2) * glideSpeedChange), glideTopSpeed)
                )
            }
        }

        //Lateral movement based on driver input.
        val latTopSpeed = topSpeed / 2.0
        if (driver.xxa != 0.0f && state.stamina > 0.0) {
            state.rideVel = Vec3(
                (state.rideVel.x + (accel * driver.xxa)).coerceIn(-latTopSpeed, latTopSpeed),
                state.rideVel.y,
                state.rideVel.z)
            activeInput = true
        }
        else {
            state.rideVel = Vec3(
                lerp(state.rideVel.x, 0.0, latTopSpeed / 20.0),
                state.rideVel.y,
                state.rideVel.z)
        }

        //Vertical movement based on driver input.
        val vertTopSpeed = topSpeed / 2.0
        val vertInput = when {
            driver.jumping -> 1.0
            driver.isShiftKeyDown -> -1.0
            else -> 0.0
        }

        if (vertInput != 0.0 && state.stamina > 0.0) {
            state.rideVel = Vec3(
                state.rideVel.x,
                (state.rideVel.y + (accel * vertInput)).coerceIn(-vertTopSpeed, vertTopSpeed),
                state.rideVel.z)
            activeInput = true
        }
        else {
            state.rideVel = Vec3(
                state.rideVel.x,
                lerp(state.rideVel.y, 0.0, vertTopSpeed / 20.0),
                state.rideVel.z)
        }

        //Check if the ride should be gliding
        if (activeInput && state.stamina > 0.0) {
            state.gliding = false
        }else {
            state.gliding = true
        }

        //Only perform stamina logic if the ride does not have infinite stamina
        if (!vehicle.runtime.resolveBoolean(settings.infiniteStamina)) {
            if (activeInput) {
                state.stamina -= (0.05 / staminaStat).toFloat()
            }

            //Lose a base amount of stamina just for being airborne
            state.stamina -= (0.01 / staminaStat).toFloat()
        }
        else
        {
            state.stamina = 1.0f
        }

        //air resistance
        state.rideVel = Vec3(
            state.rideVel.x,
            state.rideVel.y,
            lerp( state.rideVel.z,0.0, topSpeed / ( 20.0 * 30.0))
        )
    }

    override fun angRollVel(
        settings: BirdAirSettings,
        state: BirdAirState,
        vehicle: PokemonEntity,
        driver: Player,
        deltaTime: Double
    ): Vec3 {
        return Vec3.ZERO
    }

    override fun rotationOnMouseXY(
        settings: BirdAirSettings,
        state: BirdAirState,
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
        val topSpeed = vehicle.runtime.resolveDouble(settings.topSpeedExpr)

        //Smooth out mouse input.
        val smoothingSpeed = 4
        val invertRoll = if (Cobblemon.config.invertRoll) -1 else 1
        val invertPitch = if (Cobblemon.config.invertPitch) -1 else 1
        val xInput = mouseXSmoother.getNewDeltaValue(mouseX * 0.1 * invertRoll, deltaTime * smoothingSpeed);
        val yInput = mouseYSmoother.getNewDeltaValue(mouseY * 0.1 * invertPitch, deltaTime * smoothingSpeed);

        //limit rolling based on handling and current speed.
        //modulated by speed so that when flapping idle in air you are ont wobbling around to look around
        val rotMin = 15.0
        var rollForce = xInput
        val rotLimit = max(handling * normalizeVal(state.rideVel.length(), 0.0, topSpeed).pow(3), rotMin)

        //Limit roll by non linearly decreasing inputs towards
        // a rotation limit based on the current distance from
        // that rotation limit
        if (abs(controller.roll + rollForce) < rotLimit) {
            if (sign(rollForce) == sign(controller.roll).toDouble()) {
                val d = abs(abs(controller.roll) - rotLimit)
                rollForce *= (d.pow(2)) / (rotLimit.pow(2))
            }
        } else if (sign(rollForce) == sign(controller.roll).toDouble()) {
            rollForce = 0.0
        }

        //Give the ability to yaw with x mouse input when at low speeds.
        val yawForce = xInput * ( 1.0 - normalizeVal(state.rideVel.length(), 0.0, topSpeed).pow(3))

        //yaw, pitch, roll
        return Vec3(yawForce, yInput, rollForce)
    }

    /*
    *  Normalizes the given value between a min and a max.
    *  The result is clamped between 0.0 and 1.0, where 0.0 represents x is at or below min
    *  and 1.0 represents x is at or above it.
    */
    private fun normalizeVal(x: Double, min: Double, max: Double): Double {
        require(max > min) { "max must be greater than min" }
        return ((x - min) / (max - min)).coerceIn(0.0, 1.0)
    }

    override fun canJump(
        settings: BirdAirSettings,
        state: BirdAirState,
        vehicle: PokemonEntity,
        driver: Player
    ): Boolean {
        return false
    }

    override fun setRideBar(
        settings: BirdAirSettings,
        state: BirdAirState,
        vehicle: PokemonEntity,
        driver: Player
    ): Float {
        return (state.stamina / 1.0f)
    }

    override fun jumpForce(
        settings: BirdAirSettings,
        state: BirdAirState,
        vehicle: PokemonEntity,
        driver: Player,
        jumpStrength: Int
    ): Vec3 {
        return Vec3.ZERO
    }

    override fun gravity(
        settings: BirdAirSettings,
        state: BirdAirState,
        vehicle: PokemonEntity,
        regularGravity: Double
    ): Double {
        return 0.0
    }

    override fun rideFovMultiplier(
        settings: BirdAirSettings,
        state: BirdAirState,
        vehicle: PokemonEntity,
        driver: Player
    ): Float {
        val topSpeed = vehicle.runtime.resolveDouble(settings.topSpeedExpr)
        val glideTopSpeed = vehicle.runtime.resolveDouble(settings.glideTopSpeedExpr)

        //Must I ensure that topspeed is greater than minimum?
        val normalizedGlideSpeed = normalizeVal(state.rideVel.length(), topSpeed, glideTopSpeed)

        //Only ever want the fov change to be a max of 0.2 and for it to have non linear scaling.
        return 1.0f + normalizedGlideSpeed.pow(2).toFloat() * 0.2f
    }

    override fun useAngVelSmoothing(
        settings: BirdAirSettings,
        state: BirdAirState,
        vehicle: PokemonEntity
    ): Boolean {
        return false
    }

    override fun useRidingAltPose(
        settings: BirdAirSettings,
        state: BirdAirState,
        vehicle: PokemonEntity,
        driver: Player
    ): Boolean {
        return false
    }

    override fun inertia(settings: BirdAirSettings, state: BirdAirState, vehicle: PokemonEntity): Double {
        return 0.5
    }

    override fun shouldRoll(settings: BirdAirSettings, state: BirdAirState, vehicle: PokemonEntity): Boolean {
        return true
    }

    override fun turnOffOnGround(settings: BirdAirSettings, state: BirdAirState, vehicle: PokemonEntity): Boolean {
        return false
    }

    override fun dismountOnShift(settings: BirdAirSettings, state: BirdAirState, vehicle: PokemonEntity): Boolean {
        return false
    }

    override fun shouldRotatePokemonHead(
        settings: BirdAirSettings,
        state: BirdAirState,
        vehicle: PokemonEntity
    ): Boolean {
        return false
    }

    override fun shouldRotatePlayerHead(
        settings: BirdAirSettings,
        state: BirdAirState,
        vehicle: PokemonEntity
    ): Boolean {
        return false
    }

    override fun createDefaultState() = BirdAirState()

}

class BirdAirSettings : RidingBehaviourSettings {
    //max y level for the ride
    var altitudeExpr: Expression = "q.get_ride_stats('JUMP', 'AIR', 200.0, 128.0)".asExpression()
        private set

    var infiniteAltitude: Expression = "false".asExpression()
        private set

    var infiniteStamina: Expression = "false".asExpression()
        private set

    var handlingExpr: Expression = "q.get_ride_stats('SKILL', 'AIR', 135.0, 45.0)".asExpression()
        private set

    var topSpeedExpr: Expression = "q.get_ride_stats('SPEED', 'AIR', 1.0, 0.35)".asExpression()
        private set

    var glideTopSpeedExpr: Expression = "q.get_ride_stats('SPEED', 'AIR', 2.0, 1.0)".asExpression()
        private set

    // Max accel is a whole 1.0 in 3 seconds. The conversion in the function below is to convert seconds to ticks
    var accelExpr: Expression = "q.get_ride_stats('ACCELERATION', 'AIR', (1.0 / (20.0 * 3.0)), (1.0 / (20.0 * 8.0)))".asExpression()
        private set

    //Seconds self propelled flight (glide is not self propelled in this case)
    var staminaExpr: Expression = "q.get_ride_stats('STAMINA', 'AIR', 120.0, 20.0)".asExpression()
        private set
}

class BirdAirState : RidingBehaviourState {
    override var isDirty = false

    var rideVel: Vec3 = Vec3.ZERO
        set(value) {
            field = value
            isDirty = true
        }

    var stamina: Float = 1.0f
        set(value) {
            field = value
            isDirty = true
        }

    var gliding: Boolean = false
        set(value) {
            field = value
            isDirty = true
        }

    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeVec3(rideVel)
        buffer.writeFloat(stamina)
        buffer.writeBoolean(gliding)
    }

    override fun decode(buffer: RegistryFriendlyByteBuf) {
        rideVel = buffer.readVec3()
        stamina = buffer.readFloat()
        gliding = buffer.readBoolean()
        isDirty = false
    }

    override fun reset() {
        rideVel = Vec3.ZERO
        stamina = 1.0f
        gliding = false
    }
}
