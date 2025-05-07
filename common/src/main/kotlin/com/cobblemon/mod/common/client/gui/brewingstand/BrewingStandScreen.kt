package com.cobblemon.mod.common.client.gui.brewingstand

import com.cobblemon.mod.common.block.brewingstand.BrewingStandMenu
import com.cobblemon.mod.common.item.crafting.brewingstand.BrewingStandInput
import com.cobblemon.mod.common.item.crafting.brewingstand.BrewingStandRecipe
import com.cobblemon.mod.common.mixin.accessor.RecipeBookComponentAccessor
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.ImageButton
import net.minecraft.client.gui.components.StateSwitchingButton
import net.minecraft.client.gui.components.Tooltip
import net.minecraft.client.gui.components.WidgetSprites
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.Mth
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.inventory.RecipeBookMenu

class BrewingStandScreen(
    menu: BrewingStandMenu,
    playerInventory: Inventory,
    title: Component
) : AbstractContainerScreen<BrewingStandMenu>(menu, playerInventory, title), RecipeUpdateListener {

    companion object {
        private const val BACKGROUND_HEIGHT = 166
        private const val BACKGROUND_WIDTH = 176

        private val FUEL_LENGTH_SPRITE = ResourceLocation.withDefaultNamespace("container/brewing_stand/fuel_length")
        private val BREW_PROGRESS_SPRITE =
            ResourceLocation.withDefaultNamespace("container/brewing_stand/brew_progress")
        private val BUBBLES_SPRITE = ResourceLocation.withDefaultNamespace("container/brewing_stand/bubbles")
        private val BREWING_STAND_LOCATION =
            ResourceLocation.withDefaultNamespace("textures/gui/container/brewing_stand.png")
        private val BUBBLE_LENGTHS = intArrayOf(29, 24, 20, 16, 11, 6, 0)
        private val FILTER_BUTTON_SPRITES = WidgetSprites(
            ResourceLocation.withDefaultNamespace("recipe_book/campfire_pot_filter_enabled"),
            ResourceLocation.withDefaultNamespace("recipe_book/campfire_pot_filter_disabled"),
            ResourceLocation.withDefaultNamespace("recipe_book/campfire_pot_filter_enabled_highlighted"),
            ResourceLocation.withDefaultNamespace("recipe_book/campfire_pot_filter_disabled_highlighted")
        )

    }

    private val recipeBookComponent: RecipeBookComponent = RecipeBookComponent()
    private var widthTooNarrow: Boolean = false

    override fun containerTick() {
        super.containerTick()
        this.recipeBookComponent.tick()
    }

    override fun init() {
        super.init()
        titleLabelX = (imageWidth - font.width(title)) / 2
        this.recipeBookComponent.init(
            this.width,
            this.height,
            this.minecraft!!,
            this.widthTooNarrow,
            this.menu as RecipeBookMenu<BrewingStandInput, BrewingStandRecipe>
        )
        
        if (this.recipeBookComponent.isVisible) {
            val recipeBookFilterButton = (this.recipeBookComponent as RecipeBookComponentAccessor).filterButton
            overrideRecipeBookFilterButton(recipeBookFilterButton)
        }
        this.leftPos = this.recipeBookComponent.updateScreenPosition(this.width, this.imageWidth)
        val topPos = ((height - BACKGROUND_HEIGHT) / 2)

        val recipeBookButton = ImageButton(
            this.leftPos + 5, topPos + 35, 20, 18, RecipeBookComponent.RECIPE_BUTTON_SPRITES
        ) { button ->
            this.recipeBookComponent.toggleVisibility()
            if (this.recipeBookComponent.isVisible) overrideRecipeBookFilterButton((this.recipeBookComponent as RecipeBookComponentAccessor).filterButton)
            this.leftPos = this.recipeBookComponent.updateScreenPosition(this.width, this.imageWidth)
            button.setPosition(this.leftPos + 5, topPos + 35)
        }

        this.addWidget(this.recipeBookComponent)
        this.addRenderableWidget(recipeBookButton)
    }

    private fun overrideRecipeBookFilterButton(filterButton: StateSwitchingButton) {
        filterButton.initTextureValues(FILTER_BUTTON_SPRITES)
        filterButton.setTooltip(
            if (filterButton.isStateTriggered()) Tooltip.create(Component.translatable("cobblemon.container.brewing_stand.recipe_book.toggle_recipes"))
            else Tooltip.create(Component.translatable("gui.recipebook.toggleRecipes.all"))
        )
    }

    override fun render(context: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        super.render(context, mouseX, mouseY, partialTick)
        if (this.recipeBookComponent.isVisible && this.widthTooNarrow) {
            this.renderBackground(context, mouseX, mouseY, partialTick)
            this.recipeBookComponent.render(context, mouseX, mouseY, partialTick)
        } else {
            super.render(context, mouseX, mouseY, partialTick)
            this.recipeBookComponent.render(context, mouseX, mouseY, partialTick)
            this.recipeBookComponent.renderGhostRecipe(context, this.leftPos, this.topPos, true, partialTick)
        }

        renderTooltip(context, mouseX, mouseY)
        this.recipeBookComponent.renderTooltip(context, this.leftPos, this.topPos, mouseX, mouseY)

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
            var brewHeight = (28.0f * (1.0f - brewingTicks / 400.0f)).toInt()
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

    override fun recipesUpdated() {
        this.recipeBookComponent.recipesUpdated()
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (this.recipeBookComponent.mouseClicked(mouseX, mouseY, button)) {
            this.focused = this.recipeBookComponent

            if (this.recipeBookComponent.isVisible) {
                val recipeBookFilterButton = (this.recipeBookComponent as RecipeBookComponentAccessor).filterButton
                if (recipeBookFilterButton.isMouseOver(mouseX, mouseY)) overrideRecipeBookFilterButton(recipeBookFilterButton)
            }

            return true
        } else {
            return if (this.widthTooNarrow  && this.recipeBookComponent.isVisible) true else super.mouseClicked(mouseX, mouseY, button)
        }
    }


    override fun getRecipeBookComponent(): RecipeBookComponent = this.recipeBookComponent
}
