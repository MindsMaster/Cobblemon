/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.ai.config.task

import com.cobblemon.mod.common.api.ai.BrainConfigurationContext
import com.cobblemon.mod.common.api.ai.WrapperLivingEntityTask
import com.cobblemon.mod.common.api.molang.MoLangFunctions.asMostSpecificMoLangValue
import com.cobblemon.mod.common.api.npc.configuration.MoLangConfigVariable
import com.cobblemon.mod.common.util.asExpression
import com.cobblemon.mod.common.util.lang
import com.cobblemon.mod.common.util.resolveBoolean
import com.cobblemon.mod.common.util.resolveFloat
import com.cobblemon.mod.common.util.resolveInt
import com.cobblemon.mod.common.util.withQueryValue
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.PathfinderMob
import net.minecraft.world.entity.ai.behavior.BehaviorControl
import net.minecraft.world.entity.ai.behavior.SetWalkTargetAwayFrom
import net.minecraft.world.entity.ai.memory.MemoryModuleType

class FleeNearestHostileTaskConfig : SingleTaskConfig {
    companion object {
        const val FLEE_NEAREST_HOSTILE = "flee_nearest_hostile"
        const val FLEE_SPEED_MULTIPLIER = "flee_speed_multiplier"
        const val FLEE_DESIRED_DISTANCE = "flee_desired_distance"
    }

    var condition = true
    var speedMultiplier = 0.5
    var desiredDistance = 9

    override val variables: List<MoLangConfigVariable>
        get() = listOf(
            MoLangConfigVariable(
                variableName = FLEE_NEAREST_HOSTILE,
                type = MoLangConfigVariable.MoLangVariableType.BOOLEAN,
                displayName = lang("entity.variable.flee_nearest_hostile.name"),
                description = lang("entity.variable.flee_nearest_hostile.desc"),
                defaultValue = condition.toString()
            ),
            MoLangConfigVariable(
                variableName = FLEE_SPEED_MULTIPLIER,
                type = MoLangConfigVariable.MoLangVariableType.NUMBER,
                displayName = lang("entity.variable.flee_speed_multiplier.name"),
                description = lang("entity.variable.flee_speed_multiplier.desc"),
                defaultValue = speedMultiplier.toString()
            ),
            MoLangConfigVariable(
                variableName = FLEE_DESIRED_DISTANCE,
                type = MoLangConfigVariable.MoLangVariableType.NUMBER,
                displayName = lang("entity.variable.flee_desired_distance.name"),
                description = lang("entity.variable.flee_desired_distance.desc"),
                defaultValue = desiredDistance.toString()
            )
        )

    override fun createTask(
        entity: LivingEntity,
        brainConfigurationContext: BrainConfigurationContext
    ): BehaviorControl<in LivingEntity>? {
        runtime.withQueryValue("entity", entity.asMostSpecificMoLangValue())
        if (!runtime.resolveBoolean("q.entity.config.$FLEE_NEAREST_HOSTILE".asExpression()) || entity !is PathfinderMob) return null
        val speedMultiplier = runtime.resolveFloat("q.entity.config.$FLEE_SPEED_MULTIPLIER".asExpression())
        val desiredDistance = runtime.resolveInt("q.entity.config.$FLEE_DESIRED_DISTANCE".asExpression())
        return WrapperLivingEntityTask(
            SetWalkTargetAwayFrom.entity(MemoryModuleType.NEAREST_HOSTILE, speedMultiplier, desiredDistance, false),
            PathfinderMob::class.java
        )
    }
}