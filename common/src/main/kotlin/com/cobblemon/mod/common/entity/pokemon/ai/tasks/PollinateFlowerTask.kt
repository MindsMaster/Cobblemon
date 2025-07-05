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
import net.minecraft.world.entity.ai.behavior.OneShot
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder
import net.minecraft.world.entity.ai.behavior.declarative.Trigger
import net.minecraft.world.entity.ai.memory.MemoryModuleType
import net.minecraft.world.phys.Vec3

object PollinateFlowerTask {
    fun create(): OneShot<in LivingEntity> {
        return BehaviorBuilder.create {
            it.group(
                it.present(CobblemonMemories.NEARBY_FLOWER),
                it.absent(MemoryModuleType.WALK_TARGET),
                it.absent(CobblemonMemories.POLLINATED),
                it.absent(CobblemonMemories.HIVE_COOLDOWN)
            ).apply(it) { flowerMemory, walkTarget, pollinated, hiveCooldown ->
                Trigger { world, entity, time ->
                    if (entity !is PathfinderMob || !entity.isAlive) return@Trigger false

                    val flowerLocation = it.get(flowerMemory)
                    val targetVec = Vec3.atCenterOf(flowerLocation)

                    // if we are not close to it then end early
                    if (entity.distanceToSqr(targetVec) > 2.0) {
                        return@Trigger false
                    }

                    entity.brain.setMemory(CobblemonMemories.POLLINATED, true)

                    return@Trigger true
                }
            }
        }
    }
}