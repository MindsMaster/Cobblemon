package com.cobblemon.mod.common.item.crafting.brewingstand

import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.crafting.RecipeInput

class BrewingStandInput(
    private val ingredient: ItemStack,
    private val bottles: List<ItemStack>
) : RecipeInput {

    override fun getItem(index: Int): ItemStack? {
        return when (index) {
            0 -> ingredient
            in 1..3 -> bottles.getOrNull(index - 1)
            else -> null
        }
    }

    override fun size(): Int {
        return 1 + bottles.size
    }

    fun getIngredient(): ItemStack = ingredient
    fun getBottles(): List<ItemStack> = bottles
}