package com.cobblemon.mod.common.item.crafting

import com.cobblemon.mod.common.CobblemonItemComponents
import com.cobblemon.mod.common.api.cooking.Seasonings
import com.cobblemon.mod.common.item.components.IngredientComponent
import net.minecraft.world.item.ItemStack

object IngredientSeasoningProcessor : SeasoningProcessor {
    override val type = "ingredient"

    override fun apply(result: ItemStack, seasoning: List<ItemStack>) {
        val ingredients = seasoning.mapNotNull { seasoningStack ->
            val seasoningData = Seasonings.getFromItemStack(seasoningStack)
            if (seasoningData != null) {
                seasoningStack.item.builtInRegistryHolder().key().location()
            } else null
        }

        if (ingredients.isNotEmpty()) {
            result.set(CobblemonItemComponents.INGREDIENT, IngredientComponent(ingredients))
        }
    }
}