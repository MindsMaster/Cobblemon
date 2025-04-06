package com.cobblemon.mod.common.api.riding.behaviour.types.composite.strategies

import com.cobblemon.mod.common.api.riding.behaviour.RidingBehaviourState
import com.cobblemon.mod.common.api.riding.behaviour.types.composite.CompositeSettings
import com.cobblemon.mod.common.api.riding.behaviour.types.composite.CompositeState
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.player.Player
import net.minecraft.world.phys.Vec3

interface CompositeRidingStrategy<T : CompositeSettings> {
    val key: ResourceLocation

    fun tick(
        settings: T,
        state: CompositeState,
        defaultState: RidingBehaviourState,
        alternativeState: RidingBehaviourState,
        vehicle: PokemonEntity,
        driver: Player,
        input: Vec3
    )
}
