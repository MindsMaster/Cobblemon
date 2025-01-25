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
import com.cobblemon.mod.common.api.fishing.FishingBait
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
        fun getBaitOnLureCake(stack: ItemStack): FishingBait? {
            return getCookingComponentOnRod(stack) ?: stack.components.get(CobblemonItemComponents.BAIT)?.bait
        }

        fun getBaitStackOnRod(stack: ItemStack): ItemStack {
            return stack.components.get(CobblemonItemComponents.BAIT)?.stack ?: ItemStack.EMPTY
        }

        fun getCookingComponentOnRod(rodStack: ItemStack): FishingBait? {
            // Check if the stack within the RodBaitComponent has a CookingComponent
            val cookingComponent = rodStack.get(CobblemonItemComponents.COOKING_COMPONENT) ?: return null

            // Combine effects from the CookingComponent
            val combinedEffects = listOf(
                cookingComponent.bait1.effects,
                cookingComponent.bait2.effects,
                cookingComponent.bait3.effects
            ).flatten()

            // Return a new FishingBait with combined effects
            return FishingBait(
                item = BuiltInRegistries.ITEM.getKey(
                    rodStack.components.get(CobblemonItemComponents.BAIT)?.stack?.item ?: ItemStack.EMPTY.item
                ), // Use the rodStack's item as the bait identifier
                effects = combinedEffects
            )
        }

        fun setBait(stack: ItemStack, bait: ItemStack) {
            CobblemonEvents.BAIT_SET.postThen(BaitSetEvent(stack, bait), { event -> }, {
                if (bait.isEmpty) {
                    stack.set<RodBaitComponent>(CobblemonItemComponents.BAIT, null)
                    stack.set<CookingComponent>(CobblemonItemComponents.COOKING_COMPONENT, null)
                    return
                }

                // Retrieve FishingBait and CookingComponent from the bait ItemStack
                val fishingBait = FishingBaits.getFromBaitItemStack(bait) ?: return
                val cookingComponent = bait.get(CobblemonItemComponents.COOKING_COMPONENT)

                // Apply both RodBaitComponent and CookingComponent to the rod ItemStack
                stack.set(CobblemonItemComponents.BAIT, RodBaitComponent(fishingBait, bait))
                if (cookingComponent != null) {
                    stack.set(CobblemonItemComponents.COOKING_COMPONENT, cookingComponent)
                } else {
                    // Clear CookingComponent if the new bait does not have it
                    stack.set<CookingComponent>(CobblemonItemComponents.COOKING_COMPONENT, null)
                }
            })
        }

        fun consumeBait(stack: ItemStack) {
            CobblemonEvents.BAIT_CONSUMED.postThen(BaitConsumedEvent(stack), { event -> }, {
                val baitStack = getBaitStackOnRod(stack)
                val baitCount = baitStack.count
                val cookingComponent = stack.get(CobblemonItemComponents.COOKING_COMPONENT)

                if (baitCount == 1) {
                    stack.set<RodBaitComponent>(CobblemonItemComponents.BAIT, null)
                    stack.set<CookingComponent>(CobblemonItemComponents.COOKING_COMPONENT, null)
                    return
                }

                if (baitCount > 1) {
                    val fishingBait = getBaitOnLureCake(stack) ?: return
                    stack.set<RodBaitComponent>(
                        CobblemonItemComponents.BAIT,
                        RodBaitComponent(fishingBait, ItemStack(baitStack.item, baitCount - 1))
                    )
                    if (cookingComponent != null) {
                        stack.set(CobblemonItemComponents.COOKING_COMPONENT, cookingComponent)
                    }
                }
            })
        }

        fun getBaitEffects(stack: ItemStack): List<FishingBait.Effect> {
            return getBaitOnLureCake(stack)?.effects ?: return emptyList()
        }
    }

    override fun codec(): MapCodec<LureCakeBlock> {
        return simpleCodec(::LureCakeBlock)
    }

    override fun randomTick(state: BlockState, world: ServerLevel, pos: BlockPos, random: RandomSource) {
        println("randomTick triggered for LureCakeBlock at $pos")

        // Check if block has a BlockEntity
        val blockEntity = world.getBlockEntity(pos) as? LureCakeBlockEntity
        if (blockEntity == null) {
            println("No LureCakeBlockEntity found at $pos")
            return
        }

        // Attempt to spawn Pokémon in a 5-block radius
        val randomXOffset = random.nextInt(-5, 6)
        val randomZOffset = random.nextInt(-5, 6)
        val spawnPos = pos.offset(randomXOffset, 0, randomZOffset)

        println("Attempting to spawn Pokémon near $pos at $spawnPos")
        val spawnedPokemon = blockEntity.spawnPokemon(world, spawnPos)
        if (spawnedPokemon != null) {
            println("Pokémon spawned successfully at $spawnPos")

            // Increment the age of the block
            val newBites = getBites(world, pos) + 1
            if (newBites >= MAX_NUMBER_OF_BITES) {
                println("Block at $pos has reached max age. Breaking block.")
                world.destroyBlock(pos, true) // Break the block and drop its items
            } else {
                setBites(world, pos, newBites)
                println("Block at $pos aged to $newBites")
            }

            world.sendBlockUpdated(pos, state, state, UPDATE_CLIENTS)
        }
    }

    override fun isRandomlyTicking(state: BlockState): Boolean {
        return true
    }

    override fun newBlockEntity(pos: BlockPos, state: BlockState): BlockEntity {
        return LureCakeBlockEntity(pos, state)
    }
}
