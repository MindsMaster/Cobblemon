/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.item.crafting

import net.minecraft.core.HolderLookup
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.CraftingInput
import net.minecraft.world.item.crafting.Recipe
import net.minecraft.world.level.Level

interface CookingPotRecipeBase: Recipe<CraftingInput> {
    val result: ItemStack
    val groupName: String
    val category: CookingPotBookCategory
    val seasoningProcessors: List<SeasoningProcessor>

    override fun getGroup() = groupName
    override fun matches(input: CraftingInput, level: Level): Boolean
    fun category() = category
    override fun getResultItem(registries: HolderLookup.Provider) = this.result

    override fun assemble(input: CraftingInput, registries: HolderLookup.Provider): ItemStack {
        return result.copy()
    }

    fun applySeasoning(stack: ItemStack, seasoning: List<ItemStack>) {
        for (processor in seasoningProcessors) {
            processor.apply(stack, seasoning)
        }
    }
}
