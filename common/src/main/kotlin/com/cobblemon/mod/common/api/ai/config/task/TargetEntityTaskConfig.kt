/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.ai.config.task

import com.bedrockk.molang.Expression
import com.cobblemon.mod.common.CobblemonMemories
import com.cobblemon.mod.common.api.ai.BehaviourConfigurationContext
import com.cobblemon.mod.common.api.ai.CobblemonAttackTargetData
import com.cobblemon.mod.common.api.ai.ExpressionOrEntityVariable
import com.cobblemon.mod.common.api.molang.MoLangFunctions.asMostSpecificMoLangValue
import com.cobblemon.mod.common.api.npc.configuration.MoLangConfigVariable
import com.cobblemon.mod.common.util.asExpression
import com.cobblemon.mod.common.util.resolveBoolean
import com.cobblemon.mod.common.util.withQueryValue
import com.mojang.datafixers.util.Either
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.behavior.BehaviorControl
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder
import net.minecraft.world.entity.ai.behavior.declarative.Trigger
import net.minecraft.world.entity.ai.memory.MemoryModuleType
import net.minecraft.world.entity.ai.memory.MemoryStatus

/**
 * Directly sets the attack target of the entity to the nearest visible entity that matches the [entityCondition]
 * and is within the [range].
 *
 * @author Hiroku
 * @since June 23rd, 2025
 */
class TargetEntityTaskConfig : SingleTaskConfig {
    val entityCondition: Expression = "true".asExpression()
    val range: ExpressionOrEntityVariable = Either.left("24".asExpression())

    override fun getVariables(entity: LivingEntity) = emptyList<MoLangConfigVariable>()
    override fun createTask(
        entity: LivingEntity,
        behaviourConfigurationContext: BehaviourConfigurationContext
    ): BehaviorControl<in LivingEntity>? {
        val range = range.resolveFloat()
        return BehaviorBuilder.create { instance ->
            instance.group(
                instance.present(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES),
                instance.registered(MemoryModuleType.ATTACK_TARGET)
            ).apply(instance) { entities, attackTarget ->
                Trigger { world, entity, _ ->
                    val target = instance.get(entities).findClosest {
                        runtime.withQueryValue("entity", it.asMostSpecificMoLangValue())
                        return@findClosest runtime.resolveBoolean(entityCondition)
                    }.orElse(null)

                    if (target != null && target.distanceTo(entity) <= range) {
                        if (entity.brain.checkMemory(CobblemonMemories.ATTACK_TARGET_DATA, MemoryStatus.REGISTERED)) {
                            val attackTargetData = CobblemonAttackTargetData(
                                shouldContinue = entityCondition
                            )
                            entity.brain.setMemory(CobblemonMemories.ATTACK_TARGET_DATA, attackTargetData)
                        }
                        attackTarget.set(target)
                    }
                    return@Trigger true
                }
            }
        }
    }
}
