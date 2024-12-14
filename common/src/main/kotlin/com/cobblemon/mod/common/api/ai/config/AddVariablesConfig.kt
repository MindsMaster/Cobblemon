/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.ai.config

import com.cobblemon.mod.common.api.ai.BrainConfigurationContext
import com.cobblemon.mod.common.api.npc.configuration.MoLangConfigVariable
import com.cobblemon.mod.common.entity.MoLangScriptingEntity
import net.minecraft.world.entity.LivingEntity

class AddVariablesConfig : BrainConfig {
    var variables = mutableListOf<MoLangConfigVariable>()

    override fun configure(entity: LivingEntity, brainConfigurationContext: BrainConfigurationContext) {
        if (entity is MoLangScriptingEntity) {
            variables.forEach { variable ->
                if (entity.registeredVariables.none { it.variableName == variable.variableName }) {
                    entity.registeredVariables.add(variable)
                }
            }
        }
    }
}