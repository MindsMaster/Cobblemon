/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.block

import com.cobblemon.mod.common.CobblemonBlocks
import com.mojang.serialization.MapCodec
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.dispenser.DispenseItemBehavior
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.InteractionHand
import net.minecraft.world.ItemInteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.alchemy.PotionContents
import net.minecraft.world.item.alchemy.Potions
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.DirectionalBlock
import net.minecraft.world.level.block.DispenserBlock
import net.minecraft.world.level.block.RotatedPillarBlock
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.phys.BlockHitResult

class SaccharineLogSlatheredBlock(properties: Properties) : DirectionalBlock(properties) {
    companion object {
        val CODEC: MapCodec<SaccharineLogSlatheredBlock> = simpleCodec(::SaccharineLogSlatheredBlock)

        fun createBehavior(): DispenseItemBehavior {
            return DispenseItemBehavior { source, stack ->
                val level = source.level
                val pos = source.pos.relative(source.state.getValue(DispenserBlock.FACING))
                val blockState = level.getBlockState(pos)

                val waterBottle = PotionContents.createItemStack(Items.POTION, Potions.WATER).item

                if (blockState.block is SaccharineLogSlatheredBlock && stack.`is`(waterBottle)) {
                    val newState = CobblemonBlocks.SACCHARINE_LOG.defaultBlockState()
                        .setValue(RotatedPillarBlock.AXIS, blockState.getValue(RotatedPillarBlock.AXIS))
                    SaccharineLogBlock.changeLogTypeDispenser(level, pos, newState, stack, source)
                }
                stack
            }
        }
    }

    init {
        registerDefaultState(
            stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y)
        )
    }

    override fun codec() = CODEC

    override fun useItemOn(stack: ItemStack, state: BlockState, level: Level, pos: BlockPos, player: Player, hand: InteractionHand, hitResult: BlockHitResult): ItemInteractionResult {
        val itemStack = player.getItemInHand(hand)
        val waterBottle = PotionContents.createItemStack(Items.POTION, Potions.WATER).item
        if (itemStack.`is`(waterBottle)) {
            if (!level.isClientSide) {
                // Replace the honey with the block variant
                val newState = CobblemonBlocks.SACCHARINE_LOG.defaultBlockState()
                    .setValue(RotatedPillarBlock.AXIS, state.getValue(RotatedPillarBlock.AXIS))
                level.playSound(null, pos, SoundEvents.GENERIC_SWIM, SoundSource.BLOCKS)
                SaccharineLogBlock.changeLogType(level, pos, newState, player, itemStack)
            }
            spawnSplashParticles(level, pos)
            return ItemInteractionResult.SUCCESS
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hitResult)
    }

    override fun getStateForPlacement(context: BlockPlaceContext): BlockState? {
        val direction = context.player?.direction ?: Direction.NORTH
        if (direction != Direction.UP && direction != Direction.DOWN) {
            return defaultBlockState().setValue(FACING, direction)
        }
        return super.getStateForPlacement(context)
    }

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
        builder.add(FACING)
        builder.add(RotatedPillarBlock.AXIS)
    }

    fun spawnSplashParticles(level: Level, pos: BlockPos) {
        for (i in 0 until 50) {
            val offsetX: Double = (level.random.nextDouble() * 1.4) - 0.7
            val offsetY: Double = (level.random.nextDouble() * 1.4) - 0.7
            val offsetZ: Double = (level.random.nextDouble() * 1.4) - 0.7

            level.addParticle(
                ParticleTypes.SPLASH,
                pos.x + 0.5 + offsetX,
                pos.y + 0.5 + offsetY,
                pos.z + 0.5 + offsetZ,
                0.0, 0.0, 0.0
            )
        }
    }
}
