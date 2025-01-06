package com.cobblemon.mod.common.api.cooking

import com.cobblemon.mod.common.util.cobblemonResource
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.resources.ResourceLocation

data class Seasoning(
    val ingredient: ResourceLocation,
    val flavors: Map<String, Int>, // Updated to store a map of flavor types and values
    val color: String,
    val quality: Int
) {
    companion object {
        val CODEC: Codec<Seasoning> = RecordCodecBuilder.create { builder ->
            builder.group(
                ResourceLocation.CODEC.fieldOf("ingredient").forGetter<Seasoning> { it.ingredient },
                Codec.unboundedMap(Codec.STRING, Codec.INT).fieldOf("flavors").forGetter<Seasoning> { it.flavors }, // Use map codec
                Codec.STRING.fieldOf("color").forGetter<Seasoning> { it.color },
                Codec.INT.fieldOf("quality").forGetter<Seasoning> { it.quality }
            ).apply(builder, ::Seasoning)
        }

        val BLANK_SEASONING = Seasoning(
            ingredient = cobblemonResource("blank"),
            flavors = mapOf("spicy" to 0, "dry" to 0, "sweet" to 0, "bitter" to 0, "sour" to 0), // Default to all 0
            color = "",
            quality = 0
        )
    }
}
