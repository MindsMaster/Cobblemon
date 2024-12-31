package com.cobblemon.mod.common.client.gui.cookingpot

import com.cobblemon.mod.common.block.entity.CampfireBlockEntity
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
        super.onTake(player, stack)

        // Consume one of each ingredient in the crafting slots
        val menu = player.containerMenu
        if (menu is CookingPotMenu) {
            menu.consumeCraftingIngredients() // Decrement ingredients
            menu.broadcastChanges() // Notify the client
        } else {
            println("Player menu is not CookingPotMenu!")
        }
    }






    override fun mayPlace(stack: ItemStack): Boolean {
        return false
    }

}