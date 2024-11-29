/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.block.entity

import com.cobblemon.mod.common.CobblemonBlockEntities
import com.cobblemon.mod.common.CobblemonRecipeTypes
import com.cobblemon.mod.common.client.gui.cookingpot.CookingPotMenu
import com.cobblemon.mod.common.client.gui.cookingpot.CookingPotRecipe
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.NonNullList
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.WorldlyContainer
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.StackedContents
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.RecipeCraftingHolder
import net.minecraft.world.inventory.StackedContentsCompatible
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.CraftingInput
import net.minecraft.world.item.crafting.RecipeHolder
import net.minecraft.world.item.crafting.RecipeManager
import net.minecraft.world.item.crafting.RecipeType
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity
import net.minecraft.world.level.block.state.BlockState

class CookingPotBlockEntity : BaseContainerBlockEntity, WorldlyContainer, RecipeCraftingHolder, StackedContentsCompatible {

    companion object {
        fun serverTick(level: Level, pos: BlockPos, state: BlockState, cookingPotBlockEntity: CookingPotBlockEntity) {
            print(cookingPotBlockEntity.items[0]);
        }

    }


    private var items : NonNullList<ItemStack?>
    private val recipesUsed: Object2IntOpenHashMap<ResourceLocation>
    private val quickCheck: RecipeManager.CachedCheck<CraftingInput, *>

    constructor(pos: BlockPos, state: BlockState) : super(CobblemonBlockEntities.COOKING_POT, pos, state) {
        this.items = NonNullList.withSize(3, ItemStack.EMPTY);
        this.recipesUsed = Object2IntOpenHashMap()
        this.quickCheck = RecipeManager.createCheck(CobblemonRecipeTypes.COOKING_POT_COOKING);
    }

    override fun getDefaultName(): Component? {
        return Component.translatable("container.cooking_pot")
    }

    override fun getItems(): NonNullList<ItemStack?>? {
        return this.items
    }

    override fun setItems(items: NonNullList<ItemStack?>) {
        this.items = items
    }

    override fun createMenu(
        containerId: Int,
        inventory: Inventory
    ): AbstractContainerMenu? {
        return CookingPotMenu(containerId, inventory)
    }

    override fun getContainerSize(): Int {
        return 20
    }

    override fun getSlotsForFace(side: Direction): IntArray? {
        return intArrayOf(0, 1, 2)
    }

    override fun canPlaceItemThroughFace(
        index: Int,
        itemStack: ItemStack,
        direction: Direction?
    ): Boolean {
        return false
    }

    override fun canTakeItemThroughFace(
        index: Int,
        stack: ItemStack,
        direction: Direction
    ): Boolean {
        return false
    }

    override fun setRecipeUsed(recipe: RecipeHolder<*>?) {
        if (recipe != null) {
            val resourceLocation = recipe.id();
            this.recipesUsed.addTo(resourceLocation, 1);
        }
    }

    override fun getRecipeUsed(): RecipeHolder<*>? {
        return null
    }

    override fun fillStackedContents(contents: StackedContents) {
        TODO("Not yet implemented")
    }

}