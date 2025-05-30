package com.cobblemon.mod.common.api.ai.config.task

import com.cobblemon.mod.common.api.ai.BehaviourConfigurationContext
import com.cobblemon.mod.common.api.ai.WrapperLivingEntityTask
import com.cobblemon.mod.common.api.ai.asVariables
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.entity.pokemon.ai.tasks.PlaceHoneyInHiveTask
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.behavior.BehaviorControl

class PlaceHoneyInHiveTaskConfig : SingleTaskConfig {
    companion object {
        const val HONEY = "honey"
    }

    val condition = booleanVariable(HONEY, "can_add_honey", true).asExpressible()

    override fun getVariables(entity: LivingEntity) = listOf(
            condition
    ).asVariables()

    override fun createTask(
            entity: LivingEntity,
            brainConfigurationContext: BehaviourConfigurationContext
    ): BehaviorControl<in LivingEntity>? {
        return WrapperLivingEntityTask(PlaceHoneyInHiveTask.create(), PokemonEntity::class.java)
    }
}