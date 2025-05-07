package com.cobblemon.mod.common.item.crafting.brewingstand

import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.RecipeInput

class BrewingStandInput : RecipeInput {
    override fun getItem(index: Int): ItemStack? {
        return ItemStack.EMPTY
    }

    override fun size(): Int {
        return 0
    }
}