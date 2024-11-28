package com.cobblemon.mod.common.advancement.criterion

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import java.util.Optional
import net.minecraft.advancements.critereon.ContextAwarePredicate
import net.minecraft.advancements.critereon.EntityPredicate
import net.minecraft.server.level.ServerPlayer
import net.minecraft.resources.ResourceLocation

class ReelInPokemonCriterionCondition(
        playerCtx: Optional<ContextAwarePredicate>,
        val pokemonId: Optional<ResourceLocation>,
        val baitId: Optional<ResourceLocation>
) : SimpleCriterionCondition<Pair<ResourceLocation?, ResourceLocation?>>(playerCtx) {

    companion object {
        val CODEC: Codec<ReelInPokemonCriterionCondition> = RecordCodecBuilder.create { it.group(
                EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter { it.playerCtx },
                ResourceLocation.CODEC.optionalFieldOf("pokemonId").forGetter { it.pokemonId },
                ResourceLocation.CODEC.optionalFieldOf("baitId").forGetter { it.baitId }
        ).apply(it, ::ReelInPokemonCriterionCondition) }
    }

    override fun matches(player: ServerPlayer, context: Pair<ResourceLocation?, ResourceLocation?>): Boolean {
        val (contextPokemonId, contextBaitId) = context

        val pokemonMatches = pokemonId.isEmpty || pokemonId.get() == contextPokemonId
        val baitMatches = baitId.isEmpty || baitId.get() == contextBaitId
        return pokemonMatches && baitMatches
    }
}
