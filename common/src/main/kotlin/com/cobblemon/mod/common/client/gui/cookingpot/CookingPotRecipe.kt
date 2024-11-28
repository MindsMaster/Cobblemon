/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui.cookingpot

import com.cobblemon.mod.common.CobblemonRecipeSerializers
import com.cobblemon.mod.common.CobblemonRecipeTypes
import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.HolderLookup
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.*
import net.minecraft.world.level.Level

class CookingPotRecipe(
    val pattern: ShapedRecipePattern,
    val result: ItemStack,
    val groupName: String,
    val category: CookingPotBookCategory,
    val showNotification: Boolean
) : Recipe<CraftingInput> {

    override fun getGroup(): String {
        return groupName
    }

    override fun matches(input: CraftingInput, level: Level): Boolean {
        val matches = this.pattern.matches(input)
        println("Matching recipe: $this, Input: $input, Matches: $matches")
        return matches
    }

    override fun assemble(
        input: CraftingInput,
        registries: HolderLookup.Provider
    ): ItemStack? {
        return this.getResultItem(registries)?.copy()
    }

    override fun canCraftInDimensions(width: Int, height: Int): Boolean {
        return width >= this.pattern.width() && height >= this.pattern.height()
    }

    override fun getResultItem(registries: HolderLookup.Provider): ItemStack? {
        return this.result
    }

    override fun getSerializer(): RecipeSerializer<*>? {
        return CobblemonRecipeSerializers.COOKING_POT_COOKING
    }

    override fun getType(): RecipeType<CookingPotRecipe> {
        return CobblemonRecipeTypes.COOKING_POT_COOKING
    }

    class Serializer : RecipeSerializer<CookingPotRecipe> {
        companion object {
            val CODEC: MapCodec<CookingPotRecipe> = RecordCodecBuilder.mapCodec { instance ->
                instance.group(
                    ShapedRecipePattern.MAP_CODEC.forGetter { recipe -> recipe.pattern },
                    ItemStack.STRICT_CODEC.fieldOf("result").forGetter { recipe -> recipe.result },
                    Codec.STRING.optionalFieldOf("group", "").forGetter { recipe -> recipe.group },
                    CookingPotBookCategory.CODEC.fieldOf("category").orElse(CookingPotBookCategory.MISC).forGetter { recipe -> recipe.category },
                    Codec.BOOL.optionalFieldOf("show_notification", true).forGetter { recipe -> recipe.showNotification }
                ).apply(instance, ::CookingPotRecipe)
            }

            val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, CookingPotRecipe> = StreamCodec.of(::toNetwork, ::fromNetwork)

            private fun fromNetwork(buffer: RegistryFriendlyByteBuf): CookingPotRecipe {
                val group = buffer.readUtf()
                val category = buffer.readEnum(CookingPotBookCategory::class.java)
                val pattern = ShapedRecipePattern.STREAM_CODEC.decode(buffer)
                val result = ItemStack.STREAM_CODEC.decode(buffer)
                val showNotification = buffer.readBoolean()
                return CookingPotRecipe(pattern, result, group, category, showNotification)
            }

            private fun toNetwork(buffer: RegistryFriendlyByteBuf, recipe: CookingPotRecipe) {
                buffer.writeUtf(recipe.groupName)
                buffer.writeEnum(recipe.category)
                ShapedRecipePattern.STREAM_CODEC.encode(buffer, recipe.pattern)
                ItemStack.STREAM_CODEC.encode(buffer, recipe.result)
                buffer.writeBoolean(recipe.showNotification)
            }
        }

        override fun codec(): MapCodec<CookingPotRecipe> {
            return CODEC
        }

        override fun streamCodec(): StreamCodec<RegistryFriendlyByteBuf, CookingPotRecipe> {
            return STREAM_CODEC
        }
    }
}
