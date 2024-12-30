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

        // Clear or decrement items in the crafting slots
        val menu = player.containerMenu
        if (menu is CookingPotMenu) {
            for (i in 1..9) { // Assuming crafting slots are indices 1 to 9 in menu.items
                val itemInSlot = menu.items[i]
                if (!itemInSlot.isEmpty) {
                    itemInSlot.shrink(1) // Decrease count by 1
                    if (itemInSlot.count <= 0) {
                        menu.items[i] = ItemStack.EMPTY // Clear slot if count reaches 0
                    }
                }
            }
            menu.broadcastChanges() // Notify the client of changes
        } else {
            println("Player menu is not CookingPotMenu!")
        }
    }





    override fun mayPlace(stack: ItemStack): Boolean {
        return false
    }

}