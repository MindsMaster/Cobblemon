/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.spawning.condition

import com.cobblemon.mod.common.api.conditional.RegistryLikeCondition
import com.cobblemon.mod.common.api.spawning.context.BaitSpawningContext
import com.cobblemon.mod.common.api.spawning.context.GroundedSpawningContext
import com.cobblemon.mod.common.util.itemRegistry
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Item
import net.minecraft.world.item.enchantment.EnchantmentHelper
import net.minecraft.world.item.enchantment.Enchantments
import net.minecraft.world.level.block.Block

/**
 * A spawning condition that applies to [BaitSpawningContext]s.
 *
 * @author Hiroku
 * @since February 3rd, 2024
 */
class BaitSpawningCondition: SpawningCondition<BaitSpawningContext>() {
    override fun contextClass() = BaitSpawningContext::class.java

    var neededNearbyBlocks: MutableList<RegistryLikeCondition<Block>>? = null
    var baitType: ResourceLocation? = null

    override fun fits(ctx: BaitSpawningContext): Boolean {
        if (!super.fits(ctx)) {
            return false
        } else if (neededNearbyBlocks != null && neededNearbyBlocks!!.none { cond -> ctx.nearbyBlockTypes.any { cond.fits(it, ctx.blockRegistry) } }) {
            return false
        }

        if (baitType != null) { // check for the type of bait being used
            val baitItem = ctx.baitItem
            if (baitItem != baitType) {
                return false
            }
        }

        /*if (ctx is FishingSpawningContext && (ctx as FishingSpawningContext).rodItem != null) { // check if the bait attracts certain EV yields
            val pokerodItem = (ctx as FishingSpawningContext).rodItem

            // todo check if the EV yield of the berry matches the bait EV attract maybe?

            if (// todo if bait EV yield != EV yield of pokemon consideration        //Registries.ITEM.getId(pokerodItem?.bait?.item).path == )
                return false
        }*/

        return true
    }

    companion object {
        const val NAME = "bait"
    }
}