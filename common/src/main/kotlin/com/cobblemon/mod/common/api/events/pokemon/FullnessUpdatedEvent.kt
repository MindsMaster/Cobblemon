package com.cobblemon.mod.common.api.events.pokemon

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.pokemon.Pokemon

/**
 * Event that is fired when a player owned Pokémon has its fullness changed
 */
data class FullnessUpdatedEvent(
        val pokemon: Pokemon,
        val newFullnessInitial: Int
) {
    var newFullness: Int = newFullnessInitial
        set(value) {
            field = value.coerceIn(0, pokemon.getMaxFullness())
        }
}