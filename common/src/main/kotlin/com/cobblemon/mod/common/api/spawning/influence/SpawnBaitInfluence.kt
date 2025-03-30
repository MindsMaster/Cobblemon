/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.spawning.influence

import com.cobblemon.mod.common.Cobblemon.LOGGER
import com.cobblemon.mod.common.api.fishing.SpawnBait
import com.cobblemon.mod.common.api.fishing.SpawnBait.Effects
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies
import com.cobblemon.mod.common.api.pokemon.egg.EggGroup
import com.cobblemon.mod.common.api.pokemon.stats.Stats
import com.cobblemon.mod.common.api.spawning.context.SpawningContext
import com.cobblemon.mod.common.api.spawning.detail.PokemonSpawnDetail
import com.cobblemon.mod.common.api.spawning.detail.SpawnDetail
import com.cobblemon.mod.common.api.types.ElementalTypes
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import net.minecraft.core.BlockPos
import net.minecraft.world.entity.Entity

/**
 * A [SpawningInfluence] that applies some number of SpawnBait effects.
 *
 * @author Hiroku, Plastered_Crab
 * @since March 18th, 2025
 */
class SpawnBaitInfluence(val effects: List<SpawnBait.Effect>, val baitPos: BlockPos? = null) : SpawningInfluence {
    override fun affectSpawn(entity: Entity) {
        super.affectSpawn(entity)
        if (entity is PokemonEntity) {
            effects.forEach { it ->
                if (Math.random() <= it.chance) {
                    Effects.getEffectFunction(it.type)?.invoke(entity, it)
                }
            }
        }
    }

    // EV related bait effects
    override fun affectWeight(detail: SpawnDetail, ctx: SpawningContext, weight: Float): Float {
        // if bait exists and any effects are related to EV yields
        if (effects.any { it.type == Effects.EV }){
            if (detail is PokemonSpawnDetail) {
                val detailSpecies = detail.pokemon.species?.let { PokemonSpecies.getByName(it) }
                val baitEVStat = effects.firstOrNull { it.type == Effects.EV }?.subcategory?.path?.let { Stats.getStat(it) }

                if (detailSpecies != null && baitEVStat != null) {
                    val evYieldValue = detailSpecies.evYield[baitEVStat]?.toFloat() ?: 0f
                    return when {
                        evYieldValue > 0 -> super.affectWeight(detail, ctx, weight) // use original weight if EV yield is greater than 0
                        else -> super.affectWeight(detail, ctx, 0f) // use spawn weight of 0 if EV yield is 0
                    }
                }
            }
        }
        // if bait exists and any effects are related to Typing
        if (effects.any { it.type == Effects.TYPING }){
            if (detail is PokemonSpawnDetail) {
                val detailSpecies = detail.pokemon.species?.let { PokemonSpecies.getByName(it) }
                val baitEffect = effects.firstOrNull { it.type == Effects.TYPING }
                val baitTypingEffect = baitEffect?.subcategory?.path?.let { ElementalTypes.get(it) }

                if (detailSpecies != null && baitTypingEffect != null) {
                    val isMatchingType = detailSpecies.types.contains(baitTypingEffect)
                    return when {
                        isMatchingType -> super.affectWeight(detail, ctx, weight * baitEffect.value.toFloat()) // multiply weight by multiplier of bait effect if typing is found
                        else -> super.affectWeight(detail, ctx, weight) // use base spawn weight if typing is not same as bait
                    }
                }
            }
        }
        // if bait exists and any effects are related to Egg Groups
        if (effects.any { it.type == Effects.EGG_GROUP }) {
            if (detail is PokemonSpawnDetail) {
                val detailSpecies = detail.pokemon.species?.let { PokemonSpecies.getByName(it) }

                if (detailSpecies != null) {
                    // Collect all the egg group effects
                    val eggGroupEffects = effects.filter { it.type == Effects.EGG_GROUP }

                    // Check if any of the egg group effects match the species' egg groups
                    val matchingEffect = eggGroupEffects.firstOrNull { effect ->
                        val effectEggGroupKey = effect.subcategory?.path ?: return@firstOrNull false
                        val eggGroup = EggGroup.fromIdentifier(effectEggGroupKey)
                        if (eggGroup == null) {
                            LOGGER.warn("Unknown egg group identifier: $effectEggGroupKey")
                            return@firstOrNull false
                        }
                        detailSpecies.eggGroups.contains(eggGroup)
                    }

                    if (matchingEffect != null) {
                        val multiplier = matchingEffect.value
                        return super.affectWeight(detail, ctx, (weight * multiplier).toFloat())
                    }
                }
            }
        }
        return super.affectWeight(detail, ctx, weight)
    }
}