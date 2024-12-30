package com.cobblemon.mod.common.client.gui.cookingpot

import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.CraftingContainer
import net.minecraft.world.inventory.ResultContainer
import net.minecraft.world.inventory.ResultSlot
import net.minecraft.world.inventory.Slot
import net.minecraft.world.item.ItemStack

class CookingPotResultSlot : Slot {

    private val player: Player

    constructor(player: Player, container: ResultContainer, index: Int, x: Int, y: Int) : super(container, index, x, y) {
        this.player = player;
    }

    override fun onTake(player: Player, stack: ItemStack) {
        println("Item taken from result slot: ${stack.item}, Count: ${stack.count}")
        super.onTake(player, stack)
    }

    override fun mayPlace(stack: ItemStack): Boolean {
        return false
    }

}