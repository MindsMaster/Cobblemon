/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.ai.config.task

import com.cobblemon.mod.common.api.ai.BehaviourConfigurationContext
import com.cobblemon.mod.common.api.ai.ExpressionOrEntityVariable
import com.cobblemon.mod.common.api.ai.WrapperLivingEntityTask
import com.cobblemon.mod.common.api.ai.asVariables
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.entity.pokemon.ai.tasks.FindRestingPlaceTask
import com.cobblemon.mod.common.util.asExpression
import com.mojang.datafixers.util.Either
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.behavior.BehaviorControl

class FindRestingPlaceTaskConfig : SingleTaskConfig {
    val horizontalSearchDistance: ExpressionOrEntityVariable = Either.left("16".asExpression())
    val verticalSearchDistance: ExpressionOrEntityVariable = Either.left("5".asExpression())

    override fun getVariables(entity: LivingEntity) = listOf(horizontalSearchDistance, verticalSearchDistance).asVariables()
    override fun createTask(
        entity: LivingEntity,
        behaviourConfigurationContext: BehaviourConfigurationContext
    ): BehaviorControl<in LivingEntity>? {
        return WrapperLivingEntityTask(
            FindRestingPlaceTask.create(horizontalSearchDistance.resolveInt(), verticalSearchDistance.resolveInt()),
            PokemonEntity::class.java
        )
    }
}