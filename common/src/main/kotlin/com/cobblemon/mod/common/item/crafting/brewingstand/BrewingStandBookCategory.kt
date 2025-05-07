/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.item.crafting.brewingstand

import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import io.netty.buffer.ByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.util.ByIdMap
import net.minecraft.util.StringRepresentable
import java.util.function.IntFunction

enum class BrewingStandBookCategory(
    private val categoryName: String,
    private val categoryId: Int
) : StringRepresentable {
    FOODS("foods", 0),
    MEDICINES("medicines", 1),
    BAITS("baits", 2),
    MISC("misc", 3);

    override fun getSerializedName() = categoryName
    private fun getCategoryId() = categoryId

    companion object {
        val CODEC: Codec<BrewingStandBookCategory> = Codec.STRING.flatXmap(
            { name ->
                val category = BrewingStandBookCategory.entries.firstOrNull { it.categoryName.equals(name, ignoreCase = true) }
                if (category != null) {
                    DataResult.success(category)
                } else {
                    DataResult.error { "Unknown category: $name" }
                }
            }, { category ->
                DataResult.success(category.categoryName)
            }
        )
        val BY_ID: IntFunction<BrewingStandBookCategory> = ByIdMap.continuous(
            BrewingStandBookCategory::getCategoryId,
            BrewingStandBookCategory.entries.toTypedArray(),
            ByIdMap.OutOfBoundsStrategy.ZERO
        )
        val STREAM_CODEC: StreamCodec<ByteBuf, BrewingStandBookCategory> = ByteBufCodecs.idMapper(BrewingStandBookCategory.Companion.BY_ID, BrewingStandBookCategory::getCategoryId)
    }
}