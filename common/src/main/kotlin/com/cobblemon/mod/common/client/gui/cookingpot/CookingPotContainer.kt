package com.cobblemon.mod.common.client.gui.cookingpot

import net.minecraft.core.NonNullList
import net.minecraft.world.ContainerHelper
import net.minecraft.world.entity.player.Player
import net.minecraft.world.entity.player.StackedContents
import net.minecraft.world.inventory.CraftingContainer
import net.minecraft.world.item.ItemStack
import java.util.List;

class CookingPotContainer : CraftingContainer {

    val menu : CookingPotMenu
    val items : NonNullList<ItemStack>

    constructor(menu : CookingPotMenu) {
        this.menu = menu
        this.items = NonNullList.withSize(10, ItemStack.EMPTY)
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

    override fun removeItemNoUpdate(slot: Int): ItemStack {
        return ContainerHelper.takeItem(this.items, slot)
    }

    override fun removeItem(slot: Int, amount: Int): ItemStack {
        val itemStack = ContainerHelper.removeItem(this.items, slot, amount)
        if (!itemStack.isEmpty) {
            this.menu.slotsChanged(this)
        }
        return itemStack
    }

    override fun setItem(slot: Int, stack: ItemStack) {
        this.items[slot] = stack
        this.menu.slotsChanged(this)
    }

    override fun setChanged() {}

    override fun stillValid(player: Player): Boolean {
        return true
    }

    override fun clearContent() {
        this.items.clear()
    }

    override fun getHeight(): Int {
        return 3
    }

    override fun getWidth(): Int {
        return 3
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