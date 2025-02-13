/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.block.entity

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.CobblemonBlockEntities
import com.cobblemon.mod.common.api.fishing.FishingBait
import com.cobblemon.mod.common.api.spawning.BestSpawner
import com.cobblemon.mod.common.api.spawning.SpawnBucket
import com.cobblemon.mod.common.api.spawning.bait.BaitSpawnCause
import com.cobblemon.mod.common.api.spawning.detail.EntitySpawnResult
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockState

class LureCakeBlockEntity(
    pos: BlockPos,
    state: BlockState
) : CakeBlockEntity(CobblemonBlockEntities.LURE_CAKE, pos, state) {

    /**
     * Generate a `FishingBait` by combining effects from all `RodBaitComponent` data in the `CookingComponent`.
     */
    fun getBaitFromLureCake(): FishingBait? {
        val component = cookingComponent ?: return null
        val combinedEffects = listOf(
                component.bait1.effects,
                component.bait2.effects,
                component.bait3.effects
        ).flatten()

        return FishingBait(
                item = cobblemonResource("lure_cake"), // Directly specify the lure_cake ResourceLocation
                effects = combinedEffects
        )
    }

    /**
     * Spawns a Pokémon of the given species around the specified position.
     * @param world The world in which to spawn the Pokémon.
     * @param pos The position around which to spawn the Pokémon.
     * @param species The species of the Pokémon to spawn.
     * @param isMale Whether the Pokémon should be male or female.
     * @return The spawned Pokémon entity, or null if spawning failed.
     */
    fun spawnPokemon(
            world: Level,
            pos: BlockPos
    ): PokemonEntity? {
        println("Attempting to spawn Pokémon at $pos in world: ${world.dimension().location()}")
        if (world !is ServerLevel) {
            println("World is not a ServerLevel, aborting spawn.")
            return null
        }

        val spawnPos = pos.offset(world.random.nextInt(-SPAWN_RADIUS.toInt(), SPAWN_RADIUS.toInt()), 0, world.random.nextInt(-SPAWN_RADIUS.toInt(), SPAWN_RADIUS.toInt()))
        println("Spawn position determined as $spawnPos")

        val spawner = BestSpawner.baitSpawner
        val buckets = Cobblemon.bestSpawner.config.buckets
        val chosenBucket = chooseSpawnBucket(buckets)
        println("Chosen spawn bucket: $chosenBucket")

        val spawnCause = BaitSpawnCause(
                spawner = spawner,
                bucket = chosenBucket,
                entity = null,
                baitStack = this.toItemStack(),
                baitEffect = this.getBaitFromLureCake()
        )
        println("Spawn cause created: $spawnCause")

        val result = spawner.run(spawnCause, level as ServerLevel, spawnPos)

        var spawnedPokemon: PokemonEntity? = null
        val resultingSpawn = result?.get()

        if (resultingSpawn is EntitySpawnResult) {
            for (entity in resultingSpawn.entities) {
                // we can query the spawned pokemon here
                spawnedPokemon = (entity as PokemonEntity)

                // CRAB IDEA: todo maybe some grass particles? as if they appeared from the grass or forest?
                return spawnedPokemon
            }
        }


        return null
    }

    fun chooseSpawnBucket(buckets: List<SpawnBucket>): SpawnBucket {
        val baseIncreases = listOf(2.5F, 1.0F, 0.6F)  // Base increases for the first three buckets beyond the first
        val adjustedWeights = buckets.mapIndexed { index, bucket ->
            if (index == 0) {
                // Placeholder, will be recalculated
                0.0F
            } else {
                val increase = if (index < baseIncreases.size) baseIncreases[index] else baseIncreases.last() + (index - baseIncreases.size + 1) * 0.15F
                bucket.weight + increase
            }
        }.toMutableList()

        // Recalculate the first bucket's weight to ensure the total is 100%
        val totalAdjustedWeight = adjustedWeights.sum() - adjustedWeights[0]  // Corrected to ensure the list contains Floats
        adjustedWeights[0] = 100.0F - totalAdjustedWeight + buckets[0].weight

        // Random selection based on adjusted weights
        val weightSum = adjustedWeights.sum()
        val chosenSum = kotlin.random.Random.nextDouble(weightSum.toDouble()).toFloat()  // Ensure usage of Random from kotlin.random package
        var sum = 0.0F
        adjustedWeights.forEachIndexed { index, weight ->
            sum += weight
            if (sum >= chosenSum) {
                return buckets[index]
            }
        }

        return buckets.first()  // Fallback
    }

    companion object {
        private const val RANDOM_SPAWN_CHANCE = 100
        private const val SPAWN_RADIUS = 5.0

        /*val TICKER = BlockEntityTicker<LureCakeBlockEntity> { world, pos, state, blockEntity ->
            println("Ticker running for LureCakeBlockEntity at $pos in world: ${world.dimension().location()}")
            if (world.random.nextInt(RANDOM_SPAWN_CHANCE) == 0) {
                println("Triggering Pokémon spawn")
                blockEntity.spawnPokemon(world, pos)
                world.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS)
            }
        }*/
    }
}