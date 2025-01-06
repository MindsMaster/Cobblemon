package com.cobblemon.mod.common.client.tooltips

import com.cobblemon.mod.common.CobblemonItemComponents
import com.cobblemon.mod.common.api.cooking.Seasonings
import com.cobblemon.mod.common.api.text.*
import com.cobblemon.mod.common.util.lang
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack

object SeasoningTooltipGenerator : TooltipGenerator() {
    private val seasoningHeader by lazy { lang("item_class.seasoning").blue() }
    private val flavorSubHeader by lazy { lang("seasoning_flavor_header").blue() }

    override fun generateCategoryTooltip(stack: ItemStack, lines: MutableList<Component>): MutableList<Component>? {
        if (!Seasonings.isSeasoning(stack)) return null
        return mutableListOf(seasoningHeader)
    }

    override fun generateAdditionalTooltip(stack: ItemStack, lines: MutableList<Component>): MutableList<Component>? {
        val resultLines = mutableListOf<Component>()

        // Check if the stack is a valid seasoning
        val flavors: Map<String, Int> =
            Seasonings.getFromItemStack(stack)?.flavors ?:
            stack.get(CobblemonItemComponents.COOKING_COMPONENT)?.getFlavorsSum() ?:
            return null

        // Add subheader for flavor
        resultLines.add(flavorSubHeader)

        // Add flavor data with language keys and matching colors for text and values
        val flavorData = flavors.map { (flavor, value) ->
            val flavorLangKey = when (flavor.lowercase()) {
                "spicy" -> lang("seasoning_flavor.spicy").red()
                "dry" -> lang("seasoning_flavor.dry").darkAqua()
                "sweet" -> lang("seasoning_flavor.sweet").lightPurple()
                "bitter" -> lang("seasoning_flavor.bitter").green()
                "sour" -> lang("seasoning_flavor.sour").yellow()
                else -> lang("seasoning_flavor.unknown").gray() // Default for unknown flavors
            }

            // Combine the flavor text with its value
            Component.literal("").append(flavorLangKey).append(" $value")
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
