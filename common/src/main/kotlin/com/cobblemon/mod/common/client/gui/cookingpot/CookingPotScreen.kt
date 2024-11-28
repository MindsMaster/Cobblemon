/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui.cookingpot

import com.cobblemon.mod.common.api.gui.blitk
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.ImageButton
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.inventory.ClickType
import net.minecraft.world.inventory.RecipeBookMenu
import net.minecraft.world.inventory.Slot

class CookingPotScreen : AbstractContainerScreen<CookingPotMenu>, RecipeUpdateListener {

    companion object {
        private const val backgroundHeight = 198
        private const val backgroundWidth = 176
        private val BACKGROUND = cobblemonResource("textures/gui/cookingpot/cooking_pot.png")
    }

    private val recipeBookComponent: RecipeBookComponent = RecipeBookComponent()
    private var widthTooNarrow : Boolean = false

    constructor(menu: CookingPotMenu, playerInventory: Inventory, title: Component) : super(
        menu, playerInventory,
        Component.translatable("cobblemon.container.cooking_pot")
    )

    override fun containerTick() {
        super.containerTick()
        this.recipeBookComponent.tick()
    }

    override fun init() {
        super.init()
        this.widthTooNarrow = this.width < 379
        this.recipeBookComponent.init(this.width, this.height, this.minecraft!!, this.widthTooNarrow, this.menu)
        this.leftPos = this.recipeBookComponent.updateScreenPosition(this.width, this.imageWidth)
        val recipeBookButton = ImageButton(
            this.leftPos + 10, this.height / 2 - 35, 20, 18, RecipeBookComponent.RECIPE_BUTTON_SPRITES
        ) { button ->
            this.recipeBookComponent.toggleVisibility()
            this.leftPos = this.recipeBookComponent.updateScreenPosition(this.width, this.imageWidth)
            button.setPosition(this.leftPos + 10, this.height / 2 - 35)
        }
        addRenderableWidget(recipeBookButton)
        addWidget(recipeBookComponent)

        this.titleLabelX = ((this.imageWidth - this.font.width(this.title)) / 2) - 17
        this.titleLabelY = - 10
        this.inventoryLabelX = 8
        this.inventoryLabelY = this.imageHeight - 78

        Minecraft.getInstance().player?.recipeBook?.collections?.forEach { t -> println(t.recipes) }

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
            x = leftPos, y = (height - backgroundHeight) / 2,
            width = backgroundWidth, height = backgroundHeight
        )
    }

    override fun render(
        context: GuiGraphics,
        mouseX: Int,
        mouseY: Int,
        partialTicks: Float
    ) {
        if (this.recipeBookComponent.isVisible && this.widthTooNarrow) {
            this.renderBackground(context, mouseX, mouseY, partialTicks)
            this.recipeBookComponent.render(context, mouseX, mouseY, partialTicks)
        } else {
            super.render(context, mouseX, mouseY, partialTicks)
            this.recipeBookComponent.render(context, mouseX, mouseY, partialTicks)
            this.recipeBookComponent.renderGhostRecipe(context, this.leftPos, this.topPos, true, partialTicks)
        }
        this.renderTooltip(context, mouseX, mouseY)
        this.recipeBookComponent.renderTooltip(context, this.leftPos, this.topPos, mouseX, mouseY)
    }

    override fun recipesUpdated() {
        this.recipeBookComponent.recipesUpdated()
    }

    override fun getRecipeBookComponent(): RecipeBookComponent? {
        return this.recipeBookComponent
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (this.recipeBookComponent.mouseClicked(mouseX, mouseY, button)) {
            this.focused = this.recipeBookComponent
            return true
        } else {
            return if (this.widthTooNarrow  && this.recipeBookComponent.isVisible) {
                true
            } else {
                super.mouseClicked(mouseX, mouseY, button)
            }
        }
    }

    override fun slotClicked(slot: Slot?, slotId: Int, mouseButton: Int, type: ClickType) {
        super.slotClicked(slot, slotId, mouseButton, type)
        recipeBookComponent.slotClicked(slot)
    }


}