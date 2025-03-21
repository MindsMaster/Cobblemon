/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.item.crafting

import com.cobblemon.mod.common.CobblemonRecipeSerializers
import com.cobblemon.mod.common.CobblemonRecipeTypes
import com.cobblemon.mod.common.util.codec.CodecUtils.createByStringCodec
import com.cobblemon.mod.common.util.readString
import com.cobblemon.mod.common.util.writeString
import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.PrimitiveCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.HolderLookup
import net.minecraft.core.NonNullList
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.CraftingInput
import net.minecraft.world.item.crafting.Ingredient
import net.minecraft.world.item.crafting.Recipe
import net.minecraft.world.item.crafting.RecipeSerializer
import net.minecraft.world.item.crafting.RecipeType
import net.minecraft.world.item.crafting.ShapedRecipePattern
import net.minecraft.world.level.Level

class CookingPotRecipe(
    val pattern: ShapedRecipePattern,
    override val result: ItemStack,
    override val groupName: String,
    override val category: CookingPotBookCategory,
    override val seasoningProcessors: List<SeasoningProcessor>
) : CookingPotRecipeBase {
    override fun getType() = CobblemonRecipeTypes.COOKING_POT_COOKING
    override fun canCraftInDimensions(width: Int, height: Int) = true
    override fun getSerializer() = CobblemonRecipeSerializers.COOKING_POT_COOKING
    override fun getIngredients(): NonNullList<Ingredient> = this.pattern.ingredients()
    override fun matches(input: CraftingInput, level: Level): Boolean {
        //println("Validating recipe match in CookingPotRecipe...")

        // Create a filtered CraftingInput with only slots 1-9
        val filteredItems = (0..8).mapNotNull { index ->
            if (index < input.size()) input.getItem(index) else ItemStack.EMPTY
        }
        val filteredInput = CraftingInput.of(3, 3, filteredItems)

        /*// Debugging: Log filtered crafting grid contents
        for (i in 0 until filteredInput.size()) {
            val itemStack = filteredInput.getItem(i)
            println("Filtered crafting slot $i: ${itemStack.item} (${itemStack.count})")
        }*/

        // Perform pattern matching on the filtered input
        val matches = this.pattern.matches(filteredInput)
        //println("Pattern match result: $matches")

        /*// Additional logic for specific recipes
        if (this.pattern.width() == 3 && this.pattern.height() == 3) {
            if (this.result.item == CobblemonItems.DAWN_STONE_BLOCK.asItem()) {
                println("Special case for Dawn Stone Block recipe.")
            }
        }*/

        return matches
    }

    class Serializer : RecipeSerializer<CookingPotRecipe> {
        companion object {
            val CODEC: MapCodec<CookingPotRecipe> = RecordCodecBuilder.mapCodec { instance ->
                instance.group(
                    ShapedRecipePattern.MAP_CODEC.forGetter { recipe -> recipe.pattern },
                    ItemStack.STRICT_CODEC.fieldOf("result").forGetter { recipe -> recipe.result },
                    Codec.STRING.optionalFieldOf("group", "").forGetter { recipe -> recipe.group },
                    CookingPotBookCategory.Companion.CODEC.fieldOf("category").orElse(CookingPotBookCategory.MISC).forGetter { recipe -> recipe.category },
                    createByStringCodec<SeasoningProcessor>(
                        { SeasoningProcessor.processors[it] },
                        { it.type },
                        { "Unknown seasoning processor: $it" }
                    ).listOf().fieldOf("seasoningProcessors").forGetter { recipe -> recipe.seasoningProcessors }
                ).apply(instance, ::CookingPotRecipe)
            }

            val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, CookingPotRecipe> = StreamCodec.of(::toNetwork, ::fromNetwork)

            private fun fromNetwork(buffer: RegistryFriendlyByteBuf): CookingPotRecipe {
                val group = buffer.readUtf()
                val category = buffer.readEnum(CookingPotBookCategory::class.java)
                val pattern = ShapedRecipePattern.STREAM_CODEC.decode(buffer)
                val result = ItemStack.STREAM_CODEC.decode(buffer)
                val seasoningProcessors = buffer.readList {
                    val type = buffer.readString()
                    SeasoningProcessor.processors[type] ?: error("Unknown seasoning processor: $type")
                }
                return CookingPotRecipe(pattern, result, group, category, seasoningProcessors)
            }

            private fun toNetwork(buffer: RegistryFriendlyByteBuf, recipe: CookingPotRecipe) {
                buffer.writeUtf(recipe.groupName)
                buffer.writeEnum(recipe.category)
                ShapedRecipePattern.STREAM_CODEC.encode(buffer, recipe.pattern)
                ItemStack.STREAM_CODEC.encode(buffer, recipe.result)
                buffer.writeCollection(recipe.seasoningProcessors) { _, it ->
                    buffer.writeString(it.type)
                }
            }
        }

        override fun codec() = CODEC
        override fun streamCodec() = STREAM_CODEC
    }
}
