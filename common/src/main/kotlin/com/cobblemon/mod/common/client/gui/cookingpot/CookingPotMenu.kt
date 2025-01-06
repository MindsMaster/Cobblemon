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
import net.minecraft.recipebook.ServerPlaceRecipe
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.entity.player.StackedContents
import net.minecraft.world.inventory.*
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.crafting.*
import net.minecraft.world.level.Level
import java.util.*

class CookingPotMenu : RecipeBookMenu<CraftingInput, CookingPotRecipeBase>, ContainerListener {

    private val containerId: Int
    private val player: Player
    private val level: Level
    private val playerInventory: Inventory
    private val container: CraftingContainer
    private val resultContainer: ResultContainer
    private val containerData: ContainerData
    private val recipeType: RecipeType<CookingPotRecipe> = CobblemonRecipeTypes.COOKING_POT_COOKING
    private val quickCheck = RecipeManager.createCheck(CobblemonRecipeTypes.COOKING_POT_COOKING)


    val RESULT_SLOT = 0;

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

        //println("Initialized player inventory for player:")
        println(playerInventory.player.name.string)

        addSlot(CookingPotResultSlot(playerInventory.player, resultContainer, RESULT_SLOT, 124 + craftingOutputOffsetX, 35 + craftingOutputOffsetY))
        //println("Initialized result slot at index 0")

        // Crafting Grid Slots (Indices 1–9)
        for (i in 0..2) {
            for (j in 0..2) {
                val slotIndex = j + i * 3 + 1 // Indices start at 1
                addSlot(Slot(this.container, slotIndex, 44 + j * 18, 27 + i * 18))
                //println("Initialized crafting slot Grid[$i, $j] -> container index $slotIndex")
            }
        }

        // Extra Slots (Indices 10–12)
        for (j in 0..2) {
            val slotIndex = 10 + j
            addSlot(Slot(this.container, slotIndex, 44 + j * 18, 1)) // Adjust Y-coordinate as needed
            //println("Initialized additional slot $slotIndex at (${44 + j * 18}, 9)")
        }

        // Player Inventory Slots (Indices 13–39)
        for (i in 0..2) {
            for (j in 0..8) {
                val slotIndex = 13 + j + i * 9
                val x = 8 + j * 18
                val y = 100 + i * 18
                addSlot(Slot(playerInventory, slotIndex - 13 + 9, x, y)) // Adjust to map to player's inventory indices
                //println("Initialized player inventory slot $slotIndex at ($x, $y)")
            }
        }

        // Hotbar Slots (Indices 40–48)
        for (i in 0..8) {
            val slotIndex = 40 + i
            val x = 8 + i * 18
            val y = 158
            addSlot(Slot(playerInventory, i, x, y)) // Direct mapping to hotbar indices
            //println("Initialized hotbar slot $slotIndex at ($x, $y)")
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
        //println("Debug: updateResultSlot called")

        // Create crafting input from the crafting grid items
        val craftingInput = CraftingInput.of(3, 3, container.items.subList(1, 10))
        //println("Debug: Crafting input:")
        craftingInput.items().forEachIndexed { index, item ->
            //println(" - Slot $index: ${item.item} x ${item.count}")
        }

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
            //println("Debug: Matching recipe found of type ${recipe::class.simpleName}")

            val result = recipe.assemble(craftingInput, level.registryAccess())
            if (!result.isEmpty) {
                resultContainer.setItem(0, result)
                container.setItem(0, result) // Update result in the container
                //println("Debug: Result slot updated with ${result.item} x ${result.count}")
            } else {
                //println("Debug: Result is empty")
                resultContainer.setItem(0, ItemStack.EMPTY)
                container.setItem(0, ItemStack.EMPTY)
            }
        } else {
            //println("Debug: No matching recipe found")
            resultContainer.setItem(0, ItemStack.EMPTY)
            container.setItem(0, ItemStack.EMPTY) // Clear result slot
        }

        broadcastChanges() // Notify client of the updates
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
        TODO("Not yet implemented")
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
        if (dataSlotIndex in 0..13) { // Check if a crafting grid slot changed
            updateResultSlot()
        }

        this.resultContainer.setItem(0, container.items[RESULT_SLOT])
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
}