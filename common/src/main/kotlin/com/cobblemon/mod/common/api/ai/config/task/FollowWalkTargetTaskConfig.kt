/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.ai.config.task

import com.bedrockk.molang.Expression
import com.bedrockk.molang.runtime.MoLangRuntime
import com.bedrockk.molang.runtime.struct.QueryStruct
import com.cobblemon.mod.common.api.ai.BrainConfigurationContext
import com.cobblemon.mod.common.api.ai.WrapperLivingEntityTask
import com.cobblemon.mod.common.api.molang.ExpressionLike
import com.cobblemon.mod.common.api.molang.MoLangFunctions.asMostSpecificMoLangValue
import com.cobblemon.mod.common.api.npc.configuration.MoLangConfigVariable
import com.cobblemon.mod.common.entity.PosableEntity
import com.cobblemon.mod.common.entity.ai.FollowWalkTargetTask
import com.cobblemon.mod.common.util.asExpression
import com.cobblemon.mod.common.util.asExpressionLike
import com.cobblemon.mod.common.util.lang
import com.cobblemon.mod.common.util.resolveBoolean
import com.cobblemon.mod.common.util.resolveInt
import com.cobblemon.mod.common.util.withQueryValue
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.PathfinderMob
import net.minecraft.world.entity.ai.behavior.BehaviorControl

class FollowWalkTargetTaskConfig : SingleTaskConfig {
    companion object {
        const val FOLLOW_WALK_TARGET = "follow_walk_target"
    }

    val condition = true
    val minRunTicks: Expression = "150".asExpression()
    val maxRunTicks: Expression = "250".asExpression()

    override val variables = listOf<MoLangConfigVariable>(
        MoLangConfigVariable(
            variableName = FOLLOW_WALK_TARGET,
            type = MoLangConfigVariable.MoLangVariableType.BOOLEAN,
            displayName = lang("entity.variable.follow_walk_target.name"),
            description = lang("entity.variable.follow_walk_target.desc"),
            defaultValue = condition.toString()
        )
    )

    override fun createTask(entity: LivingEntity, brainConfigurationContext: BrainConfigurationContext): BehaviorControl<LivingEntity>? {
        runtime.withQueryValue("entity", entity.asMostSpecificMoLangValue())
        if (!runtime.resolveBoolean("q.entity.variable.$FOLLOW_WALK_TARGET".asExpression())) return null
        return WrapperLivingEntityTask(
            FollowWalkTargetTask(runtime.resolveInt(minRunTicks), runtime.resolveInt(maxRunTicks)),
            PathfinderMob::class.java
        )
    }
}