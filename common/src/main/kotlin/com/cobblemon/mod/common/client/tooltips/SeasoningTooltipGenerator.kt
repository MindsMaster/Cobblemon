package com.cobblemon.mod.common.client.tooltips

import com.cobblemon.mod.common.api.cooking.Seasonings
import com.cobblemon.mod.common.api.text.*
import com.cobblemon.mod.common.util.lang
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack

object SeasoningTooltipGenerator : TooltipGenerator() {
    private val seasoningHeader by lazy { lang("tooltip.seasoning.header").blue() }
    private val flavorSubHeader by lazy { lang("tooltip.seasoning.flavor").gold() }

    override fun generateCategoryTooltip(stack: ItemStack, lines: MutableList<Component>): MutableList<Component>? {
        if (!Seasonings.isSeasoning(stack)) return null
        return mutableListOf(seasoningHeader)
    }

    override fun generateAdditionalTooltip(stack: ItemStack, lines: MutableList<Component>): MutableList<Component>? {
        val resultLines = mutableListOf<Component>()

        // Check if the stack is a valid seasoning
        val seasoning = Seasonings.getFromItemStack(stack) ?: return null

        // Add subheader for flavor
        resultLines.add(flavorSubHeader)

        // Add flavor data
        val flavorData = seasoning.flavors.map { (flavor, value) ->
            when (flavor.lowercase()) {
                "spicy" -> Component.literal("$value").red()
                "dry" -> Component.literal("$value").blue()
                "sweet" -> Component.literal("$value").lightPurple()
                "bitter" -> Component.literal("$value").green()
                "sour" -> Component.literal("$value").yellow()
                else -> Component.literal("$value").gray() // Default for unknown flavors
            }
        }

        // Combine all flavor components into a single line while keeping formatting
        val combinedFlavorLine = Component.literal("")
        flavorData.forEach { flavorComponent ->
            combinedFlavorLine.append(flavorComponent).append(" ")
        }

        resultLines.add(combinedFlavorLine)

        return resultLines
    }
}
