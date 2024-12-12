/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.block

import com.cobblemon.mod.common.CobblemonBlockEntities
import com.cobblemon.mod.common.block.entity.CampfireBlockEntity
import com.cobblemon.mod.common.block.entity.DisplayCaseBlockEntity
import com.cobblemon.mod.common.item.PokeBallItem
import com.cobblemon.mod.common.item.PotItem
import com.mojang.serialization.MapCodec
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.MenuProvider
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.level.Level
import net.minecraft.world.level.LevelAccessor
import net.minecraft.world.level.block.BaseEntityBlock
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.HorizontalDirectionalBlock.FACING
import net.minecraft.world.level.block.RenderShape
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityTicker
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.world.level.block.state.properties.DirectionProperty
import net.minecraft.world.level.pathfinder.PathComputationType
import net.minecraft.world.phys.BlockHitResult
import org.jetbrains.annotations.Nullable

@Suppress("OVERRIDE_DEPRECATION")
class CampfireBlock(settings: Properties) : BaseEntityBlock(settings) {

    init {
        registerDefaultState(stateDefinition.any()
            .setValue(FACING, Direction.NORTH)
            .setValue(LIT, true)
            .setValue(ITEM_DIRECTION, Direction.NORTH))
    }

    override fun getStateForPlacement(ctx: BlockPlaceContext): BlockState? {
        var blockState = defaultBlockState()
        val worldView = ctx.level
        val blockPos = ctx.clickedPos
        ctx.nearestLookingDirections.forEach { direction ->
            if (direction.axis.isHorizontal) {
                blockState = blockState
                    .setValue(FACING, direction)
                    .setValue(ITEM_DIRECTION, direction)
                        as BlockState
                if (blockState.canSurvive(worldView, blockPos)) {
                    return blockState
                }
            }
        }
        return null
    }

    override fun useWithoutItem(
        state: BlockState,
        level: Level,
        pos: BlockPos,
        player: Player,
        hitResult: BlockHitResult
    ): InteractionResult? {
        val blockEntity = level.getBlockEntity(pos)
        if (blockEntity is CampfireBlockEntity) {
            val itemStack = blockEntity.getItemStack()
            if (!itemStack.isEmpty && itemStack.item is PotItem) {
                if (!level.isClientSide) {
                    if (player.isCrouching) {
                        takeStoredItem(blockEntity, state, level, pos, player)
                    } else {
                        this.openContainer(level, pos, player)
                    }
                }
            }
        }
        return InteractionResult.CONSUME
    }

    private fun takeStoredItem(blockEntity: CampfireBlockEntity, blockState: BlockState, level: Level, blockPos: BlockPos, player: Player) {
        if (player.getItemInHand(InteractionHand.MAIN_HAND).isEmpty) {
            player.setItemInHand(InteractionHand.MAIN_HAND, blockEntity.removeItemStack())
            blockEntity.setRemoved()
            val facing = blockState.getValue(FACING)
            val newBlockState = Blocks.CAMPFIRE.defaultBlockState().setValue(FACING, facing)
            level.setBlockAndUpdate(blockPos, newBlockState)
            level.playSound(null, blockPos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.7F, 1.0F);
        }
    }


    fun openContainer(level: Level, pos: BlockPos, player: Player) {
        var blockEntity = level.getBlockEntity(pos)
        if (blockEntity is CampfireBlockEntity) {
            player.openMenu(blockEntity as MenuProvider)
        }
    }

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
        builder.add(FACING)
        builder.add(ITEM_DIRECTION)
        builder.add(LIT)
    }

    override fun updateShape(
        state: BlockState,
        direction: Direction,
        neighborState: BlockState,
        world: LevelAccessor,
        pos: BlockPos,
        neighborPos: BlockPos
    ): BlockState {
        return if (direction == state.getValue(FACING) && !state.canSurvive(world, pos)) Blocks.AIR.defaultBlockState()
        else super.updateShape(state, direction, neighborState, world, pos, neighborPos)
    }

    override fun getRenderShape(state: BlockState) = RenderShape.MODEL

    override fun getAnalogOutputSignal(state: BlockState, world: Level, pos: BlockPos): Int {
        val stack = (world.getBlockEntity(pos) as DisplayCaseBlockEntity).getStack()

        if (stack.isEmpty) return 0
        if (stack.item is PokeBallItem) return 3
        if (stack.item is BlockItem) return 2
        return 1
    }

    override fun hasAnalogOutputSignal(state: BlockState): Boolean = true

    override fun codec(): MapCodec<out BaseEntityBlock> {
        return CODEC
    }

    override fun isPathfindable(state: BlockState, type: PathComputationType): Boolean = false

    companion object {
        val CODEC = simpleCodec(::CampfireBlock)
        val ITEM_DIRECTION = DirectionProperty.create("item_facing")
        val LIT = BlockStateProperties.LIT
    }

    override fun <T : BlockEntity?> getTicker(
        level: Level,
        state: BlockState,
        blockEntityType: BlockEntityType<T?>
    ): BlockEntityTicker<T?>? {
        return createCookingPotTicker(level, blockEntityType as BlockEntityType<*>, CobblemonBlockEntities.CAMPFIRE) as BlockEntityTicker<T?>?
    }

    @Nullable
    protected fun <T : BlockEntity> createCookingPotTicker(
        level: Level,
        serverType: BlockEntityType<T>,
        clientType: BlockEntityType<out CampfireBlockEntity>
    ): BlockEntityTicker<T>? {
        return if (level.isClientSide) null else createTickerHelper(serverType, clientType, CampfireBlockEntity::serverTick)
    }

    override fun newBlockEntity(
        pos: BlockPos,
        state: BlockState
    ): BlockEntity? {
        return CampfireBlockEntity(pos, state)
    }

}