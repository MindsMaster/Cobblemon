package com.cobblemon.mod.common.api.riding.behaviour.types

import com.bedrockk.molang.Expression
import com.cobblemon.mod.common.api.riding.behaviour.NoState
import com.cobblemon.mod.common.api.riding.behaviour.RidingBehaviour
import com.cobblemon.mod.common.api.riding.behaviour.RidingBehaviourSettings
import com.cobblemon.mod.common.api.riding.behaviour.RidingBehaviourState
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.entity.pokemon.PokemonBehaviourFlag
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.util.*
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.SmoothDouble
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.phys.Vec2
import net.minecraft.world.phys.Vec3

class FallToGlideCompositeBehaviour : RidingBehaviour<FallToGlideCompositeSettings, FallToGlideCompositeState> {
    companion object {
        val KEY = cobblemonResource("composite/fall_to_glide")
    }

    override val key = KEY

    val landBehaviour: GenericLandBehaviour = GenericLandBehaviour()
    val glideBehaviour: GliderAirBehaviour = GliderAirBehaviour()

    override fun tick(
        settings: FallToGlideCompositeSettings,
        state: FallToGlideCompositeState,
        vehicle: PokemonEntity,
        driver: Player,
        input: Vec3
    ) {
        val shouldBeFlying = checkShouldBeFlying(settings, vehicle, state.activeController == GliderAirBehaviour.KEY)
        if (state.activeController == GliderAirBehaviour.KEY && !shouldBeFlying) { // && entity.onGround() && state.timeTransitioned + 20 < entity.level().gameTime) {
            state.activeController = GenericLandBehaviour.KEY
            vehicle.setBehaviourFlag(PokemonBehaviourFlag.FLYING, false)
        } else if (state.activeController == GenericLandBehaviour.KEY && shouldBeFlying) {
            state.activeController = GliderAirBehaviour.KEY
            vehicle.setBehaviourFlag(PokemonBehaviourFlag.FLYING, true)
        }
    }

    private fun checkShouldBeFlying(
        settings: FallToGlideCompositeSettings,
        vehicle: PokemonEntity,
        isFlyingAlready: Boolean
    ): Boolean {
        val runtime = vehicle.runtime
        val minFallingSpeed = runtime.resolveFloat(settings.minimumFallSpeed)
        val minForwardSpeed = runtime.resolveFloat(settings.minimumForwardSpeed)
        val grounded = vehicle.onGround()
        return if (isFlyingAlready) {
            !grounded
        } else {
            vehicle.deltaMovement.y <= -minFallingSpeed && vehicle.deltaMovement.horizontalDistance() >= minForwardSpeed
        }
    }

    override fun isActive(
        settings: FallToGlideCompositeSettings,
        state: FallToGlideCompositeState,
        vehicle: PokemonEntity
    ): Boolean {
        return true
    }

    override fun pose(
        settings: FallToGlideCompositeSettings,
        state: FallToGlideCompositeState,
        vehicle: PokemonEntity
    ): PoseType {
        return when (state.activeController) {
            GenericLandBehaviour.KEY -> landBehaviour.pose(settings.land, state.landState, vehicle)
            GliderAirBehaviour.KEY -> glideBehaviour.pose(settings.glide, NoState, vehicle)
            else -> error("Invalid controller: ${state.activeController}")
        }
    }

    override fun speed(
        settings: FallToGlideCompositeSettings,
        state: FallToGlideCompositeState,
        vehicle: PokemonEntity,
        driver: Player
    ): Float {
        return when (state.activeController) {
            GenericLandBehaviour.KEY -> landBehaviour.speed(settings.land, state.landState, vehicle, driver)
            GliderAirBehaviour.KEY -> glideBehaviour.speed(settings.glide, NoState, vehicle, driver)
            else -> error("Invalid controller: ${state.activeController}")
        }
    }

    override fun rotation(
        settings: FallToGlideCompositeSettings,
        state: FallToGlideCompositeState,
        vehicle: PokemonEntity,
        driver: LivingEntity
    ): Vec2 {
        return when (state.activeController) {
            GenericLandBehaviour.KEY -> landBehaviour.rotation(settings.land, state.landState, vehicle, driver)
            GliderAirBehaviour.KEY -> glideBehaviour.rotation(settings.glide, NoState, vehicle, driver)
            else -> error("Invalid controller: ${state.activeController}")
        }
    }

    override fun velocity(
        settings: FallToGlideCompositeSettings,
        state: FallToGlideCompositeState,
        vehicle: PokemonEntity,
        driver: Player,
        input: Vec3
    ): Vec3 {
        return when (state.activeController) {
            GenericLandBehaviour.KEY -> landBehaviour.velocity(settings.land, state.landState, vehicle, driver, input)
            GliderAirBehaviour.KEY -> glideBehaviour.velocity(settings.glide, NoState, vehicle, driver, input)
            else -> error("Invalid controller: ${state.activeController}")
        }
    }

    override fun angRollVel(
        settings: FallToGlideCompositeSettings,
        state: FallToGlideCompositeState,
        vehicle: PokemonEntity,
        driver: Player,
        deltaTime: Double
    ): Vec3 {
        return when (state.activeController) {
            GenericLandBehaviour.KEY -> landBehaviour.angRollVel(
                settings.land,
                state.landState,
                vehicle,
                driver,
                deltaTime
            )

            GliderAirBehaviour.KEY -> glideBehaviour.angRollVel(settings.glide, NoState, vehicle, driver, deltaTime)
            else -> error("Invalid controller: ${state.activeController}")
        }
    }

    override fun rotationOnMouseXY(
        settings: FallToGlideCompositeSettings,
        state: FallToGlideCompositeState,
        vehicle: PokemonEntity,
        driver: Player,
        mouseY: Double,
        mouseX: Double,
        mouseYSmoother: SmoothDouble,
        mouseXSmoother: SmoothDouble,
        sensitivity: Double,
        deltaTime: Double
    ): Vec3 {
        return when (state.activeController) {
            GenericLandBehaviour.KEY -> landBehaviour.rotationOnMouseXY(
                settings.land,
                state.landState,
                vehicle,
                driver,
                mouseY,
                mouseX,
                mouseYSmoother,
                mouseXSmoother,
                sensitivity,
                deltaTime
            )

            GliderAirBehaviour.KEY -> glideBehaviour.rotationOnMouseXY(
                settings.glide,
                NoState,
                vehicle,
                driver,
                mouseY,
                mouseX,
                mouseYSmoother,
                mouseXSmoother,
                sensitivity,
                deltaTime
            )

            else -> error("Invalid controller: ${state.activeController}")
        }
    }

    override fun canJump(
        settings: FallToGlideCompositeSettings,
        state: FallToGlideCompositeState,
        vehicle: PokemonEntity,
        driver: Player
    ): Boolean {
        return when (state.activeController) {
            GenericLandBehaviour.KEY -> landBehaviour.canJump(settings.land, state.landState, vehicle, driver)
            GliderAirBehaviour.KEY -> glideBehaviour.canJump(settings.glide, NoState, vehicle, driver)
            else -> error("Invalid controller: ${state.activeController}")
        }
    }

    override fun setRideBar(
        settings: FallToGlideCompositeSettings,
        state: FallToGlideCompositeState,
        vehicle: PokemonEntity,
        driver: Player
    ): Float {
        return when (state.activeController) {
            GenericLandBehaviour.KEY -> landBehaviour.setRideBar(settings.land, state.landState, vehicle, driver)
            GliderAirBehaviour.KEY -> glideBehaviour.setRideBar(settings.glide, NoState, vehicle, driver)
            else -> error("Invalid controller: ${state.activeController}")
        }
    }

    override fun jumpForce(
        settings: FallToGlideCompositeSettings,
        state: FallToGlideCompositeState,
        vehicle: PokemonEntity,
        driver: Player,
        jumpStrength: Int
    ): Vec3 {
        return when (state.activeController) {
            GenericLandBehaviour.KEY -> landBehaviour.jumpForce(
                settings.land,
                state.landState,
                vehicle,
                driver,
                jumpStrength
            )

            GliderAirBehaviour.KEY -> glideBehaviour.jumpForce(settings.glide, NoState, vehicle, driver, jumpStrength)
            else -> error("Invalid controller: ${state.activeController}")
        }
    }

    override fun gravity(
        settings: FallToGlideCompositeSettings,
        state: FallToGlideCompositeState,
        vehicle: PokemonEntity,
        regularGravity: Double
    ): Double {
        return when (state.activeController) {
            GenericLandBehaviour.KEY -> landBehaviour.gravity(settings.land, state.landState, vehicle, regularGravity)
            GliderAirBehaviour.KEY -> glideBehaviour.gravity(settings.glide, NoState, vehicle, regularGravity)
            else -> error("Invalid controller: ${state.activeController}")
        }
    }

    override fun rideFovMultiplier(
        settings: FallToGlideCompositeSettings,
        state: FallToGlideCompositeState,
        vehicle: PokemonEntity,
        driver: Player
    ): Float {
        return when (state.activeController) {
            GenericLandBehaviour.KEY -> landBehaviour.rideFovMultiplier(settings.land, state.landState, vehicle, driver)
            GliderAirBehaviour.KEY -> glideBehaviour.rideFovMultiplier(settings.glide, NoState, vehicle, driver)
            else -> error("Invalid controller: ${state.activeController}")
        }
    }

    override fun useAngVelSmoothing(
        settings: FallToGlideCompositeSettings,
        state: FallToGlideCompositeState,
        vehicle: PokemonEntity
    ): Boolean {
        return when (state.activeController) {
            GenericLandBehaviour.KEY -> landBehaviour.useAngVelSmoothing(settings.land, state.landState, vehicle)
            GliderAirBehaviour.KEY -> glideBehaviour.useAngVelSmoothing(settings.glide, NoState, vehicle)
            else -> error("Invalid controller: ${state.activeController}")
        }
    }

    override fun useRidingAltPose(
        settings: FallToGlideCompositeSettings,
        state: FallToGlideCompositeState,
        vehicle: PokemonEntity,
        driver: Player
    ): Boolean {
        return when (state.activeController) {
            GenericLandBehaviour.KEY -> landBehaviour.useRidingAltPose(settings.land, state.landState, vehicle, driver)
            GliderAirBehaviour.KEY -> glideBehaviour.useRidingAltPose(settings.glide, NoState, vehicle, driver)
            else -> error("Invalid controller: ${state.activeController}")
        }
    }

    override fun inertia(
        settings: FallToGlideCompositeSettings,
        state: FallToGlideCompositeState,
        vehicle: PokemonEntity
    ): Double {
        return when (state.activeController) {
            GenericLandBehaviour.KEY -> landBehaviour.inertia(settings.land, state.landState, vehicle)
            GliderAirBehaviour.KEY -> glideBehaviour.inertia(settings.glide, NoState, vehicle)
            else -> error("Invalid controller: ${state.activeController}")
        }
    }

    override fun shouldRoll(
        settings: FallToGlideCompositeSettings,
        state: FallToGlideCompositeState,
        vehicle: PokemonEntity
    ): Boolean {
        return when (state.activeController) {
            GenericLandBehaviour.KEY -> landBehaviour.shouldRoll(settings.land, state.landState, vehicle)
            GliderAirBehaviour.KEY -> glideBehaviour.shouldRoll(settings.glide, NoState, vehicle)
            else -> error("Invalid controller: ${state.activeController}")
        }
    }

    override fun turnOffOnGround(
        settings: FallToGlideCompositeSettings,
        state: FallToGlideCompositeState,
        vehicle: PokemonEntity
    ): Boolean {
        return when (state.activeController) {
            GenericLandBehaviour.KEY -> landBehaviour.turnOffOnGround(settings.land, state.landState, vehicle)
            GliderAirBehaviour.KEY -> glideBehaviour.turnOffOnGround(settings.glide, NoState, vehicle)
            else -> error("Invalid controller: ${state.activeController}")
        }
    }

    override fun dismountOnShift(
        settings: FallToGlideCompositeSettings,
        state: FallToGlideCompositeState,
        vehicle: PokemonEntity
    ): Boolean {
        return when (state.activeController) {
            GenericLandBehaviour.KEY -> landBehaviour.dismountOnShift(settings.land, state.landState, vehicle)
            GliderAirBehaviour.KEY -> glideBehaviour.dismountOnShift(settings.glide, NoState, vehicle)
            else -> error("Invalid controller: ${state.activeController}")
        }
    }

    override fun shouldRotatePokemonHead(
        settings: FallToGlideCompositeSettings,
        state: FallToGlideCompositeState,
        vehicle: PokemonEntity
    ): Boolean {
        return when (state.activeController) {
            GenericLandBehaviour.KEY -> landBehaviour.shouldRotatePokemonHead(settings.land, state.landState, vehicle)
            GliderAirBehaviour.KEY -> glideBehaviour.shouldRotatePokemonHead(settings.glide, NoState, vehicle)
            else -> error("Invalid controller: ${state.activeController}")
        }
    }

    override fun shouldRotatePlayerHead(
        settings: FallToGlideCompositeSettings,
        state: FallToGlideCompositeState,
        vehicle: PokemonEntity
    ): Boolean {
        return when (state.activeController) {
            GenericLandBehaviour.KEY -> landBehaviour.shouldRotatePlayerHead(settings.land, state.landState, vehicle)
            GliderAirBehaviour.KEY -> glideBehaviour.shouldRotatePlayerHead(settings.glide, NoState, vehicle)
            else -> error("Invalid controller: ${state.activeController}")
        }
    }

    override fun createDefaultState() = FallToGlideCompositeState()


}

class FallToGlideCompositeSettings : RidingBehaviourSettings {
    override val key = FallToGlideCompositeBehaviour.KEY

    var glide: GliderAirSettings = GliderAirSettings()
        private set

    var land: GenericLandSettings = GenericLandSettings()
        private set

    var minimumForwardSpeed: Expression = "0.0".asExpression()
        private set

    var minimumFallSpeed: Expression = "0.5".asExpression()
        private set

    override fun encode(buffer: RegistryFriendlyByteBuf) {
        glide.encode(buffer)
        land.encode(buffer)
        buffer.writeExpression(minimumForwardSpeed)
        buffer.writeExpression(minimumFallSpeed)
    }

    override fun decode(buffer: RegistryFriendlyByteBuf) {
        glide.decode(buffer)
        land.decode(buffer)
        minimumForwardSpeed = buffer.readExpression()
        minimumFallSpeed = buffer.readExpression()
    }
}

class FallToGlideCompositeState : RidingBehaviourState {

    private var _isDirty = false
    override var isDirty: Boolean
        get() = _isDirty || landState.isDirty
        set(value) {
            _isDirty = value
            landState.isDirty = value
        }

    var activeController: ResourceLocation = GenericLandBehaviour.KEY
        set(value) {
            if (field != value)
                isDirty = true
            field = value
        }

    var landState: GenericLandState = GenericLandState()

    var timeTransitioned = -100L

    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeResourceLocation(activeController)
        landState.encode(buffer)
    }

    override fun decode(buffer: RegistryFriendlyByteBuf) {
        activeController = buffer.readResourceLocation()
        landState.decode(buffer)
    }

    override fun reset() {
        activeController = GenericLandBehaviour.KEY
        landState.reset()
        timeTransitioned = -100L
    }

    override fun toString(): String {
        return "FallToGlideCompositeState(activeController=$activeController, landState=$landState, timeTransitioned=$timeTransitioned)"
    }
}
