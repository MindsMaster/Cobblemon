package com.cobblemon.mod.common.battles.ai.strongBattleAI

import com.cobblemon.mod.common.api.moves.Move
import com.cobblemon.mod.common.api.pokemon.stats.Stat
import com.cobblemon.mod.common.pokemon.Pokemon

/**
 * AI that tries to choose the best move for the given situations. Based off of the Pokemon Trainer Tournament Simulator Github
 * https://github.com/cRz-Shadows/Pokemon_Trainer_Tournament_Simulator/blob/main/pokemon-showdown/sim/examples/Simulation-test-1.ts#L330
 *
 * @since December 15th 2023
 */

class ActiveTracker {
    var p1Active: TrackerActor = TrackerActor()
    var p2Active: TrackerActor = TrackerActor()

    // Tracker Actor with a Party of Tracker Pokemon in a Party
    data class TrackerActor(
            var party: MutableList<TrackerPokemon> = mutableListOf(),
            var uuid: String? = null,
            var activePokemon: TrackerPokemon = TrackerPokemon(),
            var nRemainingMons: Int = 0
    )

    // Tracker Pokemon within the Party
    data class TrackerPokemon(
        var pokemon: Pokemon? = null,
        var species: String? = null,
        var availableSwitches: List<Pokemon>? = null,
        var currentHp: Int = 0,
        var currentHpPercent: Double = 0.0,
        var boosts: MutableMap<Stat, Int> = mutableMapOf(), // unused for now
        var currentAbiltiy: String? = null,
        var currentTypes: MutableList<String>? = null,
        var stats: Map<Stat, Int> = mapOf(),
        var moves: List<Move> = listOf(),
        var sideConditions: Map<String, Any> = mapOf(),
        var firstTurn: Int = 0,
        var protectCount: Int = 0
    )
}