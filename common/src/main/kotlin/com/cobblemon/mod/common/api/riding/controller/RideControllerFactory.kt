package com.cobblemon.mod.common.api.riding.controller

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import net.minecraft.resources.ResourceLocation

object RideControllerFactory {
    val factories = mutableMapOf<ResourceLocation, (entity: PokemonEntity) -> RideController>()

    fun register(key: ResourceLocation, factory: (entity: PokemonEntity) -> RideController) {
        factories[key] = factory
    }

    fun <T> create(key: ResourceLocation, entity: PokemonEntity): T {
        return factories[key]?.invoke(entity) as T ?: error("Did not recognize ride controller $key")
    }
}
