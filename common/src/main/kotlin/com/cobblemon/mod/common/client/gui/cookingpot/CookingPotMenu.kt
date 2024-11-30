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
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.Container
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
    private val craftSlots: CraftingContainer
    private val resultSlot: ResultContainer
    private val recipeType: RecipeType<CookingPotRecipe> = CobblemonRecipeTypes.COOKING_POT_COOKING

    constructor(containerId: Int, playerInventory: Inventory) :
            super(CobblemonMenuType.COOKING_POT, containerId) {
        this.containerId = containerId
        this.playerInventory = playerInventory
        this.craftSlots = TransientCraftingContainer(this, 3, 3)
        this.resultSlot = ResultContainer()
        this.player = playerInventory.player
        this.level = playerInventory.player.level()
        initializeSlots(playerInventory)
    }

    constructor(containerId: Int, playerInventory: Inventory, container: CraftingContainer, containerData: ContainerData) :
            super(CobblemonMenuType.COOKING_POT, containerId) {
        this.containerId = containerId
        this.playerInventory = playerInventory
        this.craftSlots = container
        this.resultSlot = ResultContainer()

        this.player = playerInventory.player
        this.level = playerInventory.player.level()
        container.startOpen(playerInventory.player)
        initializeSlots(playerInventory)
        this.addSlotListener(this)
    }

    private fun initializeSlots(playerInventory: Inventory) {
        val craftingOutputOffsetX = 16
        val craftingOutputOffsetY = 10

        addSlot(ResultSlot(playerInventory.player, this.craftSlots, this.resultSlot, 0, 124 + craftingOutputOffsetX, 35 + craftingOutputOffsetY))

        for (i in 0..2) {
            for (j in 0..2) {
                this.addSlot(Slot(this.craftSlots, j + i * 3, 44 + j * 18, 27 + i * 18))
            }
        }

        for (i in 0..2) {
            for (j in 0..8) {
                this.addSlot(Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 100 + i * 18))
            }
        }

        for (i in 0..8) {
            this.addSlot(Slot(playerInventory, i, 8 + i * 18, 158))
        }
    }

    fun canCook(): Boolean {
        val optionalRecipe = this.level.recipeManager.getRecipeFor(
                recipeType,
                craftSlots.asCraftInput(),
                this.level
        )
        return optionalRecipe.isPresent
    }


    override fun removed(player: Player) {
        super.removed(player)

    }

    override fun fillCraftSlotsStackedContents(itemHelper: StackedContents) {
        this.craftSlots.fillStackedContents(itemHelper)
    }

    override fun clearCraftingContent() {
        craftSlots.clearContent()
        resultSlot.clearContent()
    }

    override fun recipeMatches(recipe: RecipeHolder<CookingPotRecipe?>): Boolean {
        val matches = recipe.value()?.matches(this.craftSlots.asCraftInput(), this.player.level()) == true
        if (matches) {
            println("Recipe matched: ${recipe.value()}")
        } else {
            println("Recipe did not match: ${recipe.value()}")
        }
        return matches
    }


    override fun getResultSlotIndex(): Int {
        return 0
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
        return this.craftSlots.stillValid(player)
    }

    override fun slotChanged(
        containerToSend: AbstractContainerMenu,
        dataSlotIndex: Int,
        stack: ItemStack
    ) {
        if (!level.isClientSide) {
            val craftingInput = craftSlots.asCraftInput()
            val serverPlayer = player as ServerPlayer
            var itemStack = ItemStack.EMPTY
            val optional = level.recipeManager.getRecipeFor(recipeType, craftingInput, level)
            if (optional.isPresent) {
                val recipeHolder = optional.get()
                println("Recipe holder: ${recipeHolder.value()}")
                val craftingRecipe = recipeHolder.value()
                if (this.resultSlot.setRecipeUsed(level, serverPlayer, recipeHolder)) {
                    val itemStack2 = craftingRecipe.assemble(craftingInput, level.registryAccess())
                    if (itemStack2?.isItemEnabled(level.enabledFeatures()) ?: false) {
                        itemStack = itemStack2
                    }
                }
            }

            this.resultSlot.setItem(0, itemStack)
            this.setRemoteSlot(0, itemStack)
            serverPlayer.connection.send(ClientboundContainerSetSlotPacket(this.containerId, this.incrementStateId(), 0, itemStack))
        }
    }

    override fun dataChanged(
        containerMenu: AbstractContainerMenu,
        dataSlotIndex: Int,
        value: Int
    ) {
        }
}