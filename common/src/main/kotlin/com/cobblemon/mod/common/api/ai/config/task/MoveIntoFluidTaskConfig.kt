/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.ai.config.task

import com.cobblemon.mod.common.CobblemonMemories
import com.cobblemon.mod.common.api.ai.BehaviourConfigurationContext
import com.cobblemon.mod.common.api.ai.ExpressionOrEntityVariable
import com.cobblemon.mod.common.api.ai.asVariables
import com.cobblemon.mod.common.api.molang.MoLangFunctions.asMostSpecificMoLangValue
import com.cobblemon.mod.common.api.npc.configuration.MoLangConfigVariable
import com.cobblemon.mod.common.entity.ai.CobblemonWalkTarget
import com.cobblemon.mod.common.util.asExpression
import com.cobblemon.mod.common.util.closestPosition
import com.cobblemon.mod.common.util.withQueryValue
import com.mojang.datafixers.util.Either
import java.util.function.Predicate
import net.minecraft.core.BlockPos
import net.minecraft.tags.FluidTags
import net.minecraft.util.Mth
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.behavior.BehaviorControl
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder
import net.minecraft.world.entity.ai.behavior.declarative.Trigger
import net.minecraft.world.entity.ai.memory.MemoryModuleType
import net.minecraft.world.level.material.FluidState

/**
 * Used to get entities to move into whatever their preferred fluid is, like fish
 * stranded on land trying to get back into water.
 *
 * @author Hiroku
 * @since June 15th, 2025
 */
class MoveIntoFluidTaskConfig : SingleTaskConfig {
    val condition: ExpressionOrEntityVariable = Either.left("true".asExpression())
    val movesIntoWater: ExpressionOrEntityVariable = Either.left("true".asExpression())
    val movesIntoLava: ExpressionOrEntityVariable = Either.left("false".asExpression())
    val speedModifier: ExpressionOrEntityVariable = Either.left("0.5".asExpression())

    override fun getVariables(entity: LivingEntity): List<MoLangConfigVariable> {
        return listOf(
            movesIntoWater,
            movesIntoLava
        ).asVariables()
    }

    override fun createTask(
        entity: LivingEntity,
        behaviourConfigurationContext: BehaviourConfigurationContext
    ): BehaviorControl<in LivingEntity>? {
        runtime.withQueryValue("entity", entity.asMostSpecificMoLangValue())
        if (!condition.resolveBoolean()) return null
        val movesIntoWater = this@MoveIntoFluidTaskConfig.movesIntoWater.resolveBoolean()
        val movesIntoLava = movesIntoLava.resolveBoolean()
        val speedModifier = speedModifier.resolveFloat()
        return BehaviorBuilder.create {
            it.group(
                it.absent(MemoryModuleType.WALK_TARGET),
                it.absent(CobblemonMemories.PATH_COOLDOWN)
            ).apply(it) { walkTarget, pathCooldown ->
                Trigger { world, entity, _ ->
                    val isInWater = entity.isInWater
                    val isInLava = entity.isInLava
                    // If it's already in a place it wants to be, then we're fine.
                    if (isInWater && movesIntoWater || isInLava && movesIntoLava) {
                        return@Trigger false
                    }

                    pathCooldown.setWithExpiry(true, 20L * 5L) // 5 seconds cooldown
                    val target = findTarget(entity = entity, movesIntoWater = movesIntoWater, movesIntoLava = movesIntoLava)
                    if (target == null) {
                        return@Trigger false
                    }

                    walkTarget.set(
                        CobblemonWalkTarget(
                            pos = target,
                            speedModifier = speedModifier,
                            completionRange = 0
                        )
                    )

                    return@Trigger true
                }
            }
        }


    }

    private fun findTarget(
        entity: LivingEntity,
        movesIntoWater: Boolean,
        movesIntoLava: Boolean
    ): BlockPos? {
        val predicate: Predicate<FluidState> = Predicate { fluid ->
            (movesIntoWater && fluid.`is`(FluidTags.WATER)) || (movesIntoLava && fluid.`is`(FluidTags.LAVA))
        }
        val iterable = BlockPos.betweenClosed(
            Mth.floor(entity.x - 8.0),
            Mth.floor(entity.y - 8.0),
            Mth.floor(entity.z - 8.0),
            Mth.floor(entity.x + 8.0),
            entity.blockY,
            Mth.floor(entity.z + 2.0)
        )

        val box = entity.boundingBox
        val blockPos = entity.closestPosition(iterable) { pos ->
            val maxX = pos.x + Mth.ceil(box.xsize) - 1
            val maxY = pos.y - 1 + Mth.ceil(box.ysize) - 1
            val maxZ = pos.z + Mth.ceil(box.zsize) - 1

            val mutable = BlockPos.MutableBlockPos()
            for (x in pos.x..maxX) {
                for (y in (pos.y - 1)..maxY) {
                    for (z in pos.z..maxZ) {
                        if (!entity.level().isFluidAtPosition(mutable.set(x, y, z), predicate)) {
                            return@closestPosition false
                        }
                    }
                }
            }
            return@closestPosition true
        }

        return blockPos
    }
}