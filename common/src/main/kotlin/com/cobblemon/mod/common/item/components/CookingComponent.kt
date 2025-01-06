package com.cobblemon.mod.common.item.components

import com.cobblemon.mod.common.api.cooking.Seasoning
import com.cobblemon.mod.common.api.fishing.FishingBait
import com.cobblemon.mod.common.api.fishing.FishingBaits
import com.cobblemon.mod.common.api.cooking.Seasonings
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import io.netty.buffer.ByteBuf
import net.minecraft.resources.ResourceLocation

class CookingComponent(
    val bait1: FishingBait,
    val bait2: FishingBait,
    val bait3: FishingBait,
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
                    bait1 = FishingBaits.getFromIdentifier(bait1Loc) ?: FishingBait.BLANK_BAIT,
                    bait2 = FishingBaits.getFromIdentifier(bait2Loc) ?: FishingBait.BLANK_BAIT,
                    bait3 = FishingBaits.getFromIdentifier(bait3Loc) ?: FishingBait.BLANK_BAIT,
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

    fun getDominantFlavors(): List<String> {
        val flavors = listOf(seasoning1, seasoning2, seasoning3)
            .flatMap { it.flavors.entries }
            .groupingBy { it.key }
            .fold(0) { acc, entry -> acc + entry.value }

        val maxFlavorValue = flavors.values.maxOrNull()

        return flavors.filter { it.value == maxFlavorValue }
            .map { it.key }
    }
}
