/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.entity.pokemon.ai.tasks

import com.cobblemon.mod.common.CobblemonMemories
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.PathfinderMob
import net.minecraft.world.entity.ai.behavior.BlockPosTracker
import net.minecraft.world.entity.ai.behavior.OneShot
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder
import net.minecraft.world.entity.ai.behavior.declarative.Trigger
import net.minecraft.world.entity.ai.memory.MemoryModuleType
import net.minecraft.world.entity.ai.memory.WalkTarget
import net.minecraft.world.phys.Vec3

object PathToFlowerTask {
    fun create(): OneShot<in LivingEntity> {
        return BehaviorBuilder.create {
            it.group(
                it.registered(MemoryModuleType.LOOK_TARGET),
                it.absent(MemoryModuleType.WALK_TARGET),
                it.present(CobblemonMemories.NEARBY_FLOWERS),
                it.absent(CobblemonMemories.POLLINATED),
                it.absent(CobblemonMemories.HIVE_COOLDOWN)
            ).apply(it) { lookTarget, walkTarget, flowerMemory, pollinated, hiveCooldown ->
                Trigger { world, entity, time ->
                    if (entity !is PathfinderMob || !entity.isAlive) return@Trigger false

                    val flowerLocations = it.get(flowerMemory).map(Vec3::atCenterOf)
                    if (flowerLocations.isEmpty()) {
                        return@Trigger false
                    }
                    val sortedByDistance = flowerLocations.sortedBy { it.distanceTo(entity.position()) }
                    val closestHalf = sortedByDistance.subList(0, flowerLocations.size / 2 + 1)
                    if (closestHalf.isEmpty()) {
                        return@Trigger false
                    }
                    val targetVec = closestHalf.random()

                    // if we're really close to one then forget it, pollination is gonna occur
                    if (flowerLocations.any { it.distanceToSqr(entity.x, entity.y, entity.z) <= 1.0 }) {
                        return@Trigger false
                    }

                    walkTarget.set(WalkTarget(targetVec, 0.3F, 0))
                    lookTarget.set(BlockPosTracker(targetVec.add(0.0, entity.eyeHeight.toDouble(), 0.0)))

                    return@Trigger true
                }
            }
        }
    }
}