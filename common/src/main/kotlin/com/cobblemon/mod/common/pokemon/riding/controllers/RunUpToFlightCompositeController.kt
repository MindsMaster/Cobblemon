/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.pokemon.riding.controllers

import com.bedrockk.molang.Expression
import com.cobblemon.mod.common.api.riding.RidingState
import com.cobblemon.mod.common.api.riding.controller.RideController
import com.cobblemon.mod.common.api.riding.controller.RideControllerFactory
import com.cobblemon.mod.common.api.riding.controller.posing.PoseOption
import com.cobblemon.mod.common.api.riding.controller.posing.PoseProvider
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.entity.pokemon.PokemonBehaviourFlag
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.pokemon.riding.states.*
import com.cobblemon.mod.common.util.adapters.RideControllerAdapter
import com.cobblemon.mod.common.util.asExpression
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.getString
import com.cobblemon.mod.common.util.readString
import com.cobblemon.mod.common.util.resolveInt
import com.cobblemon.mod.common.util.writeString
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.util.SmoothDouble
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.phys.Vec2
import net.minecraft.world.phys.Vec3

class RunUpToFlightCompositeController(val entity: PokemonEntity) : RideController {
    override val key = KEY
    override val poseProvider = PoseProvider(PoseType.STAND)
        .with(PoseOption(PoseType.WALK) { it.entityData.get(PokemonEntity.MOVING) })

    override val isActive = true

    override val state = RunUpToFlightCompositeState()

    var minimumSpeed: Expression = "0.5".asExpression()
        private set
    var minimumJump: Expression = "0.5".asExpression()
        private set

    var landController: GenericLandController = GenericLandController(entity)
        private set
    var flightController: BirdAirController = BirdAirController()
        private set

    private fun shouldTransitionToGround(state: RunUpToFlightCompositeState, entity: PokemonEntity): Boolean {
        return state.activeController == flightController.key
                && entity.onGround()
                && state.timeTransitioned + 20 < entity.level().gameTime
    }

    private fun shouldTransitionToAir(state: RunUpToFlightCompositeState, entity: PokemonEntity, driver: Player): Boolean {
        return state.activeController == landController.key
                && driver.jumping
                && state.timeTransitioned + 20 < entity.level().gameTime
    }

    override fun tick(entity: PokemonEntity, driver: Player, input: Vec3) {
        val flightState = flightController.state
        val groundState = landController.state
        if (shouldTransitionToGround(state, entity)) {
            //Pass the speed to the next state
            groundState.currSpeed = flightState.currSpeed
            groundState.rideVel = flightState.rideVel
            groundState.stamina = flightState.stamina

            state.activeController = landController.key
            entity.setBehaviourFlag(PokemonBehaviourFlag.FLYING, false)
            state.timeTransitioned = entity.level().gameTime
        }
        else if (shouldTransitionToAir(state, entity, driver)) {
            //Pass the speed to the next state
            flightState.currSpeed = groundState.currSpeed
            flightState.rideVel = groundState.rideVel
            flightState.stamina = groundState.stamina

            state.activeController = flightController.key
            entity.setBehaviourFlag(PokemonBehaviourFlag.FLYING, true)
            state.timeTransitioned = entity.level().gameTime
        }
    }

    override fun pose(entity: PokemonEntity): PoseType {
        return getActiveController(entity).poseProvider.select(entity)
    }

    fun getActiveController(entity: PokemonEntity): RideController {
        if (state.activeController != null) {
            return if (state.activeController == landController.key) landController else flightController
        }

        if (entity.getCurrentPoseType() in PoseType.FLYING_POSES) {
            entity.setBehaviourFlag(PokemonBehaviourFlag.FLYING, true)
            state.activeController = flightController.key
            state.timeTransitioned = entity.level().gameTime
            return flightController
        }
        else {
            entity.setBehaviourFlag(PokemonBehaviourFlag.FLYING, false)
            state.activeController = landController.key
            state.timeTransitioned = entity.level().gameTime
            return landController
        }
    }

    override fun speed(entity: PokemonEntity, driver: Player): Float {
        return getActiveController(entity).speed(entity, driver)
    }

    override fun rotation(entity: PokemonEntity, driver: LivingEntity): Vec2 {
        return getActiveController(entity).rotation(entity, driver)
    }

    override fun velocity(
        entity: PokemonEntity,
        driver: Player,
        input: Vec3
    ): Vec3 {
        return getActiveController(entity).velocity(entity, driver, input)
    }

    override fun angRollVel(entity: PokemonEntity, driver: Player, deltaTime: Double): Vec3 {
        return getActiveController(entity).angRollVel(entity, driver, deltaTime)
    }

    override fun rideFovMult(entity: PokemonEntity, driver: Player): Float {
        return getActiveController(entity).rideFovMult(entity, driver)
    }

    override fun setRideBar(entity: PokemonEntity, driver: Player): Float {
        return getActiveController(entity).setRideBar(entity, driver)
    }

    override fun canJump(entity: PokemonEntity, driver: Player): Boolean {
        return getActiveController(entity).canJump(entity, driver)
    }

    override fun jumpForce(
        entity: PokemonEntity,
        driver: Player,
        jumpStrength: Int
    ): Vec3 {
        val controller = getActiveController(entity)
        return controller.jumpForce(entity, driver, jumpStrength)
    }

    override fun gravity(entity: PokemonEntity, regularGravity: Double): Double? {
        return getActiveController(entity).gravity(entity, regularGravity)
    }

    override fun inertia(entity: PokemonEntity): Double {
        return getActiveController(entity).inertia(entity)
    }

    override fun shouldRoll(entity: PokemonEntity): Boolean = getActiveController(entity).shouldRoll(entity)

    override fun useAngVelSmoothing(entity: PokemonEntity): Boolean = getActiveController(entity).useAngVelSmoothing(entity)

    override fun useRidingAltPose(entity: PokemonEntity, driver: Player): Boolean {
        return getActiveController(entity).useRidingAltPose(entity, driver)
    }

    override fun rotationOnMouseXY
                (entity: PokemonEntity,
                 driver: Player,
                 yMouse: Double,
                 xMouse: Double,
                 yMouseSmoother: SmoothDouble,
                 xMouseSmoother: SmoothDouble,
                 sensitivity: Double,
                 deltaTime: Double ): Vec3
    {
        return getActiveController(entity).rotationOnMouseXY(entity,
                                                            driver,
                                                            yMouse,
                                                            xMouse,
                                                            yMouseSmoother,
                                                            xMouseSmoother,
                                                            sensitivity,
                                                            deltaTime )
    }

    override fun dismountOnShift(entity: PokemonEntity): Boolean = getActiveController(entity).dismountOnShift(entity)

    override fun encode(buffer: RegistryFriendlyByteBuf) {
        super.encode(buffer)
        state.encode(buffer)
        buffer.writeString(minimumSpeed.getString())
        buffer.writeString(minimumJump.getString())
        landController.encode(buffer)
        flightController.encode(buffer)
    }

    override fun decode(buffer: RegistryFriendlyByteBuf) {
        minimumSpeed = buffer.readString().asExpression()
        minimumJump = buffer.readString().asExpression()
        state.decode(buffer)
        landController = buffer.readResourceLocation().let { key ->
            val controller = RideControllerFactory.create<GenericLandController>(key, entity)
            controller.decode(buffer)
            controller
        }
        flightController = buffer.readResourceLocation().let { key ->
            val controller = RideControllerFactory.create<BirdAirController>(key, entity)
            controller.decode(buffer)
            controller
        }
    }

    companion object {
        val KEY = cobblemonResource("composite/run_up_to_flight")
    }
}
