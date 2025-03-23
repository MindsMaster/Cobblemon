/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.ai.config.task

import com.cobblemon.mod.common.CobblemonMemories
import com.cobblemon.mod.common.api.ai.BrainConfigurationContext
import com.cobblemon.mod.common.api.ai.asVariables
import com.cobblemon.mod.common.api.molang.MoLangFunctions.asMostSpecificMoLangValue
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.util.canFit
import com.cobblemon.mod.common.util.toVec3d
import com.cobblemon.mod.common.util.withQueryValue
import net.minecraft.core.BlockPos
import net.minecraft.tags.BlockTags
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.PathfinderMob
import net.minecraft.world.entity.ai.behavior.BehaviorControl
import net.minecraft.world.entity.ai.behavior.BlockPosTracker
import net.minecraft.world.entity.ai.behavior.RandomStroll
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder
import net.minecraft.world.entity.ai.behavior.declarative.Trigger
import net.minecraft.world.entity.ai.memory.MemoryModuleType
import net.minecraft.world.entity.ai.memory.WalkTarget
import net.minecraft.world.entity.ai.util.LandRandomPos
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.Vec3

class WanderTaskConfig : SingleTaskConfig {
    companion object {
        const val WANDER = "wander" // Category
    }

    val condition = booleanVariable(WANDER, "wanders", true).asExpressible()
    val wanderChance = numberVariable(WANDER, "wander_chance", 1/(20 * 6F)).asExpressible()
    val horizontalRange = numberVariable(WANDER, "horizontal_wander_range", 10).asExpressible()
    val verticalRange = numberVariable(WANDER, "vertical_wander_range", 5).asExpressible()

    val speedMultiplier = numberVariable(SharedEntityVariables.MOVEMENT_CATEGORY, SharedEntityVariables.WALK_SPEED, 0.35).asExpressible()

    override fun getVariables(entity: LivingEntity) = listOf(
        condition,
        wanderChance,
        horizontalRange,
        verticalRange,
        speedMultiplier
    ).asVariables()

    override fun createTask(
        entity: LivingEntity,
        brainConfigurationContext: BrainConfigurationContext
    ): BehaviorControl<in LivingEntity>? {
        runtime.withQueryValue("entity", entity.asMostSpecificMoLangValue())
        if (!condition.resolveBoolean()) return null

        return BehaviorBuilder.create {
            it.group(
                it.absent(MemoryModuleType.WALK_TARGET),
                it.registered(MemoryModuleType.LOOK_TARGET),
                it.absent(CobblemonMemories.PATH_COOLDOWN)
            ).apply(it) { walkTarget, lookTarget, pathCooldown ->
                Trigger { world, entity, time ->
                    if (entity !is PathfinderMob || entity.isUnderWater) {
                        return@Trigger false
                    }

                    runtime.withQueryValue("entity", entity.asMostSpecificMoLangValue())
                    val wanderChance = wanderChance.resolveFloat()
                    if (wanderChance <= 0 || world.random.nextFloat() > wanderChance) return@Trigger false

                    pathCooldown.setWithExpiry(true, 40L)

                    val targetVec = getLandTarget(entity) ?: return@Trigger true //LandRandomPos.getPos(entity, horizontalRange.resolveInt(), verticalRange.resolveInt()) ?: return@Trigger false
                    walkTarget.set(WalkTarget(targetVec, speedMultiplier.resolveFloat(), 1))
                    lookTarget.set(BlockPosTracker(targetVec.add(0.0, entity.eyeHeight.toDouble(), 0.0)))
                    return@Trigger true
                }
            }
        }
    }

    @Suppress("DEPRECATION", "MemberVisibilityCanBePrivate")
    fun getLandTarget(entity: PathfinderMob): Vec3? {
        val roamDistanceCondition: (BlockPos) -> Boolean = if (entity is PokemonEntity) ({ entity.tethering?.canRoamTo(it) != false }) else ({ true })
        val iterable: Iterable<BlockPos> = BlockPos.randomBetweenClosed(entity.random, 64, entity.blockX - 10, entity.blockY, entity.blockZ - 10, entity.blockX + 10, entity.blockY, entity.blockZ + 10)
        val condition: (BlockState, BlockPos) -> Boolean = { _, pos -> entity.canFit(pos) && roamDistanceCondition(pos) }
        val iterator = iterable.iterator()
        position@
        while (iterator.hasNext()) {
            val pos = iterator.next().mutable()
            var blockState = entity.level().getBlockState(pos)

            val maxSteps = 16
            var steps = 0
            var good = false
            if (!blockState.isSolid && !blockState.liquid()) {
                pos.move(0, -1, 0)
                var previousWasAir = true
                while (steps++ < maxSteps && pos.y > entity.level().minBuildHeight) {
                    if (pos.y <= entity.level().minBuildHeight) {
                        continue@position
                    }
                    blockState = entity.level().getBlockState(pos)
                    if (blockState.isSolid && !blockState.`is`(BlockTags.LEAVES) && previousWasAir) {
                        pos.move(0, 1, 0)
                        blockState = entity.level().getBlockState(pos)
                        good = true
                        break
                    } else {
                        previousWasAir = blockState.isAir
                    }
                    pos.move(0, -1, 0)
                }
            } else {
                var previousWasSolid = blockState.isSolid && !blockState.`is`(BlockTags.LEAVES)
                pos.move(0, 1, 0)
                while (steps++ < maxSteps) {
                    if (pos.y >= entity.level().maxBuildHeight) {
                        continue@position
                    }
                    blockState = entity.level().getBlockState(pos)
                    if (blockState.isAir && previousWasSolid) {
                        good = true
                        break
                    }
                    previousWasSolid = blockState.isSolid && !blockState.`is`(BlockTags.LEAVES)
                    pos.move(0, 1, 0)
                }
            }

            if (good && condition(blockState, pos)) {
                return pos.toVec3d()
            }
        }

        return null
    }
}