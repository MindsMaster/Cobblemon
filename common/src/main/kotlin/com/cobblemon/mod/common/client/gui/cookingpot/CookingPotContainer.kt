/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui.cookingpot

import com.cobblemon.mod.common.block.entity.CampfireBlockEntity.Companion.CRAFTING_GRID_WIDTH
import com.cobblemon.mod.common.block.entity.CampfireBlockEntity.Companion.ITEMS_SIZE
import com.cobblemon.mod.common.block.entity.CampfireBlockEntity.Companion.RESULT_SLOT
import net.minecraft.core.NonNullList
import net.minecraft.world.ContainerHelper
import net.minecraft.world.entity.player.Player
import net.minecraft.world.entity.player.StackedContents
import net.minecraft.world.inventory.TransientCraftingContainer
import net.minecraft.world.item.ItemStack
import java.util.List

class CookingPotContainer : TransientCraftingContainer {

    val menu : CookingPotMenu?
    val items : NonNullList<ItemStack>
    var outputSlot: ItemStack = ItemStack.EMPTY

    constructor(menu : CookingPotMenu, width: Int, height: Int) : super(menu, width, height) {
        this.menu = menu
        this.items = NonNullList.withSize(ITEMS_SIZE, ItemStack.EMPTY)
    }

    override fun getContainerSize(): Int {
        return this.items.size
    }

    override fun isEmpty(): Boolean {
        for (itemStack in this.items) {
            if (!itemStack.isEmpty) {
                return false
            }
        }
        return true
    }

    override fun getItem(slot: Int): ItemStack {
        return if (slot >= this.getContainerSize()) ItemStack.EMPTY else this.items[slot]
    }

    override fun setItem(slot: Int, stack: ItemStack) {
        this.items[slot] = stack
        this.menu?.slotsChanged(this)
    }

    override fun removeItemNoUpdate(slot: Int): ItemStack {
        return ContainerHelper.takeItem(this.items, slot)
    }

    override fun removeItem(slot: Int, amount: Int): ItemStack {
        val itemStack = ContainerHelper.removeItem(this.items, slot, amount)
        if (!itemStack.isEmpty) {
            this.menu?.slotsChanged(this)
        }
        return itemStack
    }

    override fun setChanged() {}

    override fun stillValid(player: Player): Boolean {
        return true
    }

    override fun clearContent() {
        for (index in 0 until this.items.size) {
            this.items[index] = ItemStack.EMPTY
        }
    }

    override fun getHeight(): Int {
        return CRAFTING_GRID_WIDTH
    }

    override fun getWidth(): Int {
        return CRAFTING_GRID_WIDTH
    }

    override fun getItems(): kotlin.collections.List<ItemStack?>? {
        return List.copyOf(this.items)
    }

    override fun fillStackedContents(contents: StackedContents) {
        for (itemStack in this.items) {
            contents.accountSimpleStack(itemStack)
        }
    }
}