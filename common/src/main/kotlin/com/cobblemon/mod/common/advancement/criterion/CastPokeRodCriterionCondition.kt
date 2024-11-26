/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.advancement.criterion

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.PrimitiveCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import java.util.Optional
import net.minecraft.advancements.critereon.ContextAwarePredicate
import net.minecraft.advancements.critereon.EntityPredicate
import net.minecraft.server.level.ServerPlayer
import net.minecraft.resources.ResourceLocation

class CastPokeRodCriterionCondition(
        playerCtx: Optional<ContextAwarePredicate>,
        val baitId: Optional<ResourceLocation>
) : SimpleCriterionCondition<ResourceLocation?>(playerCtx) {
    companion object {
        val CODEC: Codec<CastPokeRodCriterionCondition> = RecordCodecBuilder.create { it.group(
                EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(CastPokeRodCriterionCondition::playerCtx),
                ResourceLocation.CODEC.optionalFieldOf("baitId").forGetter(CastPokeRodCriterionCondition::baitId)
        ).apply(it, ::CastPokeRodCriterionCondition) }
    }

    override fun matches(player: ServerPlayer, context: ResourceLocation?): Boolean {
        return baitId.isEmpty || baitId.get() == context
    }
}