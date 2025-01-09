/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.block.entity

import com.cobblemon.mod.common.CobblemonBlockEntities
import com.cobblemon.mod.common.CobblemonItemComponents
import com.cobblemon.mod.common.CobblemonRecipeTypes
import com.cobblemon.mod.common.CobblemonSounds
import com.cobblemon.mod.common.api.cooking.Seasoning
import com.cobblemon.mod.common.api.cooking.Seasonings
import com.cobblemon.mod.common.api.fishing.FishingBait
import com.cobblemon.mod.common.api.fishing.FishingBaits
import com.cobblemon.mod.common.block.PotComponent
import com.cobblemon.mod.common.client.gui.cookingpot.CookingPotMenu
import com.cobblemon.mod.common.client.gui.cookingpot.CookingPotRecipeBase
import com.cobblemon.mod.common.client.sound.BlockEntitySoundTracker
import com.cobblemon.mod.common.client.sound.instances.CancellableSoundInstance
import com.cobblemon.mod.common.item.components.CookingComponent
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
import net.minecraft.world.item.crafting.RecipeType
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity
import net.minecraft.world.level.block.state.BlockState
import java.util.*

class CampfireBlockEntity(pos: BlockPos, state: BlockState) : BaseContainerBlockEntity(
    CobblemonBlockEntities.CAMPFIRE,
    pos,
    state
), WorldlyContainer, RecipeCraftingHolder, StackedContentsCompatible, CraftingContainer {

    companion object {
        const val RESULT_SLOT = 0;
        val CRAFTING_GRID_SLOTS = 1..9
        val SEASONING_SLOTS = 10..12
        val PLAYER_INVENTORY_SLOTS = 13..39
        val PLAYER_HOTBAR_SLOTS = 40..48

        const val COOKING_PROGRESS_INDEX = 0
        const val COOKING_PROGRESS_TOTAL_TIME = 1

        fun clientTick(level: Level, pos: BlockPos, state: BlockState, campfireBlockEntity: CampfireBlockEntity) {
            if (level.isClientSide) {
                val isSoundActive = BlockEntitySoundTracker.isActive(pos, campfireBlockEntity.runningSound.location)
                if (!campfireBlockEntity.isEmpty && !isSoundActive) {
                    BlockEntitySoundTracker.play(pos, CancellableSoundInstance(campfireBlockEntity.runningSound, pos, true, 0.8f, 1.0f))
                } else if (campfireBlockEntity.isEmpty && isSoundActive) {
                    BlockEntitySoundTracker.stop(pos, campfireBlockEntity.runningSound.location)
                }
            }
        }

        fun serverTick(level: Level, pos: BlockPos, state: BlockState, campfireBlockEntity: CampfireBlockEntity) {
            if (level.isClientSide) return

            val craftingInput = CraftingInput.of(3, 3, campfireBlockEntity.items.subList(1, 10))

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

            if (!optionalRecipe.isPresent) {
                campfireBlockEntity.cookingProgress = 0
                return
            }

            val cookingPotRecipe = optionalRecipe.get()
            val recipe = cookingPotRecipe.value()
            val cookedItem = recipe.assemble(craftingInput, level.registryAccess())
            cookedItem.set(CobblemonItemComponents.COOKING_COMPONENT, campfireBlockEntity.createCookingComponentFromSlots())

            val resultSlotItem = campfireBlockEntity.getItem(0)

            if (!resultSlotItem.isEmpty && !ItemStack.isSameItemSameComponents(resultSlotItem, cookedItem)) {
                campfireBlockEntity.cookingProgress = 0
                return
            }

            campfireBlockEntity.cookingProgress++
            if (campfireBlockEntity.cookingProgress == campfireBlockEntity.cookingTotalTime) {
                campfireBlockEntity.cookingProgress = 0

                if (!cookedItem.isEmpty) {
                    campfireBlockEntity.recipeUsed = cookingPotRecipe

                    if (resultSlotItem.isEmpty) {
                        campfireBlockEntity.setItem(0, cookedItem)
                    } else {
                        resultSlotItem.grow(cookedItem.count)
                    }

                    campfireBlockEntity.consumeCraftingIngredients()

                    setChanged(level, pos, state);
                }
            }
        }
    }

    private val runningSound = CobblemonSounds.CAMPFIRE_POT_COOK
    private var cookingProgress : Int = 0
    private var cookingTotalTime : Int = 200
    private var items : NonNullList<ItemStack?> = NonNullList.withSize(14, ItemStack.EMPTY)
    private val recipesUsed: Object2IntOpenHashMap<ResourceLocation> = Object2IntOpenHashMap()
    private val quickCheck: RecipeManager.CachedCheck<CraftingInput, *> = RecipeManager.createCheck(CobblemonRecipeTypes.COOKING_POT_COOKING)
    private var potComponent: PotComponent? = null

    private var dataAccess : ContainerData = object : ContainerData {
        override fun get(index: Int): Int {
            return when (index) {
                COOKING_PROGRESS_INDEX -> this@CampfireBlockEntity.cookingProgress
                COOKING_PROGRESS_TOTAL_TIME -> this@CampfireBlockEntity.cookingTotalTime
                else -> 0
            }
        }

        override fun set(index: Int, value: Int) {
            when (index) {
                COOKING_PROGRESS_INDEX -> this@CampfireBlockEntity.cookingProgress = value
                COOKING_PROGRESS_TOTAL_TIME -> this@CampfireBlockEntity.cookingTotalTime = value
            }
        }

        override fun getCount(): Int {
            return 2
        }
    }

    fun consumeCraftingIngredients() {
        for (i in 1..13) {
            val itemInSlot = getItem(i)
            if (!itemInSlot.isEmpty) {
                when (itemInSlot.item) {
                    Items.LAVA_BUCKET, Items.WATER_BUCKET, Items.MILK_BUCKET -> {
                        // Replace with empty bucket
                        setItem(i, ItemStack(Items.BUCKET))
                    }
                    Items.HONEY_BOTTLE -> {
                        // Replace with empty glass bottle
                        setItem(i, ItemStack(Items.GLASS_BOTTLE))
                    }
                    else -> {
                        // Decrease the stack size by 1
                        itemInSlot.shrink(1)
                        if (itemInSlot.count <= 0) {
                            setItem(i, ItemStack.EMPTY) // Clear the slot if empty
                        }
                    }
                }
            }
        }
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
            val itemInSlot = getItem(slot)

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

    override fun getDefaultName(): Component {
        return Component.translatable("container.cooking_pot")
    }

    override fun getWidth(): Int {
        return 3
    }

    override fun getHeight(): Int {
        return 3
    }

    override fun getItems(): NonNullList<ItemStack?> {
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
    ): AbstractContainerMenu {
        return CookingPotMenu(containerId, inventory, this, this.dataAccess)
    }

    override fun getContainerSize(): Int {
        return this.items.size
    }

    override fun getSlotsForFace(side: Direction): IntArray {
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
        for (itemStack in this.items) {
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
        this.potComponent = PotComponent(stack ?: ItemStack.EMPTY) // Ensure a non-null value is passed
        setChanged()
        level?.sendBlockUpdated(blockPos, blockState, blockState, Block.UPDATE_CLIENTS)
    }

    override fun saveAdditional(tag: CompoundTag, registries: HolderLookup.Provider) {
        super.saveAdditional(tag, registries)

        ContainerHelper.saveAllItems(tag, this.items, registries)
        potComponent?.let { component ->
            PotComponent.CODEC.encodeStart(NbtOps.INSTANCE, component)
                .result()
                ?.ifPresent { encoded -> tag.put("PotComponent", encoded) }
        }
    }

    override fun loadAdditional(tag: CompoundTag, registries: HolderLookup.Provider) {
        super.loadAdditional(tag, registries)

        ContainerHelper.loadAllItems(tag, this.items, registries)
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

    override fun setRemoved() {
        super.setRemoved()

        if (level?.isClientSide == true) {
            BlockEntitySoundTracker.stop(blockPos, runningSound.location)
        }
    }
}