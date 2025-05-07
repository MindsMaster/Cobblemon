package com.cobblemon.mod.common.block.brewingstand

import com.cobblemon.mod.common.CobblemonMenuType
import com.cobblemon.mod.common.CobblemonRecipeTypes
import com.cobblemon.mod.common.item.crafting.brewingstand.BrewingStandInput
import com.cobblemon.mod.common.item.crafting.brewingstand.BrewingStandRecipe
import net.minecraft.advancements.CriteriaTriggers
import net.minecraft.core.component.DataComponents
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.Container
import net.minecraft.world.SimpleContainer
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.entity.player.StackedContents
import net.minecraft.world.inventory.*
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.alchemy.PotionBrewing
import net.minecraft.world.item.alchemy.PotionContents
import net.minecraft.world.item.crafting.RecipeHolder
import net.minecraft.world.item.crafting.RecipeManager
import net.minecraft.world.item.crafting.RecipeType
import net.minecraft.world.level.Level

class BrewingStandMenu(
    containerId: Int,
    playerInventory: Inventory,
    private val brewingStand: Container = SimpleContainer(SLOT_COUNT),
    private val brewingStandData: ContainerData = SimpleContainerData(DATA_COUNT),
    menuType: MenuType<*> = CobblemonMenuType.BREWING_STAND
) : RecipeBookMenu<BrewingStandInput, BrewingStandRecipe>(menuType, containerId), ContainerListener {

    private val player: Player = playerInventory.player
    private val level: Level = player.level()
    private val resultContainer: ResultContainer = ResultContainer()
    private val recipeType: RecipeType<BrewingStandRecipe> = CobblemonRecipeTypes.BREWING_STAND
    private val quickCheck = RecipeManager.createCheck(CobblemonRecipeTypes.BREWING_STAND)
    private val ingredientSlot: Slot

    init {
        checkContainerSize(brewingStand, SLOT_COUNT)
        checkContainerDataCount(brewingStandData, DATA_COUNT)

        val potionBrewing = player.level().potionBrewing()

        addSlot(PotionSlot(brewingStand, 0, 56, 51))
        addSlot(PotionSlot(brewingStand, 1, 79, 58))
        addSlot(PotionSlot(brewingStand, 2, 102, 51))
        ingredientSlot = addSlot(IngredientsSlot(potionBrewing, brewingStand, 3, 79, 17))
        addSlot(FuelSlot(brewingStand, 4, 17, 17))

        addDataSlots(brewingStandData)

        for (i in 0 until 3) {
            for (j in 0 until 9) {
                addSlot(Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18))
            }
        }

        for (i in 0 until 9) {
            addSlot(Slot(playerInventory, i, 8 + i * 18, 142))
        }
    }

    constructor(containerId: Int, playerInventory: Inventory) : this(
        containerId,
        playerInventory,
        SimpleContainer(SLOT_COUNT),
        SimpleContainerData(DATA_COUNT)
    )

    override fun stillValid(player: Player): Boolean {
        return brewingStand.stillValid(player)
    }

    override fun quickMoveStack(player: Player, index: Int): ItemStack {
        var itemStack = ItemStack.EMPTY
        val slot = slots.getOrNull(index) ?: return itemStack

        if (slot.hasItem()) {
            val itemStack2 = slot.item
            itemStack = itemStack2.copy()

            if (index !in 0..2 && index != 3 && index != 4) {
                when {
                    FuelSlot.mayPlaceItem(itemStack2) -> {
                        if (!moveItemStackTo(itemStack2, 4, 5, false) &&
                            ingredientSlot.mayPlace(itemStack2) &&
                            !moveItemStackTo(itemStack2, 3, 4, false)
                        ) return ItemStack.EMPTY
                    }

                    ingredientSlot.mayPlace(itemStack2) -> {
                        if (!moveItemStackTo(itemStack2, 3, 4, false)) return ItemStack.EMPTY
                    }

                    PotionSlot.mayPlaceItem(itemStack2) -> {
                        if (!moveItemStackTo(itemStack2, 0, 3, false)) return ItemStack.EMPTY
                    }

                    index in 5 until 32 -> {
                        if (!moveItemStackTo(itemStack2, 32, 41, false)) return ItemStack.EMPTY
                    }

                    index in 32 until 41 -> {
                        if (!moveItemStackTo(itemStack2, 5, 32, false)) return ItemStack.EMPTY
                    }

                    else -> {
                        if (!moveItemStackTo(itemStack2, 5, 41, false)) return ItemStack.EMPTY
                    }
                }
            } else {
                if (!moveItemStackTo(itemStack2, 5, 41, true)) return ItemStack.EMPTY
                slot.onQuickCraft(itemStack2, itemStack)
            }

            if (itemStack2.isEmpty) {
                slot.setByPlayer(ItemStack.EMPTY)
            } else {
                slot.setChanged()
            }

            if (itemStack2.count == itemStack.count) return ItemStack.EMPTY
            slot.onTake(player, itemStack)
        }

        return itemStack
    }

    fun getFuel(): Int = brewingStandData[1]
    fun getBrewingTicks(): Int = brewingStandData[0]
    override fun fillCraftSlotsStackedContents(itemHelper: StackedContents) {
        TODO("Not yet implemented")
    }

    override fun clearCraftingContent() {
        TODO("Not yet implemented")
    }

    override fun recipeMatches(recipe: RecipeHolder<BrewingStandRecipe?>): Boolean {
        TODO("Not yet implemented")
    }

    override fun getResultSlotIndex(): Int {
        return 3
    }

    override fun getGridWidth(): Int {
        return 1
    }

    override fun getGridHeight(): Int {
        return 1
    }

    override fun getSize(): Int {
        return 5
    }

    override fun getRecipeBookType(): RecipeBookType? {
        return RecipeBookType.valueOf("BREWING_STAND")
    }

    override fun shouldMoveToInventory(slotIndex: Int): Boolean {
        TODO("Not yet implemented")
    }

    override fun slotChanged(
        containerToSend: AbstractContainerMenu,
        dataSlotIndex: Int,
        stack: ItemStack
    ) {
        TODO("Not yet implemented")
    }

    override fun dataChanged(
        containerMenu: AbstractContainerMenu,
        dataSlotIndex: Int,
        value: Int
    ) {
        TODO("Not yet implemented")
    }

    class PotionSlot(container: Container, slot: Int, x: Int, y: Int) : Slot(container, slot, x, y) {
        override fun mayPlace(stack: ItemStack): Boolean = mayPlaceItem(stack)

        override fun getMaxStackSize(): Int = 1

        override fun onTake(player: Player, stack: ItemStack) {
            val optional =
                (stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY) as PotionContents).potion()
            if (optional.isPresent && player is ServerPlayer) {
                CriteriaTriggers.BREWED_POTION.trigger(player, optional.get())
            }
            super.onTake(player, stack)
        }

        companion object {
            fun mayPlaceItem(stack: ItemStack): Boolean {
                return stack.`is`(Items.POTION) ||
                        stack.`is`(Items.SPLASH_POTION) ||
                        stack.`is`(Items.LINGERING_POTION) ||
                        stack.`is`(Items.GLASS_BOTTLE)
            }
        }
    }

    class IngredientsSlot(
        private val potionBrewing: PotionBrewing,
        container: Container,
        slot: Int,
        x: Int,
        y: Int
    ) : Slot(container, slot, x, y) {
        override fun mayPlace(stack: ItemStack): Boolean = potionBrewing.isIngredient(stack)
    }

    class FuelSlot(container: Container, slot: Int, x: Int, y: Int) : Slot(container, slot, x, y) {
        override fun mayPlace(stack: ItemStack): Boolean = mayPlaceItem(stack)

        companion object {
            fun mayPlaceItem(stack: ItemStack): Boolean = stack.`is`(Items.BLAZE_POWDER)
        }
    }

    companion object {
        private const val SLOT_COUNT = 5
        private const val DATA_COUNT = 2
    }
}
