/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.block

import com.cobblemon.mod.common.CobblemonItemComponents
import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.cobblemon.mod.common.api.events.fishing.BaitConsumedEvent
import com.cobblemon.mod.common.api.events.fishing.BaitSetEvent
import com.cobblemon.mod.common.api.fishing.SpawnBait
import com.cobblemon.mod.common.api.fishing.FishingBaits
import com.cobblemon.mod.common.block.entity.CakeBlockEntity.Companion.MAX_NUMBER_OF_BITES
import com.cobblemon.mod.common.block.entity.LureCakeBlockEntity
import com.cobblemon.mod.common.item.RodBaitComponent
import com.cobblemon.mod.common.item.components.CookingComponent
import com.mojang.serialization.MapCodec
import net.minecraft.core.BlockPos
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.RandomSource
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState

class LureCakeBlock(settings: Properties): CakeBlock(settings) {
    companion object {
        fun getBaitOnLureCake(stack: ItemStack): SpawnBait? {
            return getCookingComponentOnRod(stack) ?: stack.components.get(CobblemonItemComponents.BAIT)?.bait
        }

        fun getBaitStackOnRod(stack: ItemStack): ItemStack {
            return stack.components.get(CobblemonItemComponents.BAIT)?.stack ?: ItemStack.EMPTY
        }

        fun getCookingComponentOnRod(rodStack: ItemStack): SpawnBait? {
            // Check if the stack within the RodBaitComponent has a CookingComponent
            val cookingComponent = rodStack.get(CobblemonItemComponents.COOKING_COMPONENT) ?: return null

            // Combine effects from the CookingComponent
            val combinedEffects = listOf(
                cookingComponent.bait1.effects,
                cookingComponent.bait2.effects,
                cookingComponent.bait3.effects
            ).flatten()

            // Return a new FishingBait with combined effects
            return SpawnBait(
                item = BuiltInRegistries.ITEM.getKey(
                    rodStack.components.get(CobblemonItemComponents.BAIT)?.stack?.item ?: ItemStack.EMPTY.item
                ), // Use the rodStack's item as the bait identifier
                effects = combinedEffects
            )
        }

        fun getBaitEffects(stack: ItemStack): List<SpawnBait.Effect> {
            return getBaitOnLureCake(stack)?.effects ?: return emptyList()
        }
    }

    override fun codec(): MapCodec<LureCakeBlock> {
        return simpleCodec(::LureCakeBlock)
    }

    override fun newBlockEntity(pos: BlockPos, state: BlockState): BlockEntity {
        return LureCakeBlockEntity(pos, state)
    }
}
