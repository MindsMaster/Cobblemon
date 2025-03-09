/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.block.entity

import com.cobblemon.mod.common.CobblemonBlockEntities
import com.cobblemon.mod.common.api.fishing.SpawnBait
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.state.BlockState

class LureCakeBlockEntity(
    pos: BlockPos,
    state: BlockState
) : CakeBlockEntity(CobblemonBlockEntities.LURE_CAKE, pos, state) {

    /**
     * Generate a `FishingBait` by combining effects from all `RodBaitComponent` data in the `CookingComponent`.
     */
    fun getBaitFromLureCake(): SpawnBait? {
        val component = cookingComponent ?: return null
        val combinedEffects = listOf(
            component.bait1.effects,
            component.bait2.effects,
            component.bait3.effects
        ).flatten()

        return SpawnBait(
            item = cobblemonResource("lure_cake"), // Directly specify the lure_cake ResourceLocation
            effects = combinedEffects
        )
    }
}