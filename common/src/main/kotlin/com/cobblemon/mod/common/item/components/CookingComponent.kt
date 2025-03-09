/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.item.components

import com.cobblemon.mod.common.api.cooking.Seasoning
import com.cobblemon.mod.common.api.fishing.SpawnBait
import com.cobblemon.mod.common.api.fishing.FishingBaits
import com.cobblemon.mod.common.api.cooking.Seasonings
import com.cobblemon.mod.common.client.pot.CookingQuality
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import io.netty.buffer.ByteBuf
import net.minecraft.resources.ResourceLocation

class CookingComponent(
    val bait1: SpawnBait,
    val bait2: SpawnBait,
    val bait3: SpawnBait,
    val seasoning1: Seasoning,
    val seasoning2: Seasoning,
    val seasoning3: Seasoning
) {
    companion object {
        val CODEC: Codec<CookingComponent> = RecordCodecBuilder.create { builder ->
            builder.group(
                ResourceLocation.CODEC.fieldOf("bait1").forGetter<CookingComponent> { it.bait1.item },
                ResourceLocation.CODEC.fieldOf("bait2").forGetter<CookingComponent> { it.bait2.item },
                ResourceLocation.CODEC.fieldOf("bait3").forGetter<CookingComponent> { it.bait3.item },
                ResourceLocation.CODEC.fieldOf("seasoning1").forGetter<CookingComponent> { it.seasoning1.ingredient },
                ResourceLocation.CODEC.fieldOf("seasoning2").forGetter<CookingComponent> { it.seasoning2.ingredient },
                ResourceLocation.CODEC.fieldOf("seasoning3").forGetter<CookingComponent> { it.seasoning3.ingredient }
            ).apply(builder) { bait1Loc, bait2Loc, bait3Loc, seasoning1Loc, seasoning2Loc, seasoning3Loc ->
                CookingComponent(
                    bait1 = FishingBaits.getFromIdentifier(bait1Loc) ?: SpawnBait.BLANK_BAIT,
                    bait2 = FishingBaits.getFromIdentifier(bait2Loc) ?: SpawnBait.BLANK_BAIT,
                    bait3 = FishingBaits.getFromIdentifier(bait3Loc) ?: SpawnBait.BLANK_BAIT,
                    seasoning1 = Seasonings.getFromIdentifier(seasoning1Loc)
                        ?: Seasoning(ResourceLocation("minecraft", "unknown"), mapOf(), "", 0),
                    seasoning2 = Seasonings.getFromIdentifier(seasoning2Loc)
                        ?: Seasoning(ResourceLocation("minecraft", "unknown"), mapOf(), "", 0),
                    seasoning3 = Seasonings.getFromIdentifier(seasoning3Loc)
                        ?: Seasoning(ResourceLocation("minecraft", "unknown"), mapOf(), "", 0)
                )
            }
        }

        val PACKET_CODEC: StreamCodec<ByteBuf, CookingComponent> = ByteBufCodecs.fromCodec(CODEC)
    }

    override fun equals(other: Any?): Boolean {
        return other is CookingComponent &&
                bait1 == other.bait1 &&
                bait2 == other.bait2 &&
                bait3 == other.bait3 &&
                seasoning1 == other.seasoning1 &&
                seasoning2 == other.seasoning2 &&
                seasoning3 == other.seasoning3
    }

    override fun hashCode(): Int {
        return listOf(bait1, bait2, bait3, seasoning1, seasoning2, seasoning3).hashCode()
    }

    fun getCookingQuality(): CookingQuality {
        val qualityAverage = getQualityAverage()
        return when {
            qualityAverage < 10 -> CookingQuality.LOW
            qualityAverage < 20 -> CookingQuality.MEDIUM
            else -> CookingQuality.HIGH
        }
    }

    private fun getQualityAverage() = getSeasonings().map { it.quality }.average()

    fun getDominantFlavors(): List<String> {
        val flavors = getFlavorsSum()
        val maxFlavorValue = flavors.values.maxOrNull()

        return flavors.filter { it.value == maxFlavorValue }.map { it.key }
    }

    fun getFlavorsSum(): Map<String, Int> =
        getSeasonings()
            .flatMap { it.flavors.entries }
            .groupingBy { it.key }
            .fold(0) { acc, entry -> acc + entry.value }

    fun getSeasonings() = listOf(seasoning1, seasoning2, seasoning3).filter { it.quality > 0 }
}
