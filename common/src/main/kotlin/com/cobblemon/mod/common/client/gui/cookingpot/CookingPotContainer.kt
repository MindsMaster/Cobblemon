package com.cobblemon.mod.common.client.gui.cookingpot

import net.minecraft.core.NonNullList
import net.minecraft.world.ContainerHelper
import net.minecraft.world.entity.player.Player
import net.minecraft.world.entity.player.StackedContents
import net.minecraft.world.inventory.CraftingContainer
import net.minecraft.world.inventory.TransientCraftingContainer
import net.minecraft.world.item.ItemStack
import java.util.List;

class CookingPotContainer : TransientCraftingContainer {

    val menu : CookingPotMenu?
    val items : NonNullList<ItemStack>
    var outputSlot: ItemStack = ItemStack.EMPTY

    constructor(menu : CookingPotMenu, width: Int, height: Int) : super(menu, width, height) {
        this.menu = menu
        this.items = NonNullList.withSize(11, ItemStack.EMPTY)
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
        return when (slot) {
            0 -> outputSlot // Result slot
            in 1..9 -> if (slot - 1 < items.size) this.items[slot - 1] else ItemStack.EMPTY
            else -> ItemStack.EMPTY // Invalid slot
        }
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

    override fun setItem(slot: Int, stack: ItemStack) {
        when (slot) {
            0 -> outputSlot = stack // Result slot
            in 1..9 -> this.items[slot - 1] = stack // Adjust for crafting slots
            else -> println("Invalid slot index: $slot")
        }
    }


    override fun setChanged() {}

    override fun stillValid(player: Player): Boolean {
        return true
    }

    override fun clearContent() {
        this.items[0] = ItemStack.EMPTY
        this.items[1] = ItemStack.EMPTY
        this.items[2] = ItemStack.EMPTY
        this.items[3] = ItemStack.EMPTY
        this.items[4] = ItemStack.EMPTY
        this.items[5] = ItemStack.EMPTY
        this.items[6] = ItemStack.EMPTY
        this.items[7] = ItemStack.EMPTY
        this.items[8] = ItemStack.EMPTY
        this.items[9] = ItemStack.EMPTY
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