/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.block

import com.cobblemon.mod.common.CobblemonBlockEntities
import com.cobblemon.mod.common.CobblemonSounds
import com.cobblemon.mod.common.block.entity.CampfireBlockEntity
import com.cobblemon.mod.common.block.entity.DisplayCaseBlockEntity
import com.cobblemon.mod.common.item.PokeBallItem
import com.cobblemon.mod.common.item.PotItem
import com.cobblemon.mod.common.util.playSoundServer
import com.cobblemon.mod.common.util.toVec3d
import com.mojang.serialization.MapCodec
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.ItemStack
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
    ): InteractionResult {
        val blockEntity = level.getBlockEntity(pos)
        if (blockEntity is CampfireBlockEntity) {
            val potItem = blockEntity.getPotItem()
            if (!potItem?.isEmpty!! && potItem.item is PotItem) {
                if (!level.isClientSide) {
                    if (player.isCrouching) {
                        // Remove the pot item and give it to the player
                        removePotItem(blockEntity, state, level, pos, player)
                    } else {
                        // Open the cooking pot's container
                        openContainer(level, pos, player)
                    }
                }
                return InteractionResult.SUCCESS
            } else if (player.getItemInHand(InteractionHand.MAIN_HAND).item is PotItem) {
                // Add the pot item to the block entity
                if (potItem.isEmpty) {
                    val heldItem = player.getItemInHand(InteractionHand.MAIN_HAND)
                    blockEntity.setPotItem(heldItem.split(1))
                    level.playSoundServer(
                        position = pos.bottomCenter,
                        sound = CobblemonSounds.CAMPFIRE_POT_PLACE,
                    )
                    return InteractionResult.SUCCESS
                }
            }
        }
        return InteractionResult.PASS
    }

    private fun removePotItem(
        blockEntity: CampfireBlockEntity,
        blockState: BlockState,
        level: Level,
        blockPos: BlockPos,
        player: Player
    ) {
        if (player.getItemInHand(InteractionHand.MAIN_HAND).isEmpty) {
            val potItem = blockEntity.getPotItem()
            player.setItemInHand(InteractionHand.MAIN_HAND, potItem)
            blockEntity.setPotItem(ItemStack.EMPTY)
            level.playSoundServer(
                position = blockPos.bottomCenter,
                sound = CobblemonSounds.CAMPFIRE_POT_RETRIEVE,
            )
        }
    }

    fun openContainer(level: Level, pos: BlockPos, player: Player) {
        var blockEntity = level.getBlockEntity(pos)
        if (blockEntity is CampfireBlockEntity) {
            player.openMenu(blockEntity as CampfireBlockEntity)
            level.playSoundServer(
                position = pos.bottomCenter,
                sound = CobblemonSounds.CAMPFIRE_POT_OPEN,
            )
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
        return if (level.isClientSide) createTickerHelper(serverType, clientType, CampfireBlockEntity::clientTick)
            else createTickerHelper(serverType, clientType, CampfireBlockEntity::serverTick)
    }

    override fun newBlockEntity(
        pos: BlockPos,
        state: BlockState
    ): BlockEntity? {
        return CampfireBlockEntity(pos, state)
    }
}