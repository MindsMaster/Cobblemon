/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui.cookingpot


import com.cobblemon.mod.common.CobblemonMenuType
import com.cobblemon.mod.common.CobblemonRecipeTypes
import com.cobblemon.mod.common.api.cooking.Seasonings
import com.cobblemon.mod.common.block.entity.CampfireBlockEntity.Companion.CRAFTING_GRID_SLOTS
import com.cobblemon.mod.common.block.entity.CampfireBlockEntity.Companion.PLAYER_HOTBAR_SLOTS
import com.cobblemon.mod.common.block.entity.CampfireBlockEntity.Companion.PLAYER_INVENTORY_SLOTS
import com.cobblemon.mod.common.block.entity.CampfireBlockEntity.Companion.PREVIEW_ITEM_SLOT
import com.cobblemon.mod.common.block.entity.CampfireBlockEntity.Companion.RESULT_SLOT
import com.cobblemon.mod.common.block.entity.CampfireBlockEntity.Companion.SEASONING_SLOTS
import net.minecraft.recipebook.ServerPlaceRecipe
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.entity.player.StackedContents
import net.minecraft.world.inventory.*
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.CraftingInput
import net.minecraft.world.item.crafting.RecipeHolder
import net.minecraft.world.item.crafting.RecipeManager
import net.minecraft.world.item.crafting.RecipeType
import net.minecraft.world.level.Level

class CookingPotMenu : RecipeBookMenu<CraftingInput, CookingPotRecipeBase>, ContainerListener {
    private val player: Player
    private val level: Level
    private val playerInventory: Inventory
    private val container: CraftingContainer
    private val resultContainer: ResultContainer
    val containerData: ContainerData
    private val recipeType: RecipeType<CookingPotRecipe> = CobblemonRecipeTypes.COOKING_POT_COOKING
    private val quickCheck = RecipeManager.createCheck(CobblemonRecipeTypes.COOKING_POT_COOKING)

    constructor(containerId: Int, playerInventory: Inventory) : super(CobblemonMenuType.COOKING_POT, containerId) {
        this.playerInventory = playerInventory
        this.container = CookingPotContainer(this, 3, 3)
        this.resultContainer = ResultContainer()
        this.containerData = SimpleContainerData(3)
        this.addDataSlots(containerData)
        this.player = playerInventory.player
        this.level = playerInventory.player.level()
        initializeSlots(playerInventory)
    }

    constructor(containerId: Int, playerInventory: Inventory, container: CraftingContainer, containerData: ContainerData) : super(CobblemonMenuType.COOKING_POT, containerId) {
        this.playerInventory = playerInventory
        this.container = container
        this.containerData = containerData
        this.addDataSlots(containerData)
        this.resultContainer = ResultContainer()
        this.player = playerInventory.player
        this.level = playerInventory.player.level()
        container.startOpen(playerInventory.player)
        initializeSlots(playerInventory)
        this.addSlotListener(this)
    }

    private fun initializeSlots(playerInventory: Inventory) {
        val craftingOutputOffsetX = 16
        val craftingOutputOffsetY = 10

        val resultSlotX = 124 + craftingOutputOffsetX
        val resultSlotY = 51 + craftingOutputOffsetY

        // Result slot
        addSlot(CookingPotResultSlot(this.container, RESULT_SLOT, resultSlotX, resultSlotY))

        // Crafting Grid Slots (Indices 1–9)
        for (i in 0..2) {
            for (j in 0..2) {
                val slotIndex = j + i * 3 + 1 // Indices start at 1
                addSlot(Slot(this.container, slotIndex, 44 + j * 18, 43 + i * 18))
            }
        }

        // Seasoning Slots (Indices 10–12)
        for (j in 0..2) {
            val slotIndex = 10 + j
            addSlot(SeasoningSlot(this.container, slotIndex, 44 + j * 18, 17))
        }

        // Preview slot
        addSlot(CookingPotPreviewSlot(this.container, PREVIEW_ITEM_SLOT, resultSlotX, resultSlotY))

        // Player Inventory Slots (Indices 13–39)
        for (i in 0..2) {
            for (j in 0..8) {
                val slotIndex = 14 + j + i * 9
                val x = 8 + j * 18
                val y = 116 + i * 18
                addSlot(Slot(playerInventory, slotIndex - 14 + 9, x, y))
            }
        }

        // Hotbar Slots (Indices 40–48)
        for (i in 0..8) {
            val x = 8 + i * 18
            val y = 174
            addSlot(Slot(playerInventory, i, x, y))
        }
    }

    override fun broadcastChanges() {
        super.broadcastChanges()
    }

    override fun handlePlacement(placeAll: Boolean, recipe: RecipeHolder<*>, player: ServerPlayer) {
        // Check if the recipe value implements CookingPotRecipeBase
        val recipeValue = recipe.value()
        if (recipeValue is CookingPotRecipeBase) {
            @Suppress("UNCHECKED_CAST")
            val castedRecipe = recipe as RecipeHolder<CookingPotRecipeBase>
            this.beginPlacingRecipe()
            try {
                val serverPlaceRecipe = ServerPlaceRecipe(this)
                serverPlaceRecipe.recipeClicked(player, castedRecipe, placeAll)
            } finally {
                this.finishPlacingRecipe(castedRecipe)
            }
        } else {
            throw IllegalArgumentException("Unsupported recipe type: ${recipeValue::class.java.name}")
        }
    }

    override fun removed(player: Player) {
        super.removed(player)
    }

    override fun fillCraftSlotsStackedContents(itemHelper: StackedContents) {
        this.container.fillStackedContents(itemHelper)
    }

    override fun clearCraftingContent() {
        container.clearContent()
    }

    override fun recipeMatches(recipe: RecipeHolder<CookingPotRecipeBase>): Boolean {
        val recipeValue = recipe.value()
        return if (recipeValue is CookingPotRecipeBase) {
            recipeValue.matches(container.asCraftInput(), level)
        } else {
            false
        }
    }

    override fun getResultSlotIndex(): Int {
        return RESULT_SLOT
    }

    override fun getGridWidth(): Int {
        return 3
    }

    override fun getGridHeight(): Int {
        return 3
    }

    override fun getSize(): Int {
        return 13
    }

    fun getBurnProgress(): Float {
        val i = this.containerData.get(0)
        val j = this.containerData.get(1)
        return if (j != 0 && i != 0) {
            Math.clamp((i.toFloat() / j.toFloat()), 0.0F, 1.0F)
        } else {
            0.0F;
        }
    }

    override fun getRecipeBookType(): RecipeBookType {
        return RecipeBookType.valueOf("COOKING_POT")
    }

    override fun shouldMoveToInventory(slotIndex: Int): Boolean {
        return slotIndex != this.resultSlotIndex
    }

    override fun quickMoveStack(
        player: Player,
        index: Int
    ): ItemStack {
        var itemStack = ItemStack.EMPTY
        val slot = slots[index]

        if (slot.hasItem()) {
            val slotItemStack = slot.item
            itemStack = slotItemStack.copy()

            if (index == RESULT_SLOT) {
                if (!this.moveItemStackTo(slotItemStack, PLAYER_INVENTORY_SLOTS.first, PLAYER_HOTBAR_SLOTS.last + 1, false)) {
                    return ItemStack.EMPTY
                }

                slot.onQuickCraft(slotItemStack, itemStack);
            } else if (index in PLAYER_INVENTORY_SLOTS || index in PLAYER_HOTBAR_SLOTS) {
                if (Seasonings.isSeasoning(slotItemStack)) {
                    if (!this.moveItemStackTo(slotItemStack, SEASONING_SLOTS.first, SEASONING_SLOTS.last + 1, false) &&
                        !this.moveItemStackTo(slotItemStack, CRAFTING_GRID_SLOTS.first, CRAFTING_GRID_SLOTS.last + 1, false)
                    ) {
                        return ItemStack.EMPTY
                    }
                } else if (!this.moveItemStackTo(slotItemStack, CRAFTING_GRID_SLOTS.first, CRAFTING_GRID_SLOTS.last + 1, false)) {
                    return ItemStack.EMPTY
                }
            } else if (index in CRAFTING_GRID_SLOTS || index in SEASONING_SLOTS) {
                if (!this.moveItemStackTo(slotItemStack, PLAYER_INVENTORY_SLOTS.first, PLAYER_HOTBAR_SLOTS.last + 1, false)) {
                    return ItemStack.EMPTY
                }
            }

            if (slotItemStack.isEmpty) {
                slot.setByPlayer(ItemStack.EMPTY)
            } else {
                slot.setChanged()
            }

            if (slotItemStack.count == itemStack.count) {
                return ItemStack.EMPTY
            }

            slot.onTake(player, slotItemStack)
        }

        return itemStack
    }

    override fun stillValid(player: Player): Boolean {
        return this.container.stillValid(player)
    }

    override fun slotChanged(containerToSend: AbstractContainerMenu, dataSlotIndex: Int, stack: ItemStack) {
        broadcastChanges()
    }

    override fun dataChanged(
        containerMenu: AbstractContainerMenu,
        dataSlotIndex: Int,
        value: Int
    ) {
    }
}