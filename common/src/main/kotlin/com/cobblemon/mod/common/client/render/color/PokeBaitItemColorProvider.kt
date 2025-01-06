package com.cobblemon.mod.common.client.render.color

import com.cobblemon.mod.common.CobblemonItemComponents
import net.minecraft.client.color.item.ItemColor
import net.minecraft.util.FastColor
import net.minecraft.world.item.ItemStack

object PokeBaitItemColorProvider : ItemColor {
    override fun getColor(stack: ItemStack, layer: Int): Int {
        val cookingComponent = stack.get(CobblemonItemComponents.COOKING_COMPONENT) ?: return -1

        val primaryColor = cookingComponent.seasoning1.color
        val secondaryColor = cookingComponent.seasoning2.color
        val tertiaryColor = cookingComponent.seasoning3.color

        val color = when (layer) {
            0 -> getColor(primaryColor)
            1 -> getColor(secondaryColor)
            2 -> getColor(tertiaryColor)
            else -> -1
        }

        return color
    }

    // TODO convert this into an enum
    private fun getColor(color: String): Int {
        return when (color.lowercase()) {
            "red" -> FastColor.ARGB32.color(255, 255, 0, 0)
            "orange" -> FastColor.ARGB32.color(255, 255, 165, 0)
            "yellow" -> FastColor.ARGB32.color(255, 255, 255, 0)
            "lime" -> FastColor.ARGB32.color(255, 0, 255, 0)
            "green" -> FastColor.ARGB32.color(255, 0, 128, 0)
            "cyan" -> FastColor.ARGB32.color(255, 0, 255, 255)
            "light blue" -> FastColor.ARGB32.color(255, 173, 216, 230)
            "blue" -> FastColor.ARGB32.color(255, 0, 0, 255)
            "purple" -> FastColor.ARGB32.color(255, 128, 0, 128)
            "magenta" -> FastColor.ARGB32.color(255, 255, 0, 255)
            "pink" -> FastColor.ARGB32.color(255, 255, 192, 203)
            "white" -> FastColor.ARGB32.color(255, 255, 255, 255)
            else -> -1
        }
    }
}