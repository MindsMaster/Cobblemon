package com.cobblemon.mod.common.advancement.criterion

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import java.util.Optional
import net.minecraft.advancements.critereon.ContextAwarePredicate
import net.minecraft.advancements.critereon.EntityPredicate
import net.minecraft.server.level.ServerPlayer

class ReelInPokemonCriterionCondition(
        playerCtx: Optional<ContextAwarePredicate>,
        val pokemonId: Optional<String>
) : SimpleCriterionCondition<String?>(playerCtx) {
    companion object {
        val CODEC: Codec<ReelInPokemonCriterionCondition> = RecordCodecBuilder.create { it.group(
                EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(ReelInPokemonCriterionCondition::playerCtx),
                Codec.STRING.optionalFieldOf("pokemonId").forGetter(ReelInPokemonCriterionCondition::pokemonId)
        ).apply(it, ::ReelInPokemonCriterionCondition) }
    }

    override fun matches(player: ServerPlayer, context: String?): Boolean {
        println("Matching pokemonId: ${pokemonId.orElse("null")}, context: $context")
        return pokemonId.isEmpty || pokemonId.get() == context
    }
}
