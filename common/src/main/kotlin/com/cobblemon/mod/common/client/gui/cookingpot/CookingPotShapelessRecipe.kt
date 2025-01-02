package com.cobblemon.mod.common.client.gui.cookingpot

import com.cobblemon.mod.common.CobblemonRecipeSerializers
import com.cobblemon.mod.common.CobblemonRecipeTypes
import com.cobblemon.mod.common.client.gui.cookingpot.CookingPotBookCategory
import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import it.unimi.dsi.fastutil.ints.IntList
import net.minecraft.core.HolderLookup
import net.minecraft.core.NonNullList
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.CraftingInput
import net.minecraft.world.item.crafting.CraftingRecipe
import net.minecraft.world.item.crafting.Ingredient
import net.minecraft.world.item.crafting.Recipe
import net.minecraft.world.item.crafting.RecipeSerializer
import net.minecraft.world.item.crafting.RecipeType
import net.minecraft.world.level.Level

class CookingPotShapelessRecipe(
    private val group: String,
    private val category: CookingPotBookCategory,
    private val result: ItemStack,
    private val ingredients: NonNullList<Ingredient>
) : Recipe<CraftingInput> {

    override fun getSerializer(): RecipeSerializer<*> {
        return CobblemonRecipeSerializers.COOKING_POT_SHAPELESS
    }

    override fun getType(): RecipeType<CookingPotShapelessRecipe> = CobblemonRecipeTypes.COOKING_POT_SHAPELESS

    override fun getGroup(): String {
        return group
    }

    fun category(): CookingPotBookCategory {
        return category
    }

    override fun getResultItem(registries: HolderLookup.Provider): ItemStack {
        return result
    }

    override fun getIngredients(): NonNullList<Ingredient> {
        return ingredients
    }

    override fun matches(input: CraftingInput, level: Level): Boolean {
        return if (input.ingredientCount() != ingredients.size) {
            false
        } else {
            if (input.size() == 1 && ingredients.size == 1) {
                ingredients[0].test(input.getItem(0))
            } else {
                input.stackedContents().canCraft(this, null)
            }
        }
    }

    override fun assemble(input: CraftingInput, registries: HolderLookup.Provider): ItemStack {
        return result.copy()
    }

    override fun canCraftInDimensions(width: Int, height: Int): Boolean {
        return width * height >= ingredients.size
    }

    class Serializer : RecipeSerializer<CookingPotShapelessRecipe> {
        companion object {
            private val CODEC: MapCodec<CookingPotShapelessRecipe> = RecordCodecBuilder.mapCodec { instance ->
                instance.group(
                    Codec.STRING.optionalFieldOf("group", "").forGetter { it.group },
                    CookingPotBookCategory.CODEC.fieldOf("category").orElse(CookingPotBookCategory.MISC).forGetter { it.category },
                    ItemStack.STRICT_CODEC.fieldOf("result").forGetter { it.result },
                    Ingredient.CODEC_NONEMPTY.listOf().fieldOf("ingredients").flatXmap({ list ->
                        val ingredients = list.filter { !it.isEmpty }.toTypedArray()
                        when {
                            ingredients.isEmpty() -> DataResult.error { "No ingredients for shapeless recipe" }
                            ingredients.size > 9 -> DataResult.error { "Too many ingredients for shapeless recipe" }
                            else -> DataResult.success(NonNullList.of(Ingredient.EMPTY, *ingredients))
                        }
                    }, { DataResult.success(it) }).forGetter { it.ingredients }
                ).apply(instance, ::CookingPotShapelessRecipe)
            }

            val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, CookingPotShapelessRecipe> =
                StreamCodec.of(::toNetwork, ::fromNetwork)

            private fun fromNetwork(buffer: RegistryFriendlyByteBuf): CookingPotShapelessRecipe {
                val group = buffer.readUtf()
                val category = buffer.readEnum(CookingPotBookCategory::class.java)
                val size = buffer.readVarInt()
                val ingredients = NonNullList.withSize(size, Ingredient.EMPTY)
                ingredients.replaceAll { Ingredient.CONTENTS_STREAM_CODEC.decode(buffer) }
                val result = ItemStack.STREAM_CODEC.decode(buffer)
                return CookingPotShapelessRecipe(group, category, result, ingredients)
            }

            private fun toNetwork(buffer: RegistryFriendlyByteBuf, recipe: CookingPotShapelessRecipe) {
                buffer.writeUtf(recipe.group)
                buffer.writeEnum(recipe.category)
                buffer.writeVarInt(recipe.ingredients.size)
                recipe.ingredients.forEach { ingredient ->
                    Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, ingredient)
                }
                ItemStack.STREAM_CODEC.encode(buffer, recipe.result)
            }
        }

        override fun codec(): MapCodec<CookingPotShapelessRecipe> {
            return CODEC
        }

        override fun streamCodec(): StreamCodec<RegistryFriendlyByteBuf, CookingPotShapelessRecipe> {
            return STREAM_CODEC
        }
    }
}
