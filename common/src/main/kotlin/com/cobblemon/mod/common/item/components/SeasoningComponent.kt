package com.cobblemon.mod.common.item.components

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.resources.ResourceLocation
import java.util.Optional

class SeasoningComponent(
    val ingredient1: ResourceLocation?,
    val flavor1: String?,
    val color1: String?,
    val quality1: Int?,
    val ingredient2: ResourceLocation?,
    val flavor2: String?,
    val color2: String?,
    val quality2: Int?,
    val ingredient3: ResourceLocation?,
    val flavor3: String?,
    val color3: String?
) {
    companion object {
        val CODEC: Codec<SeasoningComponent> = RecordCodecBuilder.create { builder ->
            builder.group(
                ResourceLocation.CODEC.optionalFieldOf("ingredient1").forGetter { Optional.ofNullable(it.ingredient1) },
                Codec.STRING.optionalFieldOf("flavor1").forGetter { Optional.ofNullable(it.flavor1) },
                Codec.STRING.optionalFieldOf("color1").forGetter { Optional.ofNullable(it.color1) },
                Codec.INT.optionalFieldOf("quality1").forGetter { Optional.ofNullable(it.quality1) },
                ResourceLocation.CODEC.optionalFieldOf("ingredient2").forGetter { Optional.ofNullable(it.ingredient2) },
                Codec.STRING.optionalFieldOf("flavor2").forGetter { Optional.ofNullable(it.flavor2) },
                Codec.STRING.optionalFieldOf("color2").forGetter { Optional.ofNullable(it.color2) },
                Codec.INT.optionalFieldOf("quality2").forGetter { Optional.ofNullable(it.quality2) },
                ResourceLocation.CODEC.optionalFieldOf("ingredient3").forGetter { Optional.ofNullable(it.ingredient3) },
                Codec.STRING.optionalFieldOf("flavor3").forGetter { Optional.ofNullable(it.flavor3) },
                Codec.STRING.optionalFieldOf("color3").forGetter { Optional.ofNullable(it.color3) }
            ).apply(builder) { ingredient1Opt, flavor1Opt, color1Opt, quality1Opt,
                               ingredient2Opt, flavor2Opt, color2Opt, quality2Opt,
                               ingredient3Opt, flavor3Opt, color3Opt ->
                SeasoningComponent(
                    ingredient1Opt.orElse(null),
                    flavor1Opt.orElse(null),
                    color1Opt.orElse(null),
                    quality1Opt.orElse(null),
                    ingredient2Opt.orElse(null),
                    flavor2Opt.orElse(null),
                    color2Opt.orElse(null),
                    quality2Opt.orElse(null),
                    ingredient3Opt.orElse(null),
                    flavor3Opt.orElse(null),
                    color3Opt.orElse(null)
                )
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        return other is SeasoningComponent &&
                ingredient1 == other.ingredient1 &&
                flavor1 == other.flavor1 &&
                color1 == other.color1 &&
                quality1 == other.quality1 &&
                ingredient2 == other.ingredient2 &&
                flavor2 == other.flavor2 &&
                color2 == other.color2 &&
                quality2 == other.quality2 &&
                ingredient3 == other.ingredient3 &&
                flavor3 == other.flavor3 &&
                color3 == other.color3
    }

    override fun hashCode(): Int {
        return listOf(
            ingredient1, flavor1, color1, quality1,
            ingredient2, flavor2, color2, quality2,
            ingredient3, flavor3, color3
        ).fold(0) { acc, value -> 31 * acc + (value?.hashCode() ?: 0) }
    }
}
