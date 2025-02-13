/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.color

import com.cobblemon.mod.common.CobblemonItemComponents
import net.minecraft.client.color.item.ItemColor
import net.minecraft.world.item.ItemStack
import com.cobblemon.mod.common.api.cooking.getColor

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
        } ?: -1

        return color
    }
}