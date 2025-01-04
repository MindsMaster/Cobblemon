package com.cobblemon.mod.common.api.cooking

import com.cobblemon.mod.common.util.cobblemonResource
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.resources.ResourceLocation

data class Seasoning(
    val ingredient: ResourceLocation,
    val flavor: String,
    val color: String,
    val quality: Int
) {
    companion object {
        val CODEC: Codec<Seasoning> = RecordCodecBuilder.create { builder ->
            builder.group(
                ResourceLocation.CODEC.fieldOf("ingredient").forGetter<Seasoning> { it.ingredient },
                Codec.STRING.fieldOf("flavor").forGetter<Seasoning> { it.flavor },
                Codec.STRING.fieldOf("color").forGetter<Seasoning> { it.color },
                Codec.INT.fieldOf("quality").forGetter<Seasoning> { it.quality }
            ).apply(builder, ::Seasoning)
        }

        val BLANK_SEASONING = Seasoning(
            cobblemonResource("blank"),
            "",
            "",
            0
        )
    }
}
