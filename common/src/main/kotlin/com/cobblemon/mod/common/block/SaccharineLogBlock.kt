/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.block

import com.cobblemon.mod.common.CobblemonBlocks
import com.cobblemon.mod.common.api.tags.CobblemonItemTags
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.dispenser.DispenseItemBehavior
import net.minecraft.world.Containers
import net.minecraft.world.InteractionHand
import net.minecraft.world.ItemInteractionResult
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Level
import net.minecraft.world.level.LevelAccessor
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.DispenserBlock
import net.minecraft.world.level.block.RotatedPillarBlock
import net.minecraft.world.level.block.entity.DispenserBlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.world.level.block.state.properties.EnumProperty
import net.minecraft.world.level.gameevent.GameEvent
import net.minecraft.world.level.pathfinder.PathComputationType
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.EntityCollisionContext
import net.minecraft.world.phys.shapes.Shapes
import net.minecraft.world.phys.shapes.VoxelShape

class SaccharineLogBlock(properties: Properties) : RotatedPillarBlock(properties) {

    override fun useItemOn(
        stack: ItemStack,
        state: BlockState,
        level: Level,
        pos: BlockPos,
        player: Player,
        hand: InteractionHand,
        hitResult: BlockHitResult
    ): ItemInteractionResult? {
        val itemStack = player.getItemInHand(hand)
        if (itemStack.`is`(Items.HONEY_BOTTLE)) {
            if (!level.isClientSide) {
                // Replace the block with the honey variant
                val newState = CobblemonBlocks.SACCHARINE_HONEY_LOG.defaultBlockState()
                    .setValue(RotatedPillarBlock.AXIS, state.getValue(RotatedPillarBlock.AXIS))
                level.setBlock(pos, newState, 3)
                level.gameEvent(null, GameEvent.BLOCK_CHANGE, pos)

                // Consume honey bottle
                if (!player.isCreative) {
                    itemStack.shrink(1)

                    // Give glass bottle
                    val glassBottle = ItemStack(Items.GLASS_BOTTLE)
                    if (!player.addItem(glassBottle)) {
                        player.drop(glassBottle, false)
                    }
                }
            }
            return ItemInteractionResult.SUCCESS
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hitResult)
    }

    companion object {
        val AXIS: EnumProperty<Direction.Axis> = BlockStateProperties.AXIS

        private val SHAPE: VoxelShape = Shapes.block()

        fun createBehavior(): DispenseItemBehavior {
            return DispenseItemBehavior { source, stack ->
                val level = source.level
                val facing = source.state.getValue(DispenserBlock.FACING)
                val pos = source.pos.relative(facing)
                val blockState = level.getBlockState(pos)

                if (blockState.block is SaccharineLogBlock) {
                    val newState = CobblemonBlocks.SACCHARINE_HONEY_LOG.defaultBlockState()
                        .setValue(AXIS, blockState.getValue(AXIS))
                    level.setBlock(pos, newState, 3)
                    level.gameEvent(null, GameEvent.BLOCK_CHANGE, pos)

                    stack.shrink(1)

                    val dispenserEntity = source.blockEntity
                    if (dispenserEntity != null) {
                        val glassBottle = ItemStack(Items.GLASS_BOTTLE)
                        var added = false

                        for (i in 0 until dispenserEntity.containerSize) {
                            val slotStack = dispenserEntity.getItem(i)

                            if (slotStack.isEmpty) {
                                dispenserEntity.setItem(i, glassBottle.copy())
                                added = true
                                break
                            } else if (slotStack.`is`(Items.GLASS_BOTTLE) && slotStack.count < slotStack.maxStackSize) {
                                slotStack.grow(1)
                                added = true
                                break
                            }
                        }

                        if (!added) {
                            Containers.dropItemStack(
                                level,
                                source.pos.x.toDouble(), source.pos.y.toDouble(), source.pos.z.toDouble(), glassBottle
                            )
                        }
                    }
                }

                stack
            }
        }

    }
}
