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
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.entity.player.StackedContents
import net.minecraft.world.inventory.*
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.CraftingInput
import net.minecraft.world.item.crafting.RecipeHolder
import net.minecraft.world.item.crafting.RecipeType
import net.minecraft.world.level.Level

class CookingPotMenu : RecipeBookMenu<CraftingInput, CookingPotRecipe>, ContainerListener {

    private val containerId: Int
    private val player: Player
    private val level: Level
    private val playerInventory: Inventory
    private val container: CraftingContainer
    private val resultContainer: ResultContainer
    private val containerData: ContainerData
    private val recipeType: RecipeType<CookingPotRecipe> = CobblemonRecipeTypes.COOKING_POT_COOKING

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

        addSlot(CookingPotResultSlot(playerInventory.player, resultContainer, RESULT_SLOT, 124 + craftingOutputOffsetX, 35 + craftingOutputOffsetY))
        println("Initialized result slot at index 0")

        for (i in 0..2) {
            for (j in 0..2) {
                val slotIndex = j + i * 3
                val containerIndex = slotIndex + 1
                addSlot(Slot(this.container, containerIndex, 44 + j * 18, 27 + i * 18))
                println("Initialized crafting slot Grid[$i, $j] -> container index $containerIndex")
            }
        }

        for (i in 0..2) {
            for (j in 0..8) {
                val index = j + i * 9 + 9
                val x = 8 + j * 18
                val y = 100 + i * 18
                addSlot(Slot(playerInventory, index, x, y))
                println("Initialized player inventory slot $index at ($x, $y)")
            }
        }

        for (i in 0..8) {
            val x = 8 + i * 18
            val y = 158
            addSlot(Slot(playerInventory, i, x, y))
            println("Initialized hotbar slot $i at ($x, $y)")
        }
    }

    fun canCook(): Boolean {
        val optionalRecipe = this.level.recipeManager.getRecipeFor(
            recipeType,
            container.asCraftInput(),
            this.level
        )

        // Debugging: Log grid contents
        for (i in 1..9) {
            println("Crafting slot $i: ${container.getItem(i).item} (${container.getItem(i).count})")
        }

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

    override fun recipeMatches(recipe: RecipeHolder<CookingPotRecipe?>): Boolean {
        val matches = recipe.value()?.matches(this.container.asCraftInput(), this.player.level()) == true
        return matches
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
        return 10
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
        return return slotIndex != this.resultSlotIndex
    }

    override fun quickMoveStack(
        player: Player,
        index: Int
    ): ItemStack? {
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