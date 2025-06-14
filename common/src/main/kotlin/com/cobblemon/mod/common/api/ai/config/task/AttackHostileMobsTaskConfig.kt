package com.cobblemon.mod.common.entity.ai

import com.cobblemon.mod.common.api.ai.BehaviourConfigurationContext
import com.cobblemon.mod.common.api.ai.config.task.TaskConfig
import com.cobblemon.mod.common.api.npc.configuration.MoLangConfigVariable
import com.cobblemon.mod.common.util.withQueryValue
import com.google.gson.annotations.Expose
import com.mojang.serialization.Codec
import net.minecraft.data.models.blockstates.Condition.condition
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.behavior.BehaviorControl

class AttackHostileMobsTaskConfig : TaskConfig {
    override fun getVariables(entity: LivingEntity): List<MoLangConfigVariable> {
        TODO("Not yet implemented")
    }

    override fun createTasks(
        entity: LivingEntity,
        behaviourConfigurationContext: BehaviourConfigurationContext
    ): List<BehaviorControl<in LivingEntity>> {
        runtime.withQueryValue("entity", entity.asMostSpecificMoLangValue())
        if (!condition.resolveBoolean()) return null
        return AttackHostileMobsTask.create()
    }

    companion object {
        val CODEC: Codec<AttackHostileMobsTaskConfig> = Codec.unit(AttackHostileMobsTaskConfig())
    }
}