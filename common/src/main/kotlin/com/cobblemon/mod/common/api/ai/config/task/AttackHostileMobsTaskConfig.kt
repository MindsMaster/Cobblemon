package com.cobblemon.mod.common.entity.ai

import com.cobblemon.mod.common.api.ai.BehaviourConfigurationContext
import com.cobblemon.mod.common.api.ai.WrapperLivingEntityTask
import com.cobblemon.mod.common.api.ai.config.task.SingleTaskConfig
import com.cobblemon.mod.common.api.ai.config.task.TaskConfig
import com.cobblemon.mod.common.api.npc.configuration.MoLangConfigVariable
import com.cobblemon.mod.common.util.withQueryValue
import com.google.gson.annotations.Expose
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.mojang.serialization.Codec
import net.minecraft.data.models.blockstates.Condition.condition
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.behavior.BehaviorControl

class AttackHostileMobsTaskConfig : SingleTaskConfig {
    override fun getVariables(entity: LivingEntity): List<MoLangConfigVariable> {
        return emptyList<MoLangConfigVariable>()
    }

    override fun createTask(
        entity: LivingEntity,
        brainConfigurationContext: BehaviourConfigurationContext
    ): BehaviorControl<in LivingEntity>? {
        return AttackHostileMobsTask.create()
    }
    
    companion object {
        val CODEC: Codec<AttackHostileMobsTaskConfig> = Codec.unit(AttackHostileMobsTaskConfig())
    }
}