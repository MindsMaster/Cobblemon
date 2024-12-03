/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.block.entity

import com.cobblemon.mod.common.CobblemonBlockEntities
import com.cobblemon.mod.common.CobblemonRecipeTypes
import com.cobblemon.mod.common.client.gui.cookingpot.CookingPotMenu
import com.cobblemon.mod.common.client.gui.cookingpot.CookingPotRecipe
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.HolderLookup
import net.minecraft.core.NonNullList
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.ContainerHelper
import net.minecraft.world.WorldlyContainer
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.StackedContents
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.ContainerData
import net.minecraft.world.inventory.CraftingContainer
import net.minecraft.world.inventory.RecipeCraftingHolder
import net.minecraft.world.inventory.StackedContentsCompatible
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.CraftingInput
import net.minecraft.world.item.crafting.RecipeHolder
import net.minecraft.world.item.crafting.RecipeManager
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity
import net.minecraft.world.level.block.state.BlockState

class CookingPotBlockEntity : BaseContainerBlockEntity, WorldlyContainer, RecipeCraftingHolder, StackedContentsCompatible, CraftingContainer {

    companion object {
        fun serverTick(level: Level, pos: BlockPos, state: BlockState, cookingPotBlockEntity: CookingPotBlockEntity) {
            if (!level.isClientSide) {
                val craftingInput = cookingPotBlockEntity.asCraftInput()
                var itemStack = ItemStack.EMPTY
                cookingPotBlockEntity.quickCheck.getRecipeFor(craftingInput, level).ifPresent { cookingPotRecipe ->
                    val recipeHolder  = cookingPotRecipe as RecipeHolder<*>
                    recipeHolder.value.getResultItem(level.registryAccess()).let { itemStack = it }
                    craftingInput.items().clear()
                    cookingPotBlockEntity.items.set(9,  itemStack)
                }
        }
    }
    }


    private var cookingProgress : Int = 0
    private var cookingTotalTime : Int = 0
    private var dataAccess : ContainerData
    private var items : NonNullList<ItemStack?>
    private val recipesUsed: Object2IntOpenHashMap<ResourceLocation>
    private val quickCheck: RecipeManager.CachedCheck<CraftingInput, *>

    constructor(pos: BlockPos, state: BlockState) : super(CobblemonBlockEntities.COOKING_POT, pos, state) {
        this.items = NonNullList.withSize(10, ItemStack.EMPTY);
        this.recipesUsed = Object2IntOpenHashMap()
        this.quickCheck = RecipeManager.createCheck(CobblemonRecipeTypes.COOKING_POT_COOKING);

        this.dataAccess = object : ContainerData {
            override fun get(index: Int): Int {
                return when (index) {
                    0 -> this@CookingPotBlockEntity.cookingProgress
                    1 -> this@CookingPotBlockEntity.cookingTotalTime
                    else -> 0
                }
            }

            override fun set(index: Int, value: Int) {
                when (index) {
                    0 -> this@CookingPotBlockEntity.cookingProgress = value
                    1 -> this@CookingPotBlockEntity.cookingTotalTime = value
                }
            }

            override fun getCount(): Int {
                return 2
            }
        }
    }

    override fun getDefaultName(): Component? {
        return Component.translatable("container.cooking_pot")
    }

    override fun getWidth(): Int {
        return 3
    }

    override fun getHeight(): Int {
        return 3
    }

    override fun getItems(): NonNullList<ItemStack?>? {
        return this.items
    }

    override fun setItems(items: NonNullList<ItemStack?>) {
        this.items = items
    }

    override fun loadAdditional(tag: CompoundTag, registries: HolderLookup.Provider) {
        super.loadAdditional(tag, registries);
        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, this.items, registries);
        this.cookingProgress = tag.getShort("CookTime").toInt();
        this.cookingTotalTime = tag.getShort("CookTimeTotal").toInt();
        val compoundTag = tag.getCompound("RecipesUsed");

        for(string in compoundTag.getAllKeys()) {
            this.recipesUsed.put(ResourceLocation.parse(string), compoundTag.getInt(string));
        }
    }

    override fun saveAdditional(tag: CompoundTag, registries: HolderLookup.Provider) {
        super.saveAdditional(tag, registries);
        tag.putShort("CookTime", this.cookingProgress.toShort());
        tag.putShort("CookTimeTotal", this.cookingTotalTime.toShort());
        ContainerHelper.saveAllItems(tag, this.items, registries);
        val compoundTag = CompoundTag();
        this.recipesUsed.forEach { resourceLocation, integer ->
            compoundTag.putInt(resourceLocation.toString(), integer)
        }
        tag.put("RecipesUsed", compoundTag);
    }

    override fun createMenu(
        containerId: Int,
        inventory: Inventory
    ): AbstractContainerMenu? {
        return CookingPotMenu(containerId, inventory, this, this.dataAccess)
    }

    override fun getContainerSize(): Int {
        return this.items.size
    }

    override fun getSlotsForFace(side: Direction): IntArray? {
        return intArrayOf(0, 1, 2)
    }

    override fun canPlaceItemThroughFace(
        index: Int,
        itemStack: ItemStack,
        direction: Direction?
    ): Boolean {
        return false
    }

    override fun canTakeItemThroughFace(
        index: Int,
        stack: ItemStack,
        direction: Direction
    ): Boolean {
        return false
    }

    override fun setRecipeUsed(recipe: RecipeHolder<*>?) {
        if (recipe != null) {
            val resourceLocation = recipe.id();
            this.recipesUsed.addTo(resourceLocation, 1);
        }
    }

    override fun getRecipeUsed(): RecipeHolder<*>? {
        return null
    }

    override fun fillStackedContents(contents: StackedContents) {
        for(itemStack in this.items) {
            contents.accountSimpleStack(itemStack);
        }
    }
}