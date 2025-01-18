/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.cooking

import com.cobblemon.mod.common.item.components.CookingComponent
import net.minecraft.ChatFormatting
import net.minecraft.util.FastColor
import net.minecraft.world.item.ItemStack
import kotlin.collections.flatMap

private val colorMap = mapOf(
    "spicy" to ChatFormatting.RED.color,
    "dry" to ChatFormatting.BLUE.color,
    "sweet" to ChatFormatting.LIGHT_PURPLE.color,
    "bitter" to ChatFormatting.GREEN.color,
    "sour" to ChatFormatting.YELLOW.color
)

fun getColorMixFromSeasonings(seasonings: List<ItemStack>): Int? {
    val flavors = seasonings
        .flatMap { Seasonings.getFromItemStack(it)?.flavors?.entries ?: emptyList() }
        .groupingBy { it.key }
        .fold(0) { acc, entry -> acc + entry.value }

    val maxFlavorValue = flavors.values.maxOrNull()
    val dominantFlavors = flavors.filter { it.value == maxFlavorValue }.map { it.key }

    return getColorMixFromCookingComponent(dominantFlavors)
}

fun getTransparentColorMixFromSeasonings(seasonings: List<ItemStack>): Int? {
    val baseColor = getColorMixFromSeasonings(seasonings) // This gets the original color
    if (baseColor == null) return null

    // Apply 50% transparency by ensuring the alpha channel is 0x80
    return (baseColor and 0x00FFFFFF) or (0x80 shl 24)
}


fun getColorMixFromCookingComponent(cookingComponent: CookingComponent): Int? {
    val dominantFlavors = cookingComponent.getDominantFlavors()
    return getColorMixFromCookingComponent(dominantFlavors)
}

fun getColorMixFromCookingComponent(dominantFlavors: List<String>): Int? {
    val colors =
        dominantFlavors.mapNotNull { colorMap[it] }
            .map { FastColor.ARGB32.opaque(it) }

    if (colors.isEmpty()) return null

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

fun getColor(color: String): Int? {
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
        else -> null
    }
}