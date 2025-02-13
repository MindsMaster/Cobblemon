/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.cooking

import com.cobblemon.mod.common.item.components.CookingComponent
import net.minecraft.util.FastColor
import net.minecraft.world.item.DyeColor
import net.minecraft.world.item.ItemStack
import kotlin.collections.flatMap

private val colorMap = mapOf(
    "spicy" to 0xFEB37D,
    "dry" to 0x8AE9FC,
    "sweet" to 0xFFBEED,
    "bitter" to 0x9EED8F,
    "sour" to 0xFCF38A
)

private val bubbleColorMap = mapOf(
    "spicy" to 0xFFCC92,
    "dry" to 0xA1F5FE,
    "sweet" to 0xFFD6F7,
    "bitter" to 0xB7F7A7,
    "sour" to 0xFEFAA1
)

fun getColorMixFromSeasonings(seasonings: List<ItemStack>, forBubbles: Boolean = false): Int? {
    val flavors = seasonings
        .flatMap { Seasonings.getFromItemStack(it)?.flavors?.entries ?: emptyList() }
        .groupingBy { it.key }
        .fold(0) { acc, entry -> acc + entry.value }

    val maxFlavorValue = flavors.values.maxOrNull()
    val dominantFlavors = flavors.filter { it.value == maxFlavorValue }.map { it.key }

    return getColorMixFromCookingComponent(dominantFlavors, forBubbles)
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

fun getColorMixFromCookingComponent(dominantFlavors: List<String>, forBubbles: Boolean = false): Int? {
    val colors =
        dominantFlavors.mapNotNull { if (forBubbles) bubbleColorMap[it] else colorMap[it] }
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
        "red" -> DyeColor.RED
        "orange" -> DyeColor.ORANGE
        "yellow" -> DyeColor.YELLOW
        "lime" -> DyeColor.LIME
        "green" -> DyeColor.GREEN
        "cyan" -> DyeColor.CYAN
        "light blue" -> DyeColor.LIGHT_BLUE
        "blue" -> DyeColor.BLUE
        "purple" -> DyeColor.PURPLE
        "magenta" -> DyeColor.MAGENTA
        "pink" -> DyeColor.PINK
        "white" -> DyeColor.WHITE
        else -> null
    }?.textureDiffuseColor
}