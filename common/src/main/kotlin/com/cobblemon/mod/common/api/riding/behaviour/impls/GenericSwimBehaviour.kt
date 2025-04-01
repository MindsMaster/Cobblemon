package com.cobblemon.mod.common.api.riding.behaviour.impls

import com.bedrockk.molang.runtime.value.DoubleValue
import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.OrientationControllable
import com.cobblemon.mod.common.api.riding.behaviour.NoState
import com.cobblemon.mod.common.api.riding.behaviour.RidingBehaviour
import com.cobblemon.mod.common.api.riding.behaviour.RidingBehaviourSettings
import com.cobblemon.mod.common.api.riding.controller.posing.PoseOption
import com.cobblemon.mod.common.api.riding.controller.posing.PoseProvider
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.util.*
import net.minecraft.util.SmoothDouble
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.phys.Vec2
import net.minecraft.world.phys.Vec3
import net.minecraft.world.phys.shapes.Shapes

class GenericLiquidBehaviour : RidingBehaviour<GenericLiquidSettings, NoState> {
    companion object {
        val KEY = cobblemonResource("swim/generic")
    }

    val poseProvider: PoseProvider = PoseProvider(PoseType.FLOAT)
        .with(PoseOption(PoseType.SWIM) { it.isSwimming && it.entityData.get(PokemonEntity.MOVING) })

    override fun isActive(settings: GenericLiquidSettings, state: NoState, vehicle: PokemonEntity): Boolean {
        return Shapes.create(vehicle.boundingBox).blockPositionsAsListRounded().any {
            if (vehicle.isInWater || vehicle.isUnderWater) {
                return@any true
            }
            val blockState = vehicle.level().getBlockState(it)
            return@any !blockState.fluidState.isEmpty
        }
    }

    override fun pose(settings: GenericLiquidSettings, state: NoState, vehicle: PokemonEntity): PoseType {
        return poseProvider.select(vehicle)
    }

    override fun speed(settings: GenericLiquidSettings, state: NoState, vehicle: PokemonEntity, driver: Player): Float {
        return vehicle.runtime.resolveFloat(settings.speed)
    }

    override fun rotation(
        settings: GenericLiquidSettings,
        state: NoState,
        vehicle: PokemonEntity,
        driver: LivingEntity
    ): Vec2 {
        return Vec2(driver.xRot * 0.5f, driver.yRot)
    }

    override fun velocity(
        settings: GenericLiquidSettings,
        state: NoState,
        vehicle: PokemonEntity,
        driver: Player,
        input: Vec3
    ): Vec3 {
        val runtime = vehicle.runtime
        val driveFactor = runtime.resolveFloat(settings.driveFactor)
        val strafeFactor = runtime.resolveFloat(settings.strafeFactor)
        val f = driver.xxa * strafeFactor
        var g = driver.zza * driveFactor
        if (g <= 0.0f) {
            g *= runtime.resolveFloat(settings.reverseDriveFactor)
        }

        val velocity = Vec3(f.toDouble(), 0.0, g.toDouble())

        return velocity
    }

    override fun angRollVel(
        settings: GenericLiquidSettings,
        state: NoState,
        vehicle: PokemonEntity,
        driver: Player,
        deltaTime: Double
    ): Vec3 {
        return Vec3.ZERO
    }

    override fun rotationOnMouseXY(
        settings: GenericLiquidSettings,
        state: NoState,
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
        return Vec3(0.0, mouseY * invertPitch, mouseX * invertRoll)
    }

    override fun canJump(
        settings: GenericLiquidSettings,
        state: NoState,
        vehicle: PokemonEntity,
        driver: Player
    ): Boolean {
        return vehicle.runtime.resolveBoolean(settings.canJump)
    }

    override fun setRideBar(
        settings: GenericLiquidSettings,
        state: NoState,
        vehicle: PokemonEntity,
        driver: Player
    ): Float {
        return 0.0f
    }

    override fun jumpForce(
        settings: GenericLiquidSettings,
        state: NoState,
        vehicle: PokemonEntity,
        driver: Player,
        jumpStrength: Int
    ): Vec3 {
        val runtime = vehicle.runtime
        runtime.environment.query.addFunction("jump_strength") { DoubleValue(jumpStrength.toDouble()) }
        val jumpVector = settings.jumpVector.map { runtime.resolveFloat(it) }
        return Vec3(jumpVector[0].toDouble(), jumpVector[1].toDouble(), jumpVector[2].toDouble())
    }

    override fun gravity(
        settings: GenericLiquidSettings,
        state: NoState,
        vehicle: PokemonEntity,
        regularGravity: Double
    ): Double {
        return regularGravity
    }

    override fun rideFovMultiplier(
        settings: GenericLiquidSettings,
        state: NoState,
        vehicle: PokemonEntity,
        driver: Player
    ): Float {
        return 1.0f
    }

    override fun useAngVelSmoothing(settings: GenericLiquidSettings, state: NoState, vehicle: PokemonEntity): Boolean {
        return false
    }

    override fun useRidingAltPose(
        settings: GenericLiquidSettings,
        state: NoState,
        vehicle: PokemonEntity,
        driver: Player
    ): Boolean {
        return false
    }

    override fun inertia(settings: GenericLiquidSettings, state: NoState, vehicle: PokemonEntity): Double {
        return 0.5
    }

    override fun shouldRoll(settings: GenericLiquidSettings, state: NoState, vehicle: PokemonEntity): Boolean {
        return false
    }

    override fun turnOffOnGround(settings: GenericLiquidSettings, state: NoState, vehicle: PokemonEntity): Boolean {
        return false
    }

    override fun dismountOnShift(settings: GenericLiquidSettings, state: NoState, vehicle: PokemonEntity): Boolean {
        return false
    }

    override fun shouldRotatePokemonHead(
        settings: GenericLiquidSettings,
        state: NoState,
        vehicle: PokemonEntity
    ): Boolean {
        return false
    }

    override fun shouldRotatePlayerHead(
        settings: GenericLiquidSettings,
        state: NoState,
        vehicle: PokemonEntity
    ): Boolean {
        return false
    }

}

class GenericLiquidSettings : RidingBehaviourSettings {
    var canJump = "true".asExpression()
        private set

    var jumpVector = listOf("0".asExpression(), "0.3".asExpression(), "0".asExpression())
        private set

    var speed = "1.0".asExpression()
        private set

    var driveFactor = "1.0".asExpression()
        private set

    var reverseDriveFactor = "0.25".asExpression()
        private set

    var strafeFactor = "0.2".asExpression()
        private set
}
