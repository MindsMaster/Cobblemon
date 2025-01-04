package com.cobblemon.mod.common.item.components

import com.cobblemon.mod.common.api.fishing.FishingBait
import com.cobblemon.mod.common.api.fishing.FishingBaits
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack

class CookingComponent(
    val bait1: FishingBait,
    val stack1: ItemStack = ItemStack.EMPTY,
    val bait2: FishingBait,
    val stack2: ItemStack = ItemStack.EMPTY,
    val bait3: FishingBait,
    val stack3: ItemStack = ItemStack.EMPTY,
    val ingredient1: ResourceLocation = ResourceLocation("minecraft", "unknown"),
    val color1: String = "",
    val flavor1: String = "",
    val quality1: Int = 0,
    val ingredient2: ResourceLocation = ResourceLocation("minecraft", "unknown"),
    val color2: String = "",
    val flavor2: String = "",
    val quality2: Int = 0,
    val ingredient3: ResourceLocation = ResourceLocation("minecraft", "unknown"),
    val color3: String = "",
    val flavor3: String = "",
    val quality3: Int = 0
) {
    companion object {
        val CODEC: Codec<CookingComponent> = RecordCodecBuilder.create { builder ->
            builder.group(
                ResourceLocation.CODEC.fieldOf("bait1").forGetter<CookingComponent> { it.bait1.item },
                ItemStack.CODEC.optionalFieldOf("stack1", ItemStack.EMPTY).forGetter<CookingComponent> { it.stack1 },
                ResourceLocation.CODEC.fieldOf("bait2").forGetter<CookingComponent> { it.bait2.item },
                ItemStack.CODEC.optionalFieldOf("stack2", ItemStack.EMPTY).forGetter<CookingComponent> { it.stack2 },
                ResourceLocation.CODEC.fieldOf("bait3").forGetter<CookingComponent> { it.bait3.item },
                ItemStack.CODEC.optionalFieldOf("stack3", ItemStack.EMPTY).forGetter<CookingComponent> { it.stack3 },
                ResourceLocation.CODEC.optionalFieldOf("ingredient1", ResourceLocation("minecraft", "unknown")).forGetter<CookingComponent> { it.ingredient1 },
                Codec.STRING.optionalFieldOf("color1", "").forGetter<CookingComponent> { it.color1 },
                Codec.INT.optionalFieldOf("quality1", 0).forGetter<CookingComponent> { it.quality1 },
                ResourceLocation.CODEC.optionalFieldOf("ingredient2", ResourceLocation("minecraft", "unknown")).forGetter<CookingComponent> { it.ingredient2 },
                Codec.STRING.optionalFieldOf("color2", "").forGetter<CookingComponent> { it.color2 },
                Codec.INT.optionalFieldOf("quality2", 0).forGetter<CookingComponent> { it.quality2 },
                ResourceLocation.CODEC.optionalFieldOf("ingredient3", ResourceLocation("minecraft", "unknown")).forGetter<CookingComponent> { it.ingredient3 },
                Codec.STRING.optionalFieldOf("color3", "").forGetter<CookingComponent> { it.color3 },
                Codec.INT.optionalFieldOf("quality3", 0).forGetter<CookingComponent> { it.quality3 }
            ).apply(builder) { bait1Loc, stack1, bait2Loc, stack2, bait3Loc, stack3,
                               ingredient1, color1, quality1, ingredient2, color2, quality2, ingredient3, color3, quality3 ->
                CookingComponent(
                    bait1 = FishingBaits.getFromIdentifier(bait1Loc) ?: FishingBait.BLANK_BAIT,
                    stack1 = stack1,
                    bait2 = FishingBaits.getFromIdentifier(bait2Loc) ?: FishingBait.BLANK_BAIT,
                    stack2 = stack2,
                    bait3 = FishingBaits.getFromIdentifier(bait3Loc) ?: FishingBait.BLANK_BAIT,
                    stack3 = stack3,
                    ingredient1 = ingredient1,
                    color1 = color1,
                    quality1 = quality1,
                    ingredient2 = ingredient2,
                    color2 = color2,
                    quality2 = quality2,
                    ingredient3 = ingredient3,
                    color3 = color3,
                    quality3 = quality3
                )
            }
        }

    }

    override fun equals(other: Any?): Boolean {
        return other is CookingComponent &&
                bait1 == other.bait1 && stack1 == other.stack1 &&
                bait2 == other.bait2 && stack2 == other.stack2 &&
                bait3 == other.bait3 && stack3 == other.stack3 &&
                ingredient1 == other.ingredient1 && color1 == other.color1 &&
                flavor1 == other.flavor1 && quality1 == other.quality1 &&
                ingredient2 == other.ingredient2 && color2 == other.color2 &&
                flavor2 == other.flavor2 && quality2 == other.quality2 &&
                ingredient3 == other.ingredient3 && color3 == other.color3 &&
                flavor3 == other.flavor3 && quality3 == other.quality3
    }

    override fun hashCode(): Int {
        return listOf(
            bait1, stack1, bait2, stack2, bait3, stack3,
            ingredient1, color1, flavor1, quality1,
            ingredient2, color2, flavor2, quality2,
            ingredient3, color3, flavor3, quality3
        ).hashCode()
    }
}
