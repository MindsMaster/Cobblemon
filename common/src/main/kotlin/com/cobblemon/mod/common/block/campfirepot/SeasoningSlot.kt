package com.cobblemon.mod.common.block.campfirepot

import com.cobblemon.mod.common.api.cooking.Seasonings
import net.minecraft.world.Container
import net.minecraft.world.inventory.Slot
import net.minecraft.world.item.ItemStack

class SeasoningSlot(container: Container, slot: Int, x: Int, y: Int) : Slot(container, slot, x, y) {
    override fun mayPlace(stack: ItemStack): Boolean {
        return Seasonings.isSeasoning(stack) && super.mayPlace(stack)
    }
}