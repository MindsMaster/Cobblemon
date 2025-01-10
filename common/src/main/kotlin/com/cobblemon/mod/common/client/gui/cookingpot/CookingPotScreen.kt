/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui.cookingpot

import com.cobblemon.mod.common.CobblemonNetwork.sendToServer
import com.cobblemon.mod.common.CobblemonSounds
import com.cobblemon.mod.common.api.gui.blitk
import com.cobblemon.mod.common.block.entity.CampfireBlockEntity.Companion.IS_LID_OPEN_INDEX
import com.cobblemon.mod.common.block.entity.CampfireBlockEntity.Companion.PREVIEW_ITEM_SLOT
import com.cobblemon.mod.common.net.messages.client.cooking.ToggleCookingPotLidPacket
import com.cobblemon.mod.common.util.cobblemonResource
import com.mojang.blaze3d.systems.RenderSystem
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.components.ImageButton
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener
import net.minecraft.client.resources.sounds.SimpleSoundInstance
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.inventory.RecipeBookMenu
import net.minecraft.world.inventory.Slot
import net.minecraft.world.item.crafting.CraftingInput
import kotlin.math.ceil

@Environment(EnvType.CLIENT)
class CookingPotScreen(
    menu: CookingPotMenu,
    playerInventory: Inventory,
    title: Component
) : AbstractContainerScreen<CookingPotMenu>(
    menu,
    playerInventory,
    Component.translatable("cobblemon.container.cooking_pot")
), RecipeUpdateListener {

    companion object {
        private val BACKGROUND = cobblemonResource("textures/gui/cookingpot/cooking_pot.png")
        private val BURN_PROGRESS_SPRITE = ResourceLocation.withDefaultNamespace("textures/gui/sprites/container/furnace/lit_progress.png")

        private const val BACKGROUND_HEIGHT = 198
        private const val BACKGROUND_WIDTH = 176
        private const val TOGGLE_LID_BUTTON_WIDTH = 58
        private const val TOGGLE_LID_BUTTON_HEIGHT = 16
    }

    private val recipeBookComponent: RecipeBookComponent = RecipeBookComponent()
    private var widthTooNarrow : Boolean = false

    private lateinit var toggleLidButton: Button

    init {
        this.imageWidth = BACKGROUND_WIDTH
        this.imageHeight = BACKGROUND_HEIGHT
    }

    override fun containerTick() {
        super.containerTick()
        this.recipeBookComponent.tick()
    }

    override fun init() {
        super.init()
        this.widthTooNarrow = this.width < 379
        this.recipeBookComponent.init(this.width, this.height, this.minecraft!!, this.widthTooNarrow, this.menu as RecipeBookMenu<CraftingInput, CookingPotRecipe>)
        this.leftPos = this.recipeBookComponent.updateScreenPosition(this.width, this.imageWidth)
        val recipeBookButton = ImageButton(
            this.leftPos + 10, this.height / 2 - 35, 20, 18, RecipeBookComponent.RECIPE_BUTTON_SPRITES
        ) { button ->
            this.recipeBookComponent.toggleVisibility()
            this.leftPos = this.recipeBookComponent.updateScreenPosition(this.width, this.imageWidth)
            button.setPosition(this.leftPos + 10, this.height / 2 - 35)
        }

        this.addWidget(this.recipeBookComponent)
        this.addRenderableWidget(recipeBookButton)

        this.titleLabelX = ((this.imageWidth - this.font.width(this.title)) / 2) - 17
        this.titleLabelY = 6
        this.inventoryLabelX = 8
        this.inventoryLabelY = this.imageHeight - 94

        if (::toggleLidButton.isInitialized) removeWidget(toggleLidButton)
        toggleLidButton = Button.builder(Component.literal(getToggleLidButtonText())) {
            val isLidOpen = menu.containerData.get(IS_LID_OPEN_INDEX) == 1
            toggleLidButton.message = Component.literal(getToggleLidButtonText())
            sendToServer(ToggleCookingPotLidPacket(!isLidOpen))
        }
        .size(TOGGLE_LID_BUTTON_WIDTH, TOGGLE_LID_BUTTON_HEIGHT)
        .build()

        addRenderableWidget(toggleLidButton)
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
            x = leftPos, y = (height - BACKGROUND_HEIGHT) / 2,
            width = BACKGROUND_WIDTH, height = BACKGROUND_HEIGHT
        )

        val burnProgress = ceil(menu.getBurnProgress() * 14.0).toInt()
        blitk(
            matrixStack = context.pose(),
            texture = BURN_PROGRESS_SPRITE,
            x = leftPos + 141,
            y = topPos + 86 + 14 - burnProgress,
            width = 14,
            height = burnProgress,
            vOffset = 14 - burnProgress,
            textureHeight = 14
        )
    }

    override fun render(
        context: GuiGraphics,
        mouseX: Int,
        mouseY: Int,
        partialTicks: Float
    ) {
        toggleLidButton.message = Component.literal(getToggleLidButtonText())
        toggleLidButton.setPosition(
            leftPos + BACKGROUND_WIDTH - TOGGLE_LID_BUTTON_WIDTH,
            topPos - TOGGLE_LID_BUTTON_HEIGHT
        )

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

    override fun renderSlot(guiGraphics: GuiGraphics, slot: Slot) {
        if (slot.index != PREVIEW_ITEM_SLOT) {
            super.renderSlot(guiGraphics, slot)
            return
        }

        RenderSystem.enableBlend()
        RenderSystem.setShaderColor(1f, 1f, 1f, 0.5f)

        guiGraphics.renderFakeItem(slot.item, slot.x, slot.y);
        guiGraphics.renderItemDecorations(minecraft!!.font, slot.item, slot.x, slot.y)

        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f)
        RenderSystem.disableBlend()
    }

    override fun recipesUpdated() {
        this.recipeBookComponent.recipesUpdated()
    }

    override fun getRecipeBookComponent(): RecipeBookComponent {
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

    override fun onClose() {
        Minecraft.getInstance().soundManager.play(SimpleSoundInstance.forUI(CobblemonSounds.CAMPFIRE_POT_CLOSE, 1.0f))
        super.onClose()
    }

    fun getToggleLidButtonText(): String {
        val isLidOpen = menu.containerData.get(IS_LID_OPEN_INDEX) == 1
        return if (isLidOpen) "Close Lid" else "Open Lid"
    }
}