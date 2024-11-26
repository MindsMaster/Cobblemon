package com.cobblemon.mod.common.client.gui.cookingpot

import com.cobblemon.mod.common.CobblemonMenuType
import net.minecraft.world.SimpleContainer
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.entity.player.StackedContents
import net.minecraft.world.inventory.*
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.AbstractCookingRecipe
import net.minecraft.world.item.crafting.CraftingInput
import net.minecraft.world.item.crafting.RecipeHolder
import net.minecraft.world.item.crafting.SingleRecipeInput

class CookingPotMenu : RecipeBookMenu<SingleRecipeInput, AbstractCookingRecipe>{

    companion object {
        const val RESULT_ID = 0
        private const val INPUT_START = 1
        private const val INPUT_END = 10
        private const val INVENTORY_START = 10
        private const val INVENTORY_END = 37
        private const val HOTBAR_START = 37
        private const val HOTBAR_END = 46
    }

    private val containerId: Int
    private val playerInventory: Inventory
    private val input: CraftingContainer

    constructor(containerId: Int, playerInventory: Inventory) :
            super(CobblemonMenuType.COOKING_POT, containerId) {
        this.containerId = containerId
        this.playerInventory = playerInventory
        this.input = TransientCraftingContainer(this, 3, 3)
        initializeSlots(playerInventory)
    }



    private fun initializeSlots(playerInventory: Inventory) {
        val craftingGridOffsetX = 14
        val craftingGridOffsetY = 10
        val craftingOutputOffsetX = 16
        val craftingOutputOffsetY = 10
        val playerInventoryOffsetX = 0
        val playerInventoryOffsetY = 16

        //addSlot(ResultSlot(playerInventory.player, , , 0, 124 + craftingOutputOffsetX, 35 + craftingOutputOffsetY))


        for (i in 0..2) {
            for (j in 0..2) {
                addSlot(Slot(input, j + i * 3, 30 + craftingGridOffsetX + j * 18, 17 + craftingGridOffsetY + i * 18))
            }
        }
        for (i in 0..2) {
            for (j in 0..8) {
                addSlot(Slot(playerInventory, j + i * 9 + 9, 8 + playerInventoryOffsetX + j * 18, 84 + playerInventoryOffsetY+ i * 18))
            }
        }
        for (i in 0..8) {
            addSlot(Slot(playerInventory, i, 8 + playerInventoryOffsetX + i * 18, 142 + playerInventoryOffsetY))
        }
    }


    override fun fillCraftSlotsStackedContents(itemHelper: StackedContents) {
        TODO("Not yet implemented")
    }

    override fun clearCraftingContent() {
        TODO("Not yet implemented")
    }

    override fun recipeMatches(recipe: RecipeHolder<AbstractCookingRecipe?>): Boolean {
        return false
    }

    override fun getResultSlotIndex(): Int {
        return 2
    }

    override fun getGridWidth(): Int {
        return 1
    }

    override fun getGridHeight(): Int {
        return 1
    }

    override fun getSize(): Int {
        return 3
    }

    override fun getRecipeBookType(): RecipeBookType? {
        return RecipeBookType.CRAFTING
    }

    override fun shouldMoveToInventory(slotIndex: Int): Boolean {
        return false
    }

    override fun quickMoveStack(
        player: Player,
        index: Int
    ): ItemStack? {
        TODO("Not yet implemented")
    }

    override fun stillValid(player: Player): Boolean {
        return true
    }


}