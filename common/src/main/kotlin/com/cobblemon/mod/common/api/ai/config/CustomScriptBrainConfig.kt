package com.cobblemon.mod.common.api.ai.config

import com.bedrockk.molang.runtime.MoLangRuntime
import com.cobblemon.mod.common.api.ai.BrainConfigurationContext
import com.cobblemon.mod.common.api.ai.ExpressionOrEntityVariable
import com.cobblemon.mod.common.api.molang.ExpressionLike
import com.cobblemon.mod.common.api.molang.MoLangFunctions.asMostSpecificMoLangValue
import com.cobblemon.mod.common.api.molang.MoLangFunctions.setup
import com.cobblemon.mod.common.api.npc.configuration.MoLangConfigVariable
import com.cobblemon.mod.common.util.asExpression
import com.cobblemon.mod.common.util.asExpressionLike
import com.cobblemon.mod.common.util.resolve
import com.cobblemon.mod.common.util.withQueryValue
import com.mojang.datafixers.util.Either
import net.minecraft.world.entity.LivingEntity

class CustomScriptBrainConfig : BrainConfig {
    var condition: ExpressionOrEntityVariable = Either.left("true".asExpression())
    val variables: List<MoLangConfigVariable> = emptyList()
    val script: ExpressionLike = "0".asExpressionLike()

    companion object {
        val runtime = MoLangRuntime().setup()
    }

    override fun getVariables(entity: LivingEntity) = variables
    override fun configure(entity: LivingEntity, brainConfigurationContext: BrainConfigurationContext) {
        if (!checkCondition(entity, condition)) return
        runtime.withQueryValue("entity", entity.asMostSpecificMoLangValue())
        runtime.resolve(script)
    }
}