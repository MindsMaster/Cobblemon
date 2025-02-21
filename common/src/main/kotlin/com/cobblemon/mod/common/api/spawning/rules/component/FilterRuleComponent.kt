/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.spawning.rules.component

import com.bedrockk.molang.Expression
import com.bedrockk.molang.ast.BooleanExpression
import com.bedrockk.molang.runtime.MoLangRuntime
import com.cobblemon.mod.common.api.molang.MoLangFunctions.setup
import com.cobblemon.mod.common.api.spawning.context.SpawningContext
import com.cobblemon.mod.common.api.spawning.detail.SpawnDetail
import com.cobblemon.mod.common.api.spawning.rules.selector.AllSpawnDetailSelector
import com.cobblemon.mod.common.api.spawning.rules.selector.AllSpawningContextSelector
import com.cobblemon.mod.common.api.spawning.rules.selector.SpawnDetailSelector
import com.cobblemon.mod.common.api.spawning.rules.selector.SpawningContextSelector
import com.cobblemon.mod.common.util.resolveBoolean

/**
 * A rule component that targets specific spawns and contexts and judges whether it
 * is allowed to spawn. This runs prior to the regular SpawnDetail conditions.
 *
 * @author Hiroku
 * @since October 1st, 2023
 */
class FilterRuleComponent : SpawnRuleComponent {
    @Transient
    val runtime = MoLangRuntime().setup()

    val spawnSelector: SpawnDetailSelector = AllSpawnDetailSelector
    val contextSelector: SpawningContextSelector = AllSpawningContextSelector
    val allow: Expression = BooleanExpression(true)

    override fun affectSpawnable(detail: SpawnDetail, ctx: SpawningContext): Boolean {
        return if (spawnSelector.selects(detail) && contextSelector.selects(ctx)) {
            runtime.environment.setSimpleVariable("spawn", detail.struct)
            runtime.environment.setSimpleVariable("context", ctx.getOrSetupStruct())
            runtime.resolveBoolean(allow)
        } else {
            true
        }
    }
}