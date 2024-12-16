/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.ai.config.task

import com.bedrockk.molang.Expression
import com.cobblemon.mod.common.api.ai.BrainConfigurationContext
import com.cobblemon.mod.common.api.npc.configuration.MoLangConfigVariable
import com.cobblemon.mod.common.entity.ai.LookInDirectionTask
import com.cobblemon.mod.common.util.asExpression
import com.cobblemon.mod.common.util.asTranslated
import net.minecraft.world.entity.LivingEntity

class LookInDirectionTaskConfig : SingleTaskConfig {
    companion object {
        const val BRAIN_LOOK_LOCKED = "locked_rotation"
        const val BRAIN_LOOK_DIRECTION_YAW = "locked_yaw"
        const val BRAIN_LOOK_DIRECTION_PITCH = "locked_pitch"
    }

    var condition = true
    var yaw: Expression = "0".asExpression()
    var pitch: Expression = "0".asExpression()

    override val variables
        get() = listOf(
            MoLangConfigVariable(
                variableName = BRAIN_LOOK_LOCKED,
                displayName = "entity.variable.locked_rotation.name".asTranslated(),
                description = "entity.variable.locked_rotation.desc".asTranslated(),
                type = MoLangConfigVariable.MoLangVariableType.BOOLEAN,
                defaultValue = condition.toString()
            ),
            MoLangConfigVariable(
                variableName = BRAIN_LOOK_DIRECTION_YAW,
                displayName = "entity.variable.locked_yaw.name".asTranslated(),
                description = "entity.variable.locked_yaw.desc".asTranslated(),
                type = MoLangConfigVariable.MoLangVariableType.NUMBER,
                defaultValue = yaw.originalString
            ),
            MoLangConfigVariable(
                variableName = BRAIN_LOOK_DIRECTION_PITCH,
                displayName = "entity.variable.locked_pitch.name".asTranslated(),
                description = "entity.variable.locked_pitch.desc".asTranslated(),
                type = MoLangConfigVariable.MoLangVariableType.NUMBER,
                defaultValue = pitch.originalString
            )
        )

    override fun createTask(
        entity: LivingEntity,
        brainConfigurationContext: BrainConfigurationContext
    ) = LookInDirectionTask(
        shouldLock = "q.entity.config.$BRAIN_LOOK_LOCKED".asExpression(),
        yaw = "q.entity.config.$BRAIN_LOOK_DIRECTION_YAW".asExpression(),
        pitch = "q.entity.config.$BRAIN_LOOK_DIRECTION_PITCH".asExpression()
    )
}