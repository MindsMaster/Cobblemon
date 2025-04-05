package com.cobblemon.mod.common.api.fishing

object SpawnBaitUtils {
    fun mergeEffects(effects: List<SpawnBait.Effect>): List<SpawnBait.Effect> {
        val grouped = effects.groupBy { Pair(it.type, it.subcategory) }

        return grouped.map { (key, groupEffects) ->
            val count = groupEffects.size
            val totalChance = groupEffects.sumOf { it.chance }
            val totalValue = groupEffects.sumOf { it.value }

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