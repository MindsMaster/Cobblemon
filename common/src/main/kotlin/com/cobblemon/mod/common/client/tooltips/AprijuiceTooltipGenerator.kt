/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.tooltips

import com.cobblemon.mod.common.CobblemonItemComponents
import com.cobblemon.mod.common.item.AprijuiceItem
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack

object AprijuiceTooltipGenerator : TooltipGenerator() {
    override fun generateCategoryTooltip(stack: ItemStack, lines: MutableList<Component>): MutableList<Component>? {
        if (stack.item !is AprijuiceItem) return null
        return mutableListOf(seasoningHeader)
    }

    override fun generateAdditionalTooltip(stack: ItemStack, lines: MutableList<Component>): MutableList<Component>? {
        if (stack.item !is AprijuiceItem) return null

        val flavors: Map<String, Int>? = stack.get(CobblemonItemComponents.COOKING_COMPONENT)?.getFlavorsSum()
        flavors?.let {
            return generateAdditionalFlavorTooltip(flavors)
        }

        return null
    }
}