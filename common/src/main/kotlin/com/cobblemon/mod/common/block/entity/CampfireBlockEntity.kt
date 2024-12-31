/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.block.entity

import PotComponent
import com.cobblemon.mod.common.CobblemonBlockEntities
import com.cobblemon.mod.common.CobblemonRecipeTypes
import com.cobblemon.mod.common.client.gui.cookingpot.CookingPotMenu
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.HolderLookup
import net.minecraft.core.NonNullList
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtOps
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientGamePacketListener
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket
import net.minecraft.resources.RegistryOps
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.ContainerHelper
import net.minecraft.world.WorldlyContainer
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.StackedContents
import net.minecraft.world.inventory.*
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.crafting.CraftingInput
import net.minecraft.world.item.crafting.RecipeHolder
import net.minecraft.world.item.crafting.RecipeManager
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity
import net.minecraft.world.level.block.state.BlockState

class CampfireBlockEntity : BaseContainerBlockEntity, WorldlyContainer, RecipeCraftingHolder, StackedContentsCompatible, CraftingContainer {

    companion object {
        fun serverTick(level: Level, pos: BlockPos, state: BlockState, campfireBlockEntity: CampfireBlockEntity) {
            if (!level.isClientSide) {
                var itemStack = ItemStack.EMPTY
                campfireBlockEntity.quickCheck.getRecipeFor(CraftingInput.of(3, 3, campfireBlockEntity.items.subList(1,10)), level)
                    .ifPresent { cookingPotRecipe ->
                        val recipeHolder = cookingPotRecipe as RecipeHolder<*>
                        recipeHolder.value.getResultItem(level.registryAccess()).let { itemStack = it }
                        if (!itemStack.isEmpty) {
                            campfireBlockEntity.recipeUsed = recipeHolder
                            println(itemStack)
                            campfireBlockEntity.items[0] = itemStack.copy()
                        }
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
    private var potComponent: PotComponent? = null

    constructor(pos: BlockPos, state: BlockState) : super(CobblemonBlockEntities.CAMPFIRE, pos, state) {
        this.items = NonNullList.withSize(14, ItemStack.EMPTY)
        this.recipesUsed = Object2IntOpenHashMap()
        this.quickCheck = RecipeManager.createCheck(CobblemonRecipeTypes.COOKING_POT_COOKING)

        this.dataAccess = object : ContainerData {
            override fun get(index: Int): Int {
                return when (index) {
                    0 -> this@CampfireBlockEntity.cookingProgress
                    1 -> this@CampfireBlockEntity.cookingTotalTime
                    else -> 0
                }
            }

            override fun set(index: Int, value: Int) {
                when (index) {
                    0 -> this@CampfireBlockEntity.cookingProgress = value
                    1 -> this@CampfireBlockEntity.cookingTotalTime = value
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
        onItemUpdate(level!!)
        return this.items
    }

    override fun setItems(items: NonNullList<ItemStack?>) {
        this.items.clear()
        this.items.addAll(items)
        onItemUpdate(level!!) // Notify the system about updates
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
            val resourceLocation = recipe.id()
            this.recipesUsed.addTo(resourceLocation, 1)
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

    override fun getItem(slot: Int): ItemStack {
        return if (slot in 0 until items.size) items[slot] else ItemStack.EMPTY
    }

    fun getPotItem(): ItemStack? {
        return potComponent?.potItem
    }

    fun setPotItem(stack: ItemStack?) {
        this.potComponent = PotComponent(stack)
        setChanged()
        level?.sendBlockUpdated(blockPos, blockState, blockState, Block.UPDATE_CLIENTS)
    }

    override fun saveAdditional(tag: CompoundTag, registries: HolderLookup.Provider) {
        super.saveAdditional(tag, registries)
        potComponent?.let { component ->
            PotComponent.CODEC.encodeStart(NbtOps.INSTANCE, component)
                .result()
                ?.ifPresent { encoded -> tag.put("PotComponent", encoded) }
        }
    }

    override fun loadAdditional(tag: CompoundTag, registries: HolderLookup.Provider) {
        super.loadAdditional(tag, registries)
        if (tag.contains("PotComponent")) {
            val component = PotComponent.CODEC.parse(NbtOps.INSTANCE, tag.getCompound("PotComponent"))
                .result()
                ?.orElse(null)
            potComponent = component
        }
    }

    override fun getUpdatePacket(): Packet<ClientGamePacketListener>? {
        return ClientboundBlockEntityDataPacket.create(this)
    }

    override fun getUpdateTag(registryLookup: HolderLookup.Provider): CompoundTag {
        return saveWithoutMetadata(registryLookup)
    }


    private fun onItemUpdate(level: Level) {
        val oldState = level.getBlockState(blockPos)
        level.sendBlockUpdated(blockPos, oldState, level.getBlockState(blockPos), Block.UPDATE_ALL)
        level.updateNeighbourForOutputSignal(blockPos, level.getBlockState(blockPos).block)
        setChanged()
        level.sendBlockUpdated(blockPos, oldState, level.getBlockState(blockPos), Block.UPDATE_ALL)
    }

}