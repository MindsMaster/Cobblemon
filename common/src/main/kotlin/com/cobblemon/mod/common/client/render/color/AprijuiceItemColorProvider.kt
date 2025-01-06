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
    override fun getColor(stack: ItemStack, layer: Int): Int {
        if (layer == 0) return -1

        val cookingComponent = stack.get(CobblemonItemComponents.COOKING_COMPONENT) ?: return -1
        val dominantFlavor = cookingComponent.getDominantFlavor()

        val color = when (dominantFlavor) {
            "spicy" -> ChatFormatting.RED.color
            "dry" -> ChatFormatting.BLUE.color
            "sweet" -> ChatFormatting.LIGHT_PURPLE.color
            "bitter" -> ChatFormatting.GREEN.color
            "sour" -> ChatFormatting.YELLOW.color
            else -> -1
        }

        return color?.let { FastColor.ARGB32.opaque(it) } ?: -1
    }
}