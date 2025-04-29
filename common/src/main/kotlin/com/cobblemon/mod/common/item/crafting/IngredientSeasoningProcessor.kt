package com.cobblemon.mod.common.item.crafting

import com.cobblemon.mod.common.CobblemonItemComponents
import com.cobblemon.mod.common.api.cooking.Seasonings
import com.cobblemon.mod.common.item.components.IngredientComponent
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack

object IngredientSeasoningProcessor : SeasoningProcessor {
    override val type = "ingredient"

    override fun apply(result: ItemStack, seasoning: List<ItemStack>) {
        for (seasoningStack in seasoning) {
            val seasoningData = Seasonings.getFromItemStack(seasoningStack) ?: continue
            val itemId = seasoningStack.item.builtInRegistryHolder().key().location()

            // Use the first matching ingredient only (you could allow multiple if needed)
            result.set(CobblemonItemComponents.INGREDIENT, IngredientComponent(itemId))
            break
        }
    }
}