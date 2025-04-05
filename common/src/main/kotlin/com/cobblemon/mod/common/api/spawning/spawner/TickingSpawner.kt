/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.spawning.spawner

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.spawning.SpawnBucket
import com.cobblemon.mod.common.api.spawning.SpawnBucketUtils
import com.cobblemon.mod.common.api.spawning.SpawnCause
import com.cobblemon.mod.common.api.spawning.SpawnerManager
import com.cobblemon.mod.common.api.spawning.context.AreaSpawningContext
import com.cobblemon.mod.common.api.spawning.context.SpawningContext
import com.cobblemon.mod.common.api.spawning.context.SubmergedSpawningContext
import com.cobblemon.mod.common.api.spawning.detail.EntitySpawnResult
import com.cobblemon.mod.common.api.spawning.detail.SpawnAction
import com.cobblemon.mod.common.api.spawning.detail.SpawnDetail
import com.cobblemon.mod.common.api.spawning.detail.SpawnPool
import com.cobblemon.mod.common.api.spawning.influence.SpawnBaitInfluence
import com.cobblemon.mod.common.api.spawning.influence.SpawningInfluence
import com.cobblemon.mod.common.api.spawning.selection.FlatContextWeightedSelector
import com.cobblemon.mod.common.api.spawning.selection.SpawningSelector
import com.cobblemon.mod.common.block.entity.CakeBlockEntity
import com.cobblemon.mod.common.block.entity.LureCakeBlockEntity
import net.minecraft.world.entity.Entity

/**
 * A spawner that regularly attempts spawning entities. It has timing utilities,
 * and subclasses must provide the logic for generating a spawn which is called
 * periodically by the server.
 *
 * @author Hiroku
 * @since February 5th, 2022
 */
abstract class TickingSpawner(
    override val name: String,
    var spawns: SpawnPool,
    val manager: SpawnerManager
) : Spawner {
    private var selector: SpawningSelector = FlatContextWeightedSelector()
    override val influences = mutableListOf<SpawningInfluence>()

    override fun canSpawn() = active
    override fun getSpawningSelector() = selector
    override fun setSpawningSelector(selector: SpawningSelector) { this.selector = selector }
    override fun getSpawnPool() = spawns
    override fun setSpawnPool(spawnPool: SpawnPool) { spawns = spawnPool }

    abstract fun run(cause: SpawnCause): Pair<SpawningContext, SpawnDetail>?

    var active = true
    val spawnedEntities = mutableListOf<Entity>()

    var lastSpawnTime = 0L
    var ticksUntilNextSpawn = 100F
    abstract var ticksBetweenSpawns: Float
    var tickTimerMultiplier = 1F

    var removalCheckTicks = 0

    open fun tick() {
        removalCheckTicks++
        influences.removeIf { it.isExpired() }
        if (removalCheckTicks == 60) {
            spawnedEntities.removeIf { it.isRemoved }
            removalCheckTicks = 0
        }

        if (!active) {
            return
        }

        ticksUntilNextSpawn -= tickTimerMultiplier
        if (ticksUntilNextSpawn <= 0) {

            val defaultBucket = chooseBucket()
            val defaultCause = SpawnCause(spawner = this, bucket = defaultBucket, entity = getCauseEntity())

            // Try getting spawn with a dummy cause first
            val preSpawn = run(defaultCause)

            if (preSpawn != null) {
                val (ctx, detail) = preSpawn
                val influence = ctx.influences.filterIsInstance<SpawnBaitInfluence>().firstOrNull()

                val bucket = if (influence != null && influence.used) {
                    // todo Get bucket effect from bait influence and use it in the new method
                    val baitRarityLevel = 3 // I am using this for testing atm

                    SpawnBucketUtils.chooseAdjustedSpawnBucket(Cobblemon.bestSpawner.config.buckets, baitRarityLevel)
                } else {
                    defaultBucket // todo Maybe end it quicker if it is this
                }

                // todo maybe only do this stuff if there is a rarity bucket bait effect present in the influence
                // Rerun with the new bucket
                val cause = SpawnCause(spawner = this, bucket = bucket, entity = getCauseEntity())
                val finalSpawn = run(cause) ?: return
                val (finalCtx, finalDetail) = finalSpawn

                val spawnAction = finalDetail.doSpawn(ctx = finalCtx)

                if (finalCtx is AreaSpawningContext && influence != null && influence.used) {
                    val baitPos = influence.baitPos
                    val level = finalCtx.world.level
                    val blockEntity = baitPos?.let { level.getBlockEntity(it) }

                    if (blockEntity is LureCakeBlockEntity) {
                        blockEntity.bites++

                        if (blockEntity.bites >= CakeBlockEntity.MAX_NUMBER_OF_BITES) {
                            level.removeBlock(baitPos, false)
                        } else {
                            blockEntity.setChanged()
                            level.sendBlockUpdated(baitPos, blockEntity.blockState, blockEntity.blockState, 3)
                        }
                    }
                }

                spawnAction.complete()

                finalCtx.influences.filterIsInstance<SpawnBaitInfluence>().forEach {
                    it.used = false
                }
            }

/*



            val spawn = run(SpawnCause(spawner = this, bucket = chooseBucket(), entity = getCauseEntity()))
            ticksUntilNextSpawn = ticksBetweenSpawns
            if (spawn != null) {
                val ctx = spawn.first
                val detail = spawn.second
                val spawnAction = detail.doSpawn(ctx = ctx)
                if (ctx is AreaSpawningContext) {
                    val influence = ctx.influences.filter { it is SpawnBaitInfluence }.firstOrNull()

                    // this is where we try to grab a possible SpawnBaitInfluence
                    if (influence is SpawnBaitInfluence && influence.used) {
                        val baitPos = influence.baitPos
                        val level = ctx.world.level

                        val blockEntity = baitPos?.let { level.getBlockEntity(it) }

                        if (blockEntity is LureCakeBlockEntity) {
                            blockEntity.bites++

                            if (blockEntity.bites >= CakeBlockEntity.MAX_NUMBER_OF_BITES) {
                                level.removeBlock(baitPos, false)
                            } else {
                                blockEntity.setChanged()
                                level.sendBlockUpdated(baitPos, blockEntity.blockState, blockEntity.blockState, 3)
                            }
                        }
                    }
                }
                spawnAction.complete()

                // TODO reset the Influence (Not sure if we need to do this, but in case it persists by next time I am adding this)
                ctx.influences.filterIsInstance<SpawnBaitInfluence>().forEach {
                    it.used = false
                }
            }*/
        }
    }

    override fun <R> afterSpawn(action: SpawnAction<R>, result: R) {
        super.afterSpawn(action, result)
        if (result is EntitySpawnResult) {
            spawnedEntities.addAll(result.entities)
        }
        lastSpawnTime = System.currentTimeMillis()
    }

    open fun getCauseEntity(): Entity? = null

    fun getAllInfluences() = this.influences + manager.influences

    override fun copyInfluences() = this.getAllInfluences().toMutableList()
}
