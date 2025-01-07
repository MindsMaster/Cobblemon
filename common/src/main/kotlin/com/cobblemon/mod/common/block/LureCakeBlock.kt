/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.block

import com.cobblemon.mod.common.CobblemonBlockEntities
import com.cobblemon.mod.common.CobblemonItemComponents
import com.cobblemon.mod.common.CobblemonSounds
import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.cobblemon.mod.common.api.events.fishing.BaitConsumedEvent
import com.cobblemon.mod.common.api.events.fishing.BaitSetEvent
import com.cobblemon.mod.common.api.fishing.FishingBait
import com.cobblemon.mod.common.api.fishing.FishingBaits
import com.cobblemon.mod.common.block.entity.LureCakeBlockEntity
import com.cobblemon.mod.common.item.RodBaitComponent
import com.cobblemon.mod.common.item.components.CookingComponent
import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.BlockPos
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.RandomSource
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.Item.Properties
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Level
import net.minecraft.world.level.LevelReader
import net.minecraft.world.level.block.BaseEntityBlock
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.RenderShape
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityTicker
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.world.level.block.state.properties.IntegerProperty
import java.util.*
import kotlin.collections.flatten

class LureCakeBlock(settings: BlockBehaviour.Properties): BaseEntityBlock(settings) {

    companion object {
        val AGE: IntegerProperty = BlockStateProperties.AGE_5

        //val CODEC = simpleCodec(::LureCakeBlock)
        val CODEC: Codec<LureCakeBlockEntity> = RecordCodecBuilder.create { instance ->
            instance.group(
                    BlockPos.CODEC.fieldOf("pos").forGetter { it.blockPos },
                    BlockState.CODEC.fieldOf("state").forGetter { it.blockState },
                    CookingComponent.CODEC.optionalFieldOf("cookingComponent").forGetter { Optional.ofNullable(it.cookingComponent) }
            ).apply(instance) { pos, state, cookingComponentOpt ->
                LureCakeBlockEntity(pos, state).apply {
                    cookingComponent = cookingComponentOpt.orElse(null)
                }
            }
        }

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

    init {
        // Default state with age set to 0
        registerDefaultState(this.stateDefinition.any().setValue(AGE, 0))
    }

    override fun codec(): MapCodec<LureCakeBlock> {
        return simpleCodec(::LureCakeBlock)
    }

    override fun setPlacedBy(
            level: Level,
            pos: BlockPos,
            state: BlockState,
            placer: LivingEntity?,
            stack: ItemStack
    ) {
        super.setPlacedBy(level, pos, state, placer, stack)

        val blockEntity = level.getBlockEntity(pos) as? LureCakeBlockEntity
        if (blockEntity != null) {
            println("Transferring CookingComponent data from ItemStack to BlockEntity at $pos")
            blockEntity.initializeFromItemStack(stack)
        } else {
            println("No BlockEntity found at $pos for LureCakeBlock")
        }
    }



    override fun getCloneItemStack(level: LevelReader, pos: BlockPos, state: BlockState): ItemStack {
        val blockEntity = level.getBlockEntity(pos) as? LureCakeBlockEntity ?: return ItemStack.EMPTY
        return blockEntity.toItemStack()
    }
/*
    override fun codec(): MapCodec<out BaseEntityBlock> {
        return CODEC
    }*/

    /*override fun codec(): MapCodec<LureCakeBlock> {
        return CODEC
    }*/

    override fun randomTick(state: BlockState, world: ServerLevel, pos: BlockPos, random: RandomSource) {
        println("randomTick triggered for LureCakeBlock at $pos")

        // Check if block has a BlockEntity
        val blockEntity = world.getBlockEntity(pos) as? LureCakeBlockEntity
        if (blockEntity == null) {
            println("No LureCakeBlockEntity found at $pos")
            return
        }

        // Check if the block is fully aged
        val currentAge = state.getValue(AGE)
        if (currentAge >= 5) {
            println("Block at $pos is fully aged. Breaking block.")
            world.destroyBlock(pos, true) // Break the block and drop its items
            return
        }

        // Attempt to spawn Pokémon in a 5-block radius
        val randomXOffset = random.nextInt(-5, 6)
        val randomZOffset = random.nextInt(-5, 6)
        val spawnPos = pos.offset(randomXOffset, 0, randomZOffset)

        println("Attempting to spawn Pokémon near $pos at $spawnPos")
        val (spawnedPokemon, _) = blockEntity.spawnPokemon(world, spawnPos)
        if (spawnedPokemon != null) {
            println("Pokémon spawned successfully at $spawnPos")

            // Increment the age of the block
            val newAge = currentAge + 1
            if (newAge >= 5) {
                println("Block at $pos has reached max age. Breaking block.")
                world.destroyBlock(pos, true) // Break the block and drop its items
            } else {
                world.setBlock(pos, state.setValue(AGE, newAge), Block.UPDATE_ALL)
                println("Block at $pos aged to $newAge")
            }
        } else {
            println("No Pokémon spawned at $spawnPos")
        }
    }



    override fun isRandomlyTicking(state: BlockState): Boolean {
        // Enable random ticking for this block
        return true
    }

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
        // Register the AGE property
        builder.add(AGE)
    }

    override fun newBlockEntity(pos: BlockPos, state: BlockState): BlockEntity {
        println("Creating new LureCakeBlockEntity at $pos")
        return LureCakeBlockEntity(pos, state)
    }

    override fun getRenderShape(state: BlockState): RenderShape {
        return RenderShape.MODEL
    }
}
