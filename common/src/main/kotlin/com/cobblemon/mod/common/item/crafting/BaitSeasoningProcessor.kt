/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.item.crafting

import com.cobblemon.mod.common.CobblemonItemComponents
import com.cobblemon.mod.common.api.fishing.SpawnBait
import com.cobblemon.mod.common.api.fishing.SpawnBaitEffects
import com.cobblemon.mod.common.item.components.BaitEffectsComponent
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack

object BaitSeasoningProcessor : SeasoningProcessor {
    override val type = "spawn_bait"
    override fun apply(result: ItemStack, seasoning: List<ItemStack>) {
        val baitIdentifiers = mutableSetOf<ResourceLocation>()
        for (seasoningStack in seasoning) {
            baitIdentifiers.addAll(SpawnBaitEffects.getBaitIdentifiersFromItem(seasoningStack.itemHolder))
        }

        val rawEffects = baitIdentifiers
                .mapNotNull { SpawnBaitEffects.getFromIdentifier(it) }
                .flatMap { it.effects }

        val combinedEffects = combineBaitEffects(rawEffects) // TODO something like this would possibly combine similar bait effects before applying it to the stack. But the Component data would have to change to not just be ResourceLocations

        result.set(CobblemonItemComponents.BAIT_EFFECTS, BaitEffectsComponent(baitIdentifiers.toList()))
    }

    fun combineBaitEffects(effects: List<SpawnBait.Effect>): List<SpawnBait.Effect> {
        val grouped = effects.groupBy { Pair(it.type, it.subcategory) }

        return grouped.map { (key, effects) ->
            val count = effects.size
            val totalChance = effects.sumOf { it.chance }
            val totalValue = effects.sumOf { it.value }

            val multiplier = when (count) {
                2 -> 0.8
                3 -> 0.9
                else -> 1.0
            }

            val adjustedChance = (totalChance * multiplier).coerceAtMost(1.0)
            val adjustedValue = totalValue * multiplier

            val (type, subcategory) = key
            SpawnBait.Effect(type, subcategory, adjustedChance, adjustedValue)
        }
    }
}