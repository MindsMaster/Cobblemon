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

fun getColorMixFromSeasonings(seasonings: List<ItemStack>): Int {
    val flavors = seasonings
        .flatMap { Seasonings.getFromItemStack(it)?.flavors?.entries ?: emptyList() }
        .groupingBy { it.key }
        .fold(0) { acc, entry -> acc + entry.value }

    val maxFlavorValue = flavors.values.maxOrNull()
    val dominantFlavors = flavors.filter { it.value == maxFlavorValue }.map { it.key }

    return getColorMixFromCookingComponent(dominantFlavors)
}

fun getTransparentColorMixFromSeasonings(seasonings: List<ItemStack>): Int {
    val baseColor = getColorMixFromSeasonings(seasonings) // This gets the original color
    if (baseColor == -1) {
        return -1 // Return default no-color state
    }

    // Apply 50% transparency by ensuring the alpha channel is 0x80
    return (baseColor and 0x00FFFFFF) or (0x80 shl 24)
}


fun getColorMixFromCookingComponent(cookingComponent: CookingComponent): Int {
    val dominantFlavors = cookingComponent.getDominantFlavors()
    return getColorMixFromCookingComponent(dominantFlavors)
}

fun getColorMixFromCookingComponent(dominantFlavors: List<String>): Int {
    val colors =
        dominantFlavors.mapNotNull { colorMap[it] }
            .map { FastColor.ARGB32.opaque(it) }

    if (colors.isEmpty()) return -1

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