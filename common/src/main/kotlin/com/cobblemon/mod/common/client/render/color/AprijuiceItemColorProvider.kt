/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.color

import com.cobblemon.mod.common.CobblemonItemComponents
import net.minecraft.ChatFormatting
import net.minecraft.client.color.item.ItemColor
import net.minecraft.util.FastColor
import net.minecraft.world.item.ItemStack

object AprijuiceItemColorProvider : ItemColor {

    private val LEAF_INDEX = 1
    private val JUICE_INDEX = 2

    private val colorMap = mapOf(
        "spicy" to ChatFormatting.RED.color,
        "dry" to ChatFormatting.BLUE.color,
        "sweet" to ChatFormatting.LIGHT_PURPLE.color,
        "bitter" to ChatFormatting.GREEN.color,
        "sour" to ChatFormatting.YELLOW.color
    )

    override fun getColor(stack: ItemStack, layer: Int): Int {
        if (layer == 0) return -1

        val cookingComponent = stack.get(CobblemonItemComponents.COOKING_COMPONENT) ?: return -1

        if (layer == LEAF_INDEX) {
            val quality = cookingComponent.getQualityAverage()
            val color = when {
                quality < 10 -> ChatFormatting.RED.color
                quality < 20 -> ChatFormatting.YELLOW.color
                else -> ChatFormatting.GREEN.color
            }

            color?.let { return FastColor.ARGB32.opaque(color) }
        }

        if (layer == JUICE_INDEX) {
            val dominantFlavors = cookingComponent.getDominantFlavors()
            val colors =
                dominantFlavors.mapNotNull { colorMap[it] }
                .map { FastColor.ARGB32.opaque(it) }

            if (colors.isNotEmpty()) return getColorMix(colors)
        }

        return -1
    }

    private fun getColorMix(colors: List<Int>): Int {
        val (alphaSum, redSum, greenSum, blueSum) = colors.fold(IntArray(4)) { acc, color ->
            acc[0] += FastColor.ARGB32.alpha(color)
            acc[1] += FastColor.ARGB32.red(color)
            acc[2] += FastColor.ARGB32.green(color)
            acc[3] += FastColor.ARGB32.blue(color)
            acc
        }

        return FastColor.ARGB32.color(
            alphaSum / colors.size,
            redSum / colors.size,
            greenSum / colors.size,
            blueSum / colors.size
        )
    }
}