/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.cooking

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder

data class MobCookingEffects(
    val appliedEffects: List<SerializableMobEffectInstance> = emptyList()
) {
    companion object {
        val CODEC: Codec<MobCookingEffects> = RecordCodecBuilder.create { builder ->
            builder.group(
                SerializableMobEffectInstance.CODEC.listOf().optionalFieldOf("appliedEffects", emptyList()).forGetter { it.appliedEffects }
            ).apply(builder, ::MobCookingEffects)
        }
    }
}