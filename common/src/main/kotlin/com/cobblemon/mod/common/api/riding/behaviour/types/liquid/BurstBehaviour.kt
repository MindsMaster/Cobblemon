/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.riding.behaviour.types.liquid

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.OrientationControllable
import com.cobblemon.mod.common.api.riding.RidingStyle
import com.cobblemon.mod.common.api.riding.behaviour.*
import com.cobblemon.mod.common.api.riding.posing.PoseOption
import com.cobblemon.mod.common.api.riding.posing.PoseProvider
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.util.blockPositionsAsListRounded
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.SmoothDouble
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.phys.Vec2
import net.minecraft.world.phys.Vec3
import net.minecraft.world.phys.shapes.Shapes

class BurstBehaviour : RidingBehaviour<BurstSettings, BurstState> {
    companion object {
        val KEY = cobblemonResource("liquid/burst")
        const val DASH_TICKS: Int = 60
    }

    override val key = KEY

    override fun getRidingStyle(settings: BurstSettings, state: BurstState): RidingStyle {
        return RidingStyle.LIQUID
    }

    val poseProvider = PoseProvider<BurstSettings, BurstState>(PoseType.FLOAT)
        .with(PoseOption(PoseType.SWIM) { _, _, entity -> entity.isSwimming && entity.entityData.get(PokemonEntity.MOVING) })

    override fun isActive(settings: BurstSettings, state: BurstState, vehicle: PokemonEntity): Boolean {
        //This could be kinda weird... what if the top of the mon is in a fluid but the bottom isnt?
        return Shapes.create(vehicle.boundingBox).blockPositionsAsListRounded().any {
            if (vehicle.isInWater || vehicle.isUnderWater) {
                return@any true
            }
            val blockState = vehicle.level().getBlockState(it)
            return@any !blockState.fluidState.isEmpty
        }
    }

    override fun pose(settings: BurstSettings, state: BurstState, vehicle: PokemonEntity): PoseType {
        return poseProvider.select(settings, state, vehicle)
    }

    override fun speed(
        settings: BurstSettings,
        state: BurstState,
        vehicle: PokemonEntity,
        driver: Player
    ): Float {
        if(state.dashing.get()) {
            state.ticks.set(state.ticks.get() + 1)
            if(state.ticks.get() >= DASH_TICKS) {
                state.dashing.set(false)
            }

            return 0.0F
        }

        state.dashing.set(true)
        return settings.dashSpeed
    }

    override fun rotation(
        settings: BurstSettings,
        state: BurstState,
        vehicle: PokemonEntity,
        driver: LivingEntity
    ): Vec2 {
        return Vec2(driver.xRot * 0.5f, driver.yRot)
    }

    override fun velocity(
        settings: BurstSettings,
        state: BurstState,
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
        settings: BurstSettings,
        state: BurstState,
        vehicle: PokemonEntity,
        driver: Player,
        deltaTime: Double
    ): Vec3 {
        return Vec3.ZERO
    }

    override fun rotationOnMouseXY(
        settings: BurstSettings,
        state: BurstState,
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
        settings: BurstSettings,
        state: BurstState,
        vehicle: PokemonEntity,
        driver: Player
    ): Boolean {
        TODO("Not yet implemented")
    }

    override fun setRideBar(
        settings: BurstSettings,
        state: BurstState,
        vehicle: PokemonEntity,
        driver: Player
    ): Float {
        return 0.0f
    }

    override fun jumpForce(
        settings: BurstSettings,
        state: BurstState,
        vehicle: PokemonEntity,
        driver: Player,
        jumpStrength: Int
    ): Vec3 {
        TODO("Not yet implemented")
    }

    override fun gravity(
        settings: BurstSettings,
        state: BurstState,
        vehicle: PokemonEntity,
        regularGravity: Double
    ): Double {
        return regularGravity
    }

    override fun rideFovMultiplier(
        settings: BurstSettings,
        state: BurstState,
        vehicle: PokemonEntity,
        driver: Player
    ): Float {
        return 1.0f
    }

    override fun useAngVelSmoothing(settings: BurstSettings, state: BurstState, vehicle: PokemonEntity): Boolean {
        return false
    }

    override fun useRidingAltPose(
        settings: BurstSettings,
        state: BurstState,
        vehicle: PokemonEntity,
        driver: Player
    ): ResourceLocation {
        return cobblemonResource("no_pose")
    }

    override fun inertia(settings: BurstSettings, state: BurstState, vehicle: PokemonEntity): Double {
        return 0.5
    }

    override fun shouldRoll(settings: BurstSettings, state: BurstState, vehicle: PokemonEntity): Boolean {
        return false
    }

    override fun turnOffOnGround(settings: BurstSettings, state: BurstState, vehicle: PokemonEntity): Boolean {
        return false
    }

    override fun dismountOnShift(settings: BurstSettings, state: BurstState, vehicle: PokemonEntity): Boolean {
        return false
    }

    override fun shouldRotatePokemonHead(
        settings: BurstSettings,
        state: BurstState,
        vehicle: PokemonEntity
    ): Boolean {
        return false
    }

    override fun shouldRotatePlayerHead(
        settings: BurstSettings,
        state: BurstState,
        vehicle: PokemonEntity
    ): Boolean {
        return false
    }

    override fun createDefaultState(settings: BurstSettings) = BurstState()
}

class BurstSettings : RidingBehaviourSettings {
    override val key = BurstBehaviour.KEY

    var dashSpeed = 1F
        private set

    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeResourceLocation(key)
        buffer.writeFloat(dashSpeed)
    }

    override fun decode(buffer: RegistryFriendlyByteBuf) {
        dashSpeed = buffer.readFloat()
    }
}

class BurstState : RidingBehaviourState() {
    var dashing = ridingState(false, Side.BOTH)
    var ticks = ridingState(0, Side.BOTH)

    override fun reset() {
        super.reset()
        dashing.set(false, forced = true)
        ticks.set(0, forced = true)
    }

    override fun toString(): String {
        return "BurstState(dashing=${dashing.get()}, ticks=${ticks.get()})"
    }

    override fun copy() = BurstState().also {
        it.rideVelocity.set(rideVelocity.get(), forced = true)
        it.stamina.set(stamina.get(), forced = true)
        it.dashing.set(dashing.get(), forced = true)
        it.ticks.set(ticks. get(), forced = true)
    }
}
