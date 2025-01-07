/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui.cookingpot


import com.cobblemon.mod.common.CobblemonItemComponents
import com.cobblemon.mod.common.CobblemonMenuType
import com.cobblemon.mod.common.CobblemonRecipeTypes
import com.cobblemon.mod.common.api.cooking.Seasoning
import com.cobblemon.mod.common.api.cooking.Seasonings
import com.cobblemon.mod.common.api.fishing.FishingBait
import com.cobblemon.mod.common.api.fishing.FishingBaits
import com.cobblemon.mod.common.item.components.CookingComponent
import net.minecraft.recipebook.ServerPlaceRecipe
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.Container
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.entity.player.StackedContents
import net.minecraft.world.inventory.*
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.crafting.CraftingInput
import net.minecraft.world.item.crafting.RecipeHolder
import net.minecraft.world.item.crafting.RecipeManager
import net.minecraft.world.item.crafting.RecipeType
import net.minecraft.world.level.Level
import java.util.*

class CookingPotMenu : RecipeBookMenu<CraftingInput, CookingPotRecipeBase>, ContainerListener {

    companion object {
        const val RESULT_SLOT = 0;
        val CRAFTING_GRID_SLOTS = 1..9
        val SEASONING_SLOTS = 10..12
        val PLAYER_INVENTORY_SLOTS = 13..39
        val PLAYER_HOTBAR_SLOTS = 40..48
    }

    private val containerId: Int
    private val player: Player
    private val level: Level
    private val playerInventory: Inventory
    private val container: CraftingContainer
    private val resultContainer: ResultContainer
    private val containerData: ContainerData
    private val recipeType: RecipeType<CookingPotRecipe> = CobblemonRecipeTypes.COOKING_POT_COOKING
    private val quickCheck = RecipeManager.createCheck(CobblemonRecipeTypes.COOKING_POT_COOKING)

    constructor(containerId: Int, playerInventory: Inventory) :
            super(CobblemonMenuType.COOKING_POT, containerId) {
        this.containerId = containerId
        this.playerInventory = playerInventory
        this.container = CookingPotContainer(this, 3, 3)
        this.resultContainer = ResultContainer()
        this.resultContainer.setItem(0, container.getItem(RESULT_SLOT))
        this.containerData = SimpleContainerData(4)
        this.player = playerInventory.player
        this.level = playerInventory.player.level()
        initializeSlots(playerInventory)
    }

    constructor(containerId: Int, playerInventory: Inventory, container: CraftingContainer, containerData: ContainerData) :
            super(CobblemonMenuType.COOKING_POT, containerId) {
        this.containerId = containerId
        this.playerInventory = playerInventory
        this.container = container
        this.containerData = containerData
        this.resultContainer = ResultContainer()
        this.resultContainer.setItem(0, container.getItem(RESULT_SLOT))
        this.player = playerInventory.player
        this.level = playerInventory.player.level()
        container.startOpen(playerInventory.player)
        initializeSlots(playerInventory)
        this.addSlotListener(this)
    }

    private fun initializeSlots(playerInventory: Inventory) {
        val craftingOutputOffsetX = 16
        val craftingOutputOffsetY = 10

        // Result slot
        addSlot(CookingPotResultSlot(playerInventory.player, resultContainer, RESULT_SLOT, 124 + craftingOutputOffsetX, 51 + craftingOutputOffsetY))

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

        // Player Inventory Slots (Indices 13–39)
        for (i in 0..2) {
            for (j in 0..8) {
                val slotIndex = 13 + j + i * 9
                val x = 8 + j * 18
                val y = 116 + i * 18
                addSlot(Slot(playerInventory, slotIndex - 13 + 9, x, y))
            }
        }

        // Hotbar Slots (Indices 40–48)
        for (i in 0..8) {
            val x = 8 + i * 18
            val y = 174
            addSlot(Slot(playerInventory, i, x, y))
        }

        updateResultSlot()
    }

    fun consumeCraftingIngredients() {
        for (i in 1..13) {
            val itemInSlot = container.getItem(i)
            if (!itemInSlot.isEmpty) {
                when (itemInSlot.item) {
                    Items.LAVA_BUCKET, Items.WATER_BUCKET, Items.MILK_BUCKET -> {
                        // Replace with empty bucket
                        container.setItem(i, ItemStack(Items.BUCKET))
                    }
                    Items.HONEY_BOTTLE -> {
                        // Replace with empty glass bottle
                        container.setItem(i, ItemStack(Items.GLASS_BOTTLE))
                    }
                    else -> {
                        // Decrease the stack size by 1
                        itemInSlot.shrink(1)
                        if (itemInSlot.count <= 0) {
                            container.setItem(i, ItemStack.EMPTY) // Clear the slot if empty
                        }
                    }
                }
            }
        }
        broadcastChanges() // Notify the client of changes
    }



    override fun broadcastChanges() {
        super.broadcastChanges()
        /*slots.forEachIndexed { index, slot ->
            println("Slot $index contains: ${slot.item}")
        }*/
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

    private fun updateResultSlot() {
        val craftingInput = CraftingInput.of(3, 3, container.items.subList(1, 10))

        fun <T : CookingPotRecipeBase> fetchRecipe(
                recipeType: RecipeType<T>
        ): Optional<RecipeHolder<CookingPotRecipeBase>> {
            val optional = level.recipeManager.getRecipeFor(recipeType, craftingInput, level)
            @Suppress("UNCHECKED_CAST")
            return optional.map { it as RecipeHolder<CookingPotRecipeBase> }
        }

        // Check for both COOKING_POT_COOKING and COOKING_POT_SHAPELESS recipes
        val optionalRecipe = fetchRecipe(CobblemonRecipeTypes.COOKING_POT_COOKING)
                .or { fetchRecipe(CobblemonRecipeTypes.COOKING_POT_SHAPELESS) }

        if (optionalRecipe.isPresent) {
            val recipe = optionalRecipe.get().value()
            val result = recipe.assemble(craftingInput, level.registryAccess())

            if (!result.isEmpty) {
                // Makes dynamic rendering and tooltips work
                result.set(CobblemonItemComponents.COOKING_COMPONENT, createCookingComponentFromSlots())

                resultContainer.setItem(0, result)
                container.setItem(0, result) // Update result in the container
            } else {
                resultContainer.setItem(0, ItemStack.EMPTY)
                container.setItem(0, ItemStack.EMPTY)
            }
        } else {
            resultContainer.setItem(0, ItemStack.EMPTY)
            container.setItem(0, ItemStack.EMPTY) // Clear result slot
        }

        broadcastChanges() // Notify client of the updates
    }

    fun createCookingComponentFromSlots(): CookingComponent {
        // Variables to store bait and seasoning
        var bait1 = FishingBait.BLANK_BAIT
        var bait2 = FishingBait.BLANK_BAIT
        var bait3 = FishingBait.BLANK_BAIT
        var seasoning1 = Seasoning.BLANK_SEASONING
        var seasoning2 = Seasoning.BLANK_SEASONING
        var seasoning3 = Seasoning.BLANK_SEASONING

        // Iterate through slots 10-12
        for ((index, slot) in (10..12).withIndex()) {
            val itemInSlot = getSlot(slot).item

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
        return CookingComponent(
            bait1 = bait1,
            bait2 = bait2,
            bait3 = bait3,
            seasoning1 = seasoning1,
            seasoning2 = seasoning2,
            seasoning3 = seasoning3
        )
    }


    fun canCook(): Boolean {
        val optionalRecipe = this.level.recipeManager.getRecipeFor(
            recipeType,
            container.asCraftInput(),
            this.level
        )

        return optionalRecipe.isPresent
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
            Math.clamp((i / j).toFloat(), 0.0F, 1.0F)
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
        /*if (dataSlotIndex == RESULT_SLOT && !stack.isEmpty) {
            // Clear crafting slots after result is taken
            for (i in 1..9) { // Assuming crafting slots are indices 1 to 9
                val craftingItem = container.getItem(i)
                if (!craftingItem.isEmpty) {
                    craftingItem.shrink(1) // Decrease count by 1
                    if (craftingItem.count <= 0) {
                        container.setItem(i, ItemStack.EMPTY) // Clear slot if empty
                    }
                }
            }
            container.setChanged() // Notify container of changes
        }*/
        if (dataSlotIndex in 1..13) { // Check if a crafting grid slot changed
            updateResultSlot()
            this.resultContainer.setItem(0, container.items[RESULT_SLOT])
        }
    }




    /*override fun slotChanged(
        containerToSend: AbstractContainerMenu,
        dataSlotIndex: Int,
        stack: ItemStack
    ) {
        println("Slot changed - Index: $dataSlotIndex, Item: ${stack.item}, Count: ${stack.count}")

        if (dataSlotIndex in 1..9) { // Crafting grid slots
            val optionalRecipe = this.level.recipeManager.getRecipeFor(
                recipeType,
                container.asCraftInput(),
                this.level
            )

            if (optionalRecipe.isPresent) {
                val recipe = optionalRecipe.get()
                if (recipe is CookingPotRecipe) { // Ensure proper type
                    val result = recipe.assemble(this.container.asCraftInput(), this.level.registryAccess())
                    this.resultContainer.setItem(0, result ?: ItemStack.EMPTY)
                    println("Updated result slot with recipe output: ${result?.item}")
                } else {
                    println("Error: Recipe is not of type CookingPotRecipe.")
                }
            } else {
                this.resultContainer.setItem(0, ItemStack.EMPTY)
                println("Cleared result slot (no matching recipe).")
            }
        }
    }*/



    override fun dataChanged(
        containerMenu: AbstractContainerMenu,
        dataSlotIndex: Int,
        value: Int
    ) {
    }

    private class SeasoningSlot(container: Container, slot: Int, x: Int, y: Int) : Slot(container, slot, x, y) {
        override fun mayPlace(stack: ItemStack): Boolean = Seasonings.isSeasoning(stack) && super.mayPlace(stack)
    }
}