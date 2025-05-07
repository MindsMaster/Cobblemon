package com.cobblemon.mod.common.block.entity

import com.cobblemon.mod.common.CobblemonBlockEntities
import com.cobblemon.mod.common.block.brewingstand.BrewingStandBlock
import com.cobblemon.mod.common.block.brewingstand.BrewingStandMenu
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.HolderLookup
import net.minecraft.core.NonNullList
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.ContainerHelper
import net.minecraft.world.Containers
import net.minecraft.world.WorldlyContainer
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.StackedContents
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.ContainerData
import net.minecraft.world.inventory.CraftingContainer
import net.minecraft.world.inventory.RecipeCraftingHolder
import net.minecraft.world.inventory.StackedContentsCompatible
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.alchemy.PotionBrewing
import net.minecraft.world.item.crafting.RecipeHolder
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity
import net.minecraft.world.level.block.state.BlockState
import org.jetbrains.annotations.Nullable
import java.util.*

class BrewingStandBlockEntity(pos: BlockPos, state: BlockState) :
    BaseContainerBlockEntity(CobblemonBlockEntities.BREWING_STAND, pos, state), WorldlyContainer, RecipeCraftingHolder, StackedContentsCompatible, CraftingContainer {

    private var items: NonNullList<ItemStack> = NonNullList.withSize(5, ItemStack.EMPTY)
    var brewTime: Int = 0
    private var lastPotionCount: BooleanArray? = null
    private var ingredient: Item? = null
    var fuel: Int = 0
    private val recipesUsed: Object2IntOpenHashMap<ResourceLocation> = Object2IntOpenHashMap()

    companion object {
        private const val INGREDIENT_SLOT = 3
        private const val FUEL_SLOT = 4
        private val SLOTS_FOR_UP = intArrayOf(3)
        private val SLOTS_FOR_DOWN = intArrayOf(0, 1, 2, 3)
        private val SLOTS_FOR_SIDES = intArrayOf(0, 1, 2, 4)
        const val FUEL_USES = 20

        private fun isBrewable(potionBrewing: PotionBrewing, items: NonNullList<ItemStack>): Boolean {
            val ingredientStack = items[3]
            if (ingredientStack.isEmpty || !potionBrewing.isIngredient(ingredientStack)) return false
            for (i in 0 until 3) {
                val potionStack = items[i]
                if (!potionStack.isEmpty && potionBrewing.hasMix(potionStack, ingredientStack)) {
                    return true
                }
            }
            return false
        }

        private fun doBrew(level: Level, pos: BlockPos, items: NonNullList<ItemStack>) {
            val ingredientStack = items[3]
            val potionBrewing = level.potionBrewing()
            for (i in 0 until 3) {
                items[i] = potionBrewing.mix(ingredientStack, items[i])
            }

            ingredientStack.shrink(1)
            if (ingredientStack.item.hasCraftingRemainingItem()) {
                val remainder = ItemStack(ingredientStack.item.craftingRemainingItem!!)
                if (ingredientStack.isEmpty) {
                    items[3] = remainder
                } else {
                    Containers.dropItemStack(level, pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble(), remainder)
                }
            }
            level.levelEvent(1035, pos, 0)
        }
    }

    fun serverTick(level: Level, pos: BlockPos, state: BlockState, blockEntity: BrewingStandBlockEntity) {
        val fuelStack = blockEntity.items[4]
        if (blockEntity.fuel <= 0 && fuelStack.`is`(Items.BLAZE_POWDER)) {
            blockEntity.fuel = FUEL_USES
            fuelStack.shrink(1)
            setChanged(level, pos, state)
        }

        val brewable = isBrewable(level.potionBrewing(), blockEntity.items)
        val isBrewing = blockEntity.brewTime > 0
        val ingredientStack = blockEntity.items[3]

        if (isBrewing) {
            blockEntity.brewTime--
            val finishBrew = blockEntity.brewTime == 0
            if (finishBrew && brewable) {
                doBrew(level, pos, blockEntity.items)
            } else if (!brewable || ingredientStack.item != blockEntity.ingredient) {
                blockEntity.brewTime = 0
            }
            setChanged(level, pos, state)
        } else if (brewable && blockEntity.fuel > 0) {
            blockEntity.fuel--
            blockEntity.brewTime = 400
            blockEntity.ingredient = ingredientStack.item
            setChanged(level, pos, state)
        }

        val newPotionBits = blockEntity.getPotionBits()
        if (!Arrays.equals(newPotionBits, blockEntity.lastPotionCount)) {
            blockEntity.lastPotionCount = newPotionBits
            if (state.block is BrewingStandBlock) {
                var newState = state
                for (i in BrewingStandBlock.HAS_BOTTLE.indices) {
                    newState = newState.setValue(BrewingStandBlock.HAS_BOTTLE[i], newPotionBits[i])
                }
                level.setBlock(pos, newState, 2)
            }
        }
    }

    override fun getDefaultName(): Component = Component.translatable("container.brewing")

    override fun getContainerSize(): Int = items.size
    override fun getWidth(): Int {
        return 1
    }

    override fun getHeight(): Int {
        return 1
    }

    override fun getItems(): NonNullList<ItemStack> = items

    override fun setItems(items: NonNullList<ItemStack>) {
        this.items = items
    }

    private fun getPotionBits(): BooleanArray {
        val result = BooleanArray(3)
        for (i in 0..2) {
            result[i] = !items[i].isEmpty
        }
        return result
    }

    override fun loadAdditional(tag: CompoundTag, registries: HolderLookup.Provider) {
        super.loadAdditional(tag, registries)
        items = NonNullList.withSize(containerSize, ItemStack.EMPTY)
        ContainerHelper.loadAllItems(tag, items, registries)
        brewTime = tag.getShort("BrewTime").toInt()
        if (brewTime > 0) ingredient = items[3].item
        fuel = tag.getByte("Fuel").toInt()
    }

    override fun saveAdditional(tag: CompoundTag, registries: HolderLookup.Provider) {
        super.saveAdditional(tag, registries)
        tag.putShort("BrewTime", brewTime.toShort())
        ContainerHelper.saveAllItems(tag, items, registries)
        tag.putByte("Fuel", fuel.toByte())
    }

    override fun canPlaceItem(slot: Int, stack: ItemStack): Boolean {
        return when (slot) {
            3 -> level?.potionBrewing()?.isIngredient(stack) ?: false
            4 -> stack.`is`(Items.BLAZE_POWDER)
            in 0..2 -> (stack.`is`(Items.POTION) || stack.`is`(Items.SPLASH_POTION) ||
                    stack.`is`(Items.LINGERING_POTION) || stack.`is`(Items.GLASS_BOTTLE)) &&
                    getItem(slot).isEmpty

            else -> false
        }
    }

    override fun getSlotsForFace(side: Direction): IntArray {
        return when (side) {
            Direction.UP -> SLOTS_FOR_UP
            Direction.DOWN -> SLOTS_FOR_DOWN
            else -> SLOTS_FOR_SIDES
        }
    }

    override fun canPlaceItemThroughFace(index: Int, itemStack: ItemStack, @Nullable direction: Direction?): Boolean {
        return canPlaceItem(index, itemStack)
    }

    override fun canTakeItemThroughFace(index: Int, stack: ItemStack, direction: Direction): Boolean {
        return index != 3 || stack.`is`(Items.GLASS_BOTTLE)
    }

    override fun createMenu(containerId: Int, inventory: Inventory): AbstractContainerMenu {
        return BrewingStandMenu(containerId, inventory, this, dataAccess)
    }

    private val dataAccess = object : ContainerData {
        override fun get(index: Int): Int {
            return when (index) {
                0 -> brewTime
                1 -> fuel
                else -> 0
            }
        }

        override fun set(index: Int, value: Int) {
            when (index) {
                0 -> brewTime = value
                1 -> fuel = value
            }
        }

        override fun getCount(): Int = 2
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
}
