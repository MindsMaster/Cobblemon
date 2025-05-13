package com.cobblemon.mod.common.client.gui.brewingstand

import com.cobblemon.mod.common.block.brewingstand.BrewingStandMenu
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.Mth
import net.minecraft.world.entity.player.Inventory

class BrewingStandScreen(
    menu: BrewingStandMenu,
    inventory: Inventory,
    title: Component
) : AbstractContainerScreen<BrewingStandMenu>(menu, inventory, title) {

    companion object {
        private val FUEL_LENGTH_SPRITE = ResourceLocation.withDefaultNamespace("container/brewing_stand/fuel_length")
        private val BREW_PROGRESS_SPRITE =
            ResourceLocation.withDefaultNamespace("container/brewing_stand/brew_progress")

        private val BUBBLES_SPRITE = ResourceLocation.withDefaultNamespace("container/brewing_stand/bubbles")
        private val BREWING_STAND_LOCATION =
            ResourceLocation.withDefaultNamespace("textures/gui/container/brewing_stand.png")

        private val BUBBLE_LENGTHS = intArrayOf(29, 24, 20, 16, 11, 6, 0)
    }

    override fun init() {
        super.init()
        titleLabelX = (imageWidth - font.width(title)) / 2
    }

    override fun render(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        super.render(guiGraphics, mouseX, mouseY, partialTick)
        renderTooltip(guiGraphics, mouseX, mouseY)
    }
    
    override fun renderBg(guiGraphics: GuiGraphics, partialTick: Float, mouseX: Int, mouseY: Int) {
        val i = (width - imageWidth) / 2
        val j = (height - imageHeight) / 2

        guiGraphics.blit(BREWING_STAND_LOCATION, i, j, 0, 0, imageWidth, imageHeight)

        val fuel = menu.getFuel()
        val fuelWidth = Mth.clamp((18 * fuel + 20 - 1) / 20, 0, 18)
        if (fuelWidth > 0) {
            guiGraphics.blitSprite(FUEL_LENGTH_SPRITE, 18, 4, 0, 0, i + 60, j + 44, fuelWidth, 4)
        }

        val brewingTicks = menu.getBrewingTicks()
        if (brewingTicks > 0) {
            val brewHeight = (28.0f * (1.0f - brewingTicks / 400.0f)).toInt()
            if (brewHeight > 0) {
                guiGraphics.blitSprite(BREW_PROGRESS_SPRITE, 9, 28, 0, 0, i + 97, j + 16, 9, brewHeight)
            }

            val bubbleHeight = BUBBLE_LENGTHS[brewingTicks / 2 % 7]
            if (bubbleHeight > 0) {
                guiGraphics.blitSprite(
                    BUBBLES_SPRITE,
                    12,
                    29,
                    0,
                    29 - bubbleHeight,
                    i + 63,
                    j + 14 + 29 - bubbleHeight,
                    12,
                    bubbleHeight
                )
            }
        }
    }
}