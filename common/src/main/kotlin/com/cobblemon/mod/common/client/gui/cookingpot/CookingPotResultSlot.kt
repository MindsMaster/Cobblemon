package com.cobblemon.mod.common.client.gui.cookingpot

import com.cobblemon.mod.common.CobblemonItemComponents
import com.cobblemon.mod.common.CobblemonSounds
import com.cobblemon.mod.common.api.cooking.Seasoning
import com.cobblemon.mod.common.api.cooking.Seasonings
import com.cobblemon.mod.common.api.fishing.FishingBait
import com.cobblemon.mod.common.api.fishing.FishingBaits
import com.cobblemon.mod.common.item.components.CookingComponent
import com.cobblemon.mod.common.util.playSoundServer
import com.cobblemon.mod.common.util.toBlockPos
import com.cobblemon.mod.common.util.toVec3d
import net.minecraft.client.Minecraft
import net.minecraft.client.resources.sounds.SimpleSoundInstance
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

        if (menu is CookingPotMenu) {
            val cookingComponent = menu.createCookingComponentFromSlots()

            // Attach the CookingComponent to the result stack
            stack.set(CobblemonItemComponents.COOKING_COMPONENT, cookingComponent)

            menu.consumeCraftingIngredients() // Decrement ingredients
            menu.broadcastChanges() // Notify the client

            Minecraft.getInstance().soundManager.play(SimpleSoundInstance.forUI(CobblemonSounds.CAMPFIRE_POT_CRAFT, 1.0f, 1.0f))
        } else {
            println("Player menu is not CookingPotMenu!")
        }

        super.onTake(player, stack)
    }

    override fun mayPlace(stack: ItemStack): Boolean {
        return false
    }
}