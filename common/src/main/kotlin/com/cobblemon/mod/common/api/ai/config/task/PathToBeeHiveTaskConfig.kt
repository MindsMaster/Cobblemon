/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.ai.config.task

import com.cobblemon.mod.common.api.ai.BehaviourConfigurationContext
import com.cobblemon.mod.common.api.ai.WrapperLivingEntityTask
import com.cobblemon.mod.common.api.ai.asVariables
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.entity.pokemon.ai.tasks.PathToBeeHiveTask
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.behavior.BehaviorControl

class PathToBeeHiveTaskConfig : SingleTaskConfig {
    companion object {
        const val HONEY = "honey"
    }

    val condition = booleanVariable(HONEY, "can_add_honey", true).asExpressible()
    val speedMultiplier = numberVariable(HONEY, "speed_multiplier", 0.6).asExpressible()

    override fun getVariables(entity: LivingEntity) = listOf(
            condition,
            speedMultiplier
    ).asVariables()

    override fun createTask(
            entity: LivingEntity,
            brainConfigurationContext: BehaviourConfigurationContext
    ): BehaviorControl<in LivingEntity>? {
        return WrapperLivingEntityTask(PathToBeeHiveTask.create(), PokemonEntity::class.java)
    }
}