package com.cobblemon.mod.common.client.gui.cookingpot

import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.CraftingContainer
import net.minecraft.world.inventory.ResultSlot
import net.minecraft.world.item.ItemStack

class CookingPotResultSlot : ResultSlot {

    constructor(player: Player, container: CraftingContainer, resultContainer: CraftingContainer, index: Int, x: Int, y: Int) : super(player, container, resultContainer, index, x, y) {

    }

    override fun onTake(player: Player, stack: ItemStack) {

    }

}