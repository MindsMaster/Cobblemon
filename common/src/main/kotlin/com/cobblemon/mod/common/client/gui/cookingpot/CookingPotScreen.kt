package com.cobblemon.mod.common.client.gui.cookingpot

import com.cobblemon.mod.common.api.gui.blitk
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener
import net.minecraft.client.gui.screens.recipebook.SmeltingRecipeBookComponent
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.player.Inventory

class CookingPotScreen(
    menu: CookingPotMenu,
    playerInventory: Inventory,
    title: Component
) : AbstractContainerScreen<CookingPotMenu>(menu, playerInventory, title), RecipeUpdateListener {

    companion object {
        // Size of Background
        private const val backgroundHeight = 198
        private const val backgroundWidth = 178


        private val BACKGROUND = cobblemonResource("textures/gui/cookingpot/cooking_pot.png")
    }

    override fun renderBg(
        context: GuiGraphics,
        partialTick: Float,
        mouseX: Int,
        mouseY: Int
    ) {
        blitk(
            matrixStack = context.pose(),
            texture = BACKGROUND,
            x = (width - backgroundWidth) / 2, y = (height - backgroundHeight) / 2,
            width = backgroundWidth, height = backgroundHeight
        )
    }

    override fun recipesUpdated() {
    }

    override fun getRecipeBookComponent(): RecipeBookComponent? {
        return SmeltingRecipeBookComponent()
    }


}