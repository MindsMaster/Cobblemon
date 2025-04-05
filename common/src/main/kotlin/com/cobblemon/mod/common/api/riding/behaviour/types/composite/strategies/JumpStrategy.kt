package com.cobblemon.mod.common.api.riding.behaviour.types.composite.strategies

import com.cobblemon.mod.common.api.riding.RidingStyle
import com.cobblemon.mod.common.api.riding.behaviour.RidingBehaviourSettings
import com.cobblemon.mod.common.api.riding.behaviour.RidingBehaviours
import com.cobblemon.mod.common.api.riding.behaviour.types.composite.CompositeState
import com.cobblemon.mod.common.api.riding.behaviour.types.composite.RideTransitionStrategyParams
import com.cobblemon.mod.common.entity.pokemon.PokemonBehaviourFlag
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import net.minecraft.world.entity.player.Player

object JumpStrategy {

    fun strategy(params: RideTransitionStrategyParams) {
        if (shouldTransitionToDefault(params.state, params.settings.defaultBehaviour, params.vehicle)) {
            params.defaultState.stamina.set(params.alternativeState.stamina.get())
            params.defaultState.rideVelocity.set(params.alternativeState.rideVelocity.get())
            val defaultBehaviour = RidingBehaviours.get(params.settings.defaultBehaviour.key)
            params.vehicle.setBehaviourFlag(PokemonBehaviourFlag.FLYING, defaultBehaviour.style == RidingStyle.AIR)
        } else if (shouldTransitionToAlternative(params.state, params.settings.alternativeBehaviour, params.vehicle, params.driver)) {
            params.alternativeState.stamina.set(params.defaultState.stamina.get())
            params.alternativeState.rideVelocity.set(params.defaultState.rideVelocity.get())
            val alternativeBehaviour = RidingBehaviours.get(params.settings.alternativeBehaviour.key)
            params.vehicle.setBehaviourFlag(PokemonBehaviourFlag.FLYING, alternativeBehaviour.style == RidingStyle.AIR)
        }
    }

    private fun shouldTransitionToDefault(
        state: CompositeState,
        defaultSettings: RidingBehaviourSettings,
        entity: PokemonEntity
    ): Boolean {
        if (state.activeController.get() == defaultSettings.key) return false
        if (!entity.onGround()) return false
        if (state.lastTransition.get() + 20 >= entity.level().gameTime) return false
        val defaultBehaviour = RidingBehaviours.get(defaultSettings.key)
        return defaultBehaviour.isActive(defaultSettings, state.defaultBehaviourState, entity)
    }

    private fun shouldTransitionToAlternative(
        state: CompositeState,
        alternativeSettings: RidingBehaviourSettings,
        entity: PokemonEntity,
        driver: Player
    ): Boolean {
        if (state.activeController.get() == alternativeSettings.key) return false
        if (!driver.jumping) return false
        if (state.lastTransition.get() + 20 >= entity.level().gameTime) return false
        val alternativeBehaviour = RidingBehaviours.get(alternativeSettings.key)
        return alternativeBehaviour.isActive(alternativeSettings, state.alternativeBehaviourState, entity)
    }

}
