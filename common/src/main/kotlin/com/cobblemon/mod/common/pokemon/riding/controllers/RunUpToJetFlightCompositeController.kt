/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.pokemon.riding.controllers

import com.bedrockk.molang.Expression
import com.cobblemon.mod.common.api.riding.controller.RideController
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
import com.cobblemon.mod.common.util.writeString
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.util.SmoothDouble
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.phys.Vec2
import net.minecraft.world.phys.Vec3

class RunUpToJetFlightCompositeController : RideController {
    companion object {
        val KEY = cobblemonResource("composite/run_up_to_jet_flight")
    }

    var minimumSpeed: Expression = "0.5".asExpression()
        private set
    var minimumJump: Expression = "0.5".asExpression()
        private set

    var landController: GenericLandController = GenericLandController()
        private set
    var flightController: JetAirController = JetAirController()
        private set

    @Transient
    override val key = KEY

    @Transient
    override val poseProvider = PoseProvider(PoseType.STAND)
        .with(PoseOption(PoseType.WALK) {
            it.entityData.get(PokemonEntity.MOVING)
        })

    @Transient
    override val state = RunUpToFlightCompositeState()

    fun shouldTransitionToGround(state: RunUpToFlightCompositeState, entity: PokemonEntity, driver: Player): Boolean {
        if (state.activeController != flightController.key) return false
        if (state.timeTransitioned + 20 >= entity.level().gameTime) return false
        return driver.isSprinting || entity.onGround()
    }

    fun shouldTransitionToAir(state: RunUpToFlightCompositeState, entity: PokemonEntity, driver: Player, input: Vec3): Boolean {
        if (state.activeController != landController.key) return false
        if (state.timeTransitioned + 20 >= entity.level().gameTime) return false
        return driver.isSprinting && input.z > 0.5
    }

    //TODO: This composite controller needs to have the jet lift off at a certain speed.
    //maybe just pressing shift or space? Unsure, will need to test to see what works best.
    override fun tick(entity: PokemonEntity, driver: Player, input: Vec3) {
        val flightState = flightController.state
        val groundState = landController.state
        if (shouldTransitionToGround(state, entity, driver)) {
            groundState.currSpeed = flightState.currSpeed
            groundState.stamina = flightState.stamina
            groundState.rideVel = flightState.rideVel

            state.activeController = landController.key
            entity.setBehaviourFlag(PokemonBehaviourFlag.FLYING, false)
            state.timeTransitioned = entity.level().gameTime
        }
        else if (shouldTransitionToAir(state, entity, driver, input)) {
            flightState.currSpeed = groundState.currSpeed
            flightState.stamina = groundState.stamina
            flightState.rideVel = groundState.rideVel

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
        else {
            val controller = if (entity.getCurrentPoseType() in PoseType.FLYING_POSES) {
                entity.setBehaviourFlag(PokemonBehaviourFlag.FLYING, true)
                flightController
            } else {
                entity.setBehaviourFlag(PokemonBehaviourFlag.FLYING, false)
                landController
            }
            state.activeController = controller.key
            state.timeTransitioned = entity.level().gameTime
            return controller
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

    override fun canJump(entity: PokemonEntity, driver: Player): Boolean {
        return getActiveController(entity).canJump(entity, driver)
    }

    override fun setRideBar(entity: PokemonEntity, driver: Player): Float {
        return getActiveController(entity).setRideBar(entity, driver)
    }

    override fun rideFovMult(entity: PokemonEntity, driver: Player): Float {
        return getActiveController(entity).rideFovMult(entity, driver)
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

    override fun rotationOnMouseXY(
        entity: PokemonEntity,
        driver: Player,
        yMouse: Double,
        xMouse: Double,
        yMouseSmoother: SmoothDouble,
        xMouseSmoother: SmoothDouble,
        sensitivity: Double,
        deltaTime: Double
    ): Vec3 {
        return getActiveController(entity).rotationOnMouseXY(
            entity,
            driver,
            yMouse,
            xMouse,
            yMouseSmoother,
            xMouseSmoother,
            sensitivity,
            deltaTime
        ) }

    override fun dismountOnShift(entity: PokemonEntity): Boolean = getActiveController(entity).dismountOnShift(entity)

    override fun encode(buffer: RegistryFriendlyByteBuf) {
        super.encode(buffer)
        buffer.writeString(minimumSpeed.getString())
        buffer.writeString(minimumJump.getString())
        state.encode(buffer)
        landController.encode(buffer)
        flightController.encode(buffer)
    }

    override fun decode(buffer: RegistryFriendlyByteBuf) {
        minimumSpeed = buffer.readString().asExpression()
        minimumJump = buffer.readString().asExpression()
        state.decode(buffer)
        landController = buffer.readResourceLocation().let { key ->
            val controller = RideControllerAdapter.types[key]?.getConstructor()?.newInstance() ?: error("Unknown controller key: $key")
            controller.decode(buffer)
            controller as GenericLandController
        }
        flightController = buffer.readResourceLocation().let { key ->
            val controller = RideControllerAdapter.types[key]?.getConstructor()?.newInstance() ?: error("Unknown controller key: $key")
            controller.decode(buffer)
            controller as JetAirController
        }
    }

    override fun copy(): RideController {
        val controller = RunUpToJetFlightCompositeController()
        controller.minimumSpeed = minimumSpeed
        controller.minimumJump = minimumJump
        controller.state.activeController = state.activeController
        controller.state.timeTransitioned = state.timeTransitioned
        controller.landController = landController.copy()
        controller.flightController = flightController.copy()
        return controller
    }
}
