package com.cobblemon.mod.common.client.gui.cookingpot

import com.cobblemon.mod.common.CobblemonItemComponents
import com.cobblemon.mod.common.api.cooking.Seasoning
import com.cobblemon.mod.common.api.cooking.Seasonings
import com.cobblemon.mod.common.api.fishing.FishingBait
import com.cobblemon.mod.common.api.fishing.FishingBaits
import com.cobblemon.mod.common.item.components.CookingComponent
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.ResultContainer
import net.minecraft.world.inventory.Slot
import net.minecraft.world.item.ItemStack

class CookingPotResultSlot(
    private val player: Player,
    private val container: ResultContainer,
    index: Int,
    x: Int,
    y: Int
) : Slot(container, index, x, y) {

    override fun onTake(player: Player, stack: ItemStack) {
        val menu = player.containerMenu

        // Variables to store bait and seasoning
        var bait1 = FishingBait.BLANK_BAIT
        var bait2 = FishingBait.BLANK_BAIT
        var bait3 = FishingBait.BLANK_BAIT
        var seasoning1 = Seasoning.BLANK_SEASONING
        var seasoning2 = Seasoning.BLANK_SEASONING
        var seasoning3 = Seasoning.BLANK_SEASONING

        if (menu is CookingPotMenu) {
            // Iterate through slots 10-12
            for ((index, slot) in (10..12).withIndex()) {
                val itemInSlot = menu.getSlot(slot).item

                if (!itemInSlot.isEmpty) {
                    // Check if item is a bait
                    val bait = FishingBaits.getFromBaitItemStack(itemInSlot)
                    if (bait != null) {
                        when (index) {
                            0 -> bait1 = bait
                            1 -> bait2 = bait
                            2 -> bait3 = bait
                        }
                    }

                    // Check if item is a seasoning
                    val seasoning = Seasonings.getFromItemStack(itemInSlot)
                    if (seasoning != null) {
                        when (index) {
                            0 -> seasoning1 = seasoning
                            1 -> seasoning2 = seasoning
                            2 -> seasoning3 = seasoning
                        }
                    }
                }
            }

            // Create CookingComponent and attach to result item
            val cookingComponent = CookingComponent(
                bait1 = bait1,
                bait2 = bait2,
                bait3 = bait3,
                seasoning1 = seasoning1,
                seasoning2 = seasoning2,
                seasoning3 = seasoning3
            )

            // Attach the CookingComponent to the result stack
            stack.set(CobblemonItemComponents.COOKING_COMPONENT, cookingComponent)

            menu.consumeCraftingIngredients() // Decrement ingredients
            menu.broadcastChanges() // Notify the client
        } else {
            println("Player menu is not CookingPotMenu!")
        }

        super.onTake(player, stack)
    }

    override fun mayPlace(stack: ItemStack): Boolean {
        return false
    }
}