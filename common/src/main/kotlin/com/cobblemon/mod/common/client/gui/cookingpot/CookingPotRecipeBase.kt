package com.cobblemon.mod.common.client.gui.cookingpot

import net.minecraft.core.HolderLookup
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.CraftingInput
import net.minecraft.world.item.crafting.Recipe
import net.minecraft.world.level.Level

interface CookingPotRecipeBase: Recipe<CraftingInput> {
    override fun matches(input: CraftingInput, level: Level): Boolean
    override fun assemble(input: CraftingInput, registries: HolderLookup.Provider): ItemStack
}
