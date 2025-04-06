package com.cobblemon.mod.common.api.riding.behaviour.types.composite.strategies

import com.bedrockk.molang.Expression
import com.cobblemon.mod.common.api.riding.RidingStyle
import com.cobblemon.mod.common.api.riding.behaviour.RidingBehaviourSettings
import com.cobblemon.mod.common.api.riding.behaviour.RidingBehaviourState
import com.cobblemon.mod.common.api.riding.behaviour.RidingBehaviours
import com.cobblemon.mod.common.api.riding.behaviour.types.composite.CompositeSettings
import com.cobblemon.mod.common.api.riding.behaviour.types.composite.CompositeState
import com.cobblemon.mod.common.entity.pokemon.PokemonBehaviourFlag
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.util.asExpression
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.readExpression
import com.cobblemon.mod.common.util.writeExpression
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.world.entity.player.Player
import net.minecraft.world.phys.Vec3

object FallStrategy : CompositeRidingStrategy<FallCompositeSettings> {

    override val key = cobblemonResource("strategy/fall")

    override fun tick(
        settings: FallCompositeSettings,
        state: CompositeState,
        defaultState: RidingBehaviourState,
        alternativeState: RidingBehaviourState,
        vehicle: PokemonEntity,
        driver: Player,
        input: Vec3
    ) {
        if (shouldTransitionToDefault(state, settings.defaultBehaviour, vehicle)) {
            defaultState.stamina.set(alternativeState.stamina.get())
            defaultState.rideVelocity.set(alternativeState.rideVelocity.get())
            val defaultBehaviour = RidingBehaviours.get(settings.defaultBehaviour.key)
            vehicle.setBehaviourFlag(PokemonBehaviourFlag.FLYING, defaultBehaviour.style == RidingStyle.AIR)
        } else if (shouldTransitionToAlternative(state, settings.alternativeBehaviour, vehicle, driver)) {
            alternativeState.stamina.set(defaultState.stamina.get())
            alternativeState.rideVelocity.set(defaultState.rideVelocity.get())
            val alternativeBehaviour = RidingBehaviours.get(settings.alternativeBehaviour.key)
            vehicle.setBehaviourFlag(PokemonBehaviourFlag.FLYING, alternativeBehaviour.style == RidingStyle.AIR)
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

class FallCompositeSettings : CompositeSettings() {
    override val key = FallStrategy.key

    var minimumForwardSpeed: Expression = "0.0".asExpression()
        private set

    var minimumFallSpeed: Expression = "0.5".asExpression()
        private set

    override fun encode(buffer: RegistryFriendlyByteBuf) {
        super.encode(buffer)
        buffer.writeExpression(minimumForwardSpeed)
        buffer.writeExpression(minimumFallSpeed)
    }

    override fun decode(buffer: RegistryFriendlyByteBuf) {
        super.decode(buffer)
        minimumForwardSpeed = buffer.readExpression()
        minimumFallSpeed = buffer.readExpression()
    }
}
