/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.block.campfirepot

import com.cobblemon.mod.common.CobblemonSounds
import net.minecraft.client.Minecraft
import net.minecraft.client.resources.sounds.SimpleSoundInstance
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.CraftingContainer
import net.minecraft.world.inventory.Slot
import net.minecraft.world.item.ItemStack

class CookingPotResultSlot(
    container: CraftingContainer,
    index: Int,
    x: Int,
    y: Int
) : Slot(container, index, x, y) {

    override fun onTake(player: Player, stack: ItemStack) {
        val menu = player.containerMenu

        if (menu is CookingPotMenu) {
            // val cookingComponent = menu.createCookingComponentFromSlots()

            // Attach the CookingComponent to the result stack
            // stack.set(CobblemonItemComponents.COOKING_COMPONENT, cookingComponent)

            // menu.consumeCraftingIngredients() // Decrement ingredients
            menu.broadcastChanges() // Notify the client

            Minecraft.getInstance().soundManager.play(SimpleSoundInstance.forUI(CobblemonSounds.CAMPFIRE_POT_USE, 1.0f, 1.0f))
        } else {
            println("Player menu is not CookingPotMenu!")
        }

        super.onTake(player, stack)
    }

    override fun mayPlace(stack: ItemStack): Boolean = false
    override fun isActive(): Boolean = hasItem()
}