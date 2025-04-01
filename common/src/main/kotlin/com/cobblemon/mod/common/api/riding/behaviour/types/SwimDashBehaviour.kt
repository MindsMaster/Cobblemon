package com.cobblemon.mod.common.api.riding.behaviour.types

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.OrientationControllable
import com.cobblemon.mod.common.api.riding.behaviour.RidingBehaviour
import com.cobblemon.mod.common.api.riding.behaviour.RidingBehaviourSettings
import com.cobblemon.mod.common.api.riding.behaviour.RidingBehaviourState
import com.cobblemon.mod.common.api.riding.controller.posing.PoseOption
import com.cobblemon.mod.common.api.riding.controller.posing.PoseProvider
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.pokemon.riding.controllers.SwimDashController.Companion.DASH_TICKS
import com.cobblemon.mod.common.util.blockPositionsAsListRounded
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.util.SmoothDouble
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.phys.Vec2
import net.minecraft.world.phys.Vec3
import net.minecraft.world.phys.shapes.Shapes

class SwimDashBehaviour : RidingBehaviour<SwimDashSettings, SwimDashState> {
    companion object {
        val KEY = cobblemonResource("swim/dash")
    }

    val poseProvider: PoseProvider = PoseProvider(PoseType.FLOAT)
        .with(PoseOption(PoseType.SWIM) { it.isSwimming && it.entityData.get(PokemonEntity.MOVING) })

    override fun isActive(settings: SwimDashSettings, state: SwimDashState, vehicle: PokemonEntity): Boolean {
        //This could be kinda weird... what if the top of the mon is in a fluid but the bottom isnt?
        return Shapes.create(vehicle.boundingBox).blockPositionsAsListRounded().any {
            if (vehicle.isInWater || vehicle.isUnderWater) {
                return@any true
            }
            val blockState = vehicle.level().getBlockState(it)
            return@any !blockState.fluidState.isEmpty
        }
    }

    override fun pose(settings: SwimDashSettings, state: SwimDashState, vehicle: PokemonEntity): PoseType {
        return poseProvider.select(vehicle)
    }

    override fun speed(
        settings: SwimDashSettings,
        state: SwimDashState,
        vehicle: PokemonEntity,
        driver: Player
    ): Float {
        if(state.dashing) {
            if(state.ticks++ >= DASH_TICKS) {
                state.dashing = false
            }

            return 0.0F
        }

        state.dashing = true
        return settings.dashSpeed
    }

    override fun rotation(
        settings: SwimDashSettings,
        state: SwimDashState,
        vehicle: PokemonEntity,
        driver: LivingEntity
    ): Vec2 {
        return Vec2(driver.xRot * 0.5f, driver.yRot)
    }

    override fun velocity(
        settings: SwimDashSettings,
        state: SwimDashState,
        vehicle: PokemonEntity,
        driver: Player,
        input: Vec3
    ): Vec3 {
        val f = driver.xxa * 0.05f
        var g = driver.zza * 0.6f
        if (g <= 0.0f) {
            g *= 0.25f
        }

        return Vec3(f.toDouble(), 0.0, g.toDouble())
    }

    override fun angRollVel(
        settings: SwimDashSettings,
        state: SwimDashState,
        vehicle: PokemonEntity,
        driver: Player,
        deltaTime: Double
    ): Vec3 {
        return Vec3.ZERO
    }

    override fun rotationOnMouseXY(
        settings: SwimDashSettings,
        state: SwimDashState,
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

        //Might need to add the smoothing here for default.
        val invertRoll = if (Cobblemon.config.invertRoll) -1 else 1
        val invertPitch = if (Cobblemon.config.invertPitch) -1 else 1
        return Vec3(0.0, mouseX * invertPitch, mouseY * invertRoll)
    }

    override fun canJump(
        settings: SwimDashSettings,
        state: SwimDashState,
        vehicle: PokemonEntity,
        driver: Player
    ): Boolean {
        TODO("Not yet implemented")
    }

    override fun setRideBar(
        settings: SwimDashSettings,
        state: SwimDashState,
        vehicle: PokemonEntity,
        driver: Player
    ): Float {
        return 0.0f
    }

    override fun jumpForce(
        settings: SwimDashSettings,
        state: SwimDashState,
        vehicle: PokemonEntity,
        driver: Player,
        jumpStrength: Int
    ): Vec3 {
        TODO("Not yet implemented")
    }

    override fun gravity(
        settings: SwimDashSettings,
        state: SwimDashState,
        vehicle: PokemonEntity,
        regularGravity: Double
    ): Double {
        return regularGravity
    }

    override fun rideFovMultiplier(
        settings: SwimDashSettings,
        state: SwimDashState,
        vehicle: PokemonEntity,
        driver: Player
    ): Float {
        return 1.0f
    }

    override fun useAngVelSmoothing(settings: SwimDashSettings, state: SwimDashState, vehicle: PokemonEntity): Boolean {
        return false
    }

    override fun useRidingAltPose(
        settings: SwimDashSettings,
        state: SwimDashState,
        vehicle: PokemonEntity,
        driver: Player
    ): Boolean {
        return false
    }

    override fun inertia(settings: SwimDashSettings, state: SwimDashState, vehicle: PokemonEntity): Double {
        return 0.5
    }

    override fun shouldRoll(settings: SwimDashSettings, state: SwimDashState, vehicle: PokemonEntity): Boolean {
        return false
    }

    override fun turnOffOnGround(settings: SwimDashSettings, state: SwimDashState, vehicle: PokemonEntity): Boolean {
        return false
    }

    override fun dismountOnShift(settings: SwimDashSettings, state: SwimDashState, vehicle: PokemonEntity): Boolean {
        return false
    }

    override fun shouldRotatePokemonHead(
        settings: SwimDashSettings,
        state: SwimDashState,
        vehicle: PokemonEntity
    ): Boolean {
        return false
    }

    override fun shouldRotatePlayerHead(
        settings: SwimDashSettings,
        state: SwimDashState,
        vehicle: PokemonEntity
    ): Boolean {
        return false
    }

    override fun createDefaultState() = SwimDashState()
}

class SwimDashSettings : RidingBehaviourSettings {
    var dashSpeed = 1F
        private set
}

class SwimDashState : RidingBehaviourState {
    override var isDirty = false
    var dashing = false
    var ticks = 0

    override fun encode(buffer: RegistryFriendlyByteBuf) = Unit
    override fun decode(buffer: RegistryFriendlyByteBuf) = Unit

    override fun reset() {
        dashing = false
        ticks = 0
    }
}
