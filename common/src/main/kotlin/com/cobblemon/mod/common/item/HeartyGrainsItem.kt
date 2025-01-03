package com.cobblemon.mod.common.item

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.block.HeartyGrainsBlock
import net.minecraft.core.BlockPos
import net.minecraft.tags.FluidTags
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemNameBlockItem
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.PlaceOnWaterBlockItem
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.item.context.UseOnContext
import net.minecraft.world.level.ClipContext
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockState

class HeartyGrainsItem(block: HeartyGrainsBlock) : ItemNameBlockItem(block, Properties()) {

    init {
        Cobblemon.implementation.registerCompostable(this, 0.65F)
    }

    override fun useOn(context: UseOnContext): InteractionResult {
        val world = context.level
        val pos = context.clickedPos
        val player = context.player ?: return InteractionResult.FAIL

        if (world.isClientSide) {
            return InteractionResult.SUCCESS
        }

        // Check if placement is on valid land
        val blockBelow = world.getBlockState(pos)
        if (canPlaceOnLand(world, pos, blockBelow)) {
            return placeOnLand(context, pos.above())
        }

        // Otherwise, check for water placement
        if (canPlaceInWater(world, pos)) {
            return placeInWater(context, pos)
        }

        return InteractionResult.PASS
    }

    private fun placeOnLand(context: UseOnContext, targetPos: BlockPos): InteractionResult {
        val world = context.level
        val targetState = this.block.defaultBlockState()

        if (world.setBlock(targetPos, targetState, Block.UPDATE_CLIENTS)) {
            context.itemInHand.shrink(1)
            return InteractionResult.SUCCESS
        }
        return InteractionResult.FAIL
    }

    private fun placeInWater(context: UseOnContext, targetPos: BlockPos): InteractionResult {
        val world = context.level
        val targetState = this.block.defaultBlockState()

        if (world.setBlock(targetPos, targetState, Block.UPDATE_CLIENTS)) {
            context.itemInHand.shrink(1)
            return InteractionResult.SUCCESS
        }
        return InteractionResult.FAIL
    }

    override fun use(world: Level, user: Player, hand: InteractionHand): InteractionResultHolder<ItemStack> {
        val blockHitResult = PlaceOnWaterBlockItem.getPlayerPOVHitResult(world, user, ClipContext.Fluid.SOURCE_ONLY)
        val stack = user.getItemInHand(hand)

        if (world.isClientSide) {
            return InteractionResultHolder.success(stack)
        }

        val pos = blockHitResult.blockPos
        val blockBelow = world.getBlockState(pos)

        if (canPlaceOnLand(world, pos, blockBelow)) {
            if (world.setBlock(pos.above(), this.block.defaultBlockState(), Block.UPDATE_CLIENTS)) {
                stack.shrink(1)
                return InteractionResultHolder.success(stack)
            }
        }

        if (canPlaceInWater(world, pos)) {
            if (world.setBlock(pos, this.block.defaultBlockState(), Block.UPDATE_CLIENTS)) {
                stack.shrink(1)
                return InteractionResultHolder.success(stack)
            }
        }

        return InteractionResultHolder.fail(stack)
    }

    private fun canPlaceOnLand(world: Level, pos: BlockPos, blockBelow: BlockState): Boolean {
        return blockBelow.`is`(Blocks.FARMLAND) || blockBelow.`is`(Blocks.MUD) || blockBelow.`is`(Blocks.DIRT) ||
                blockBelow.`is`(Blocks.GRASS_BLOCK) || blockBelow.`is`(Blocks.COARSE_DIRT) ||
                blockBelow.`is`(Blocks.SAND)
    }

    private fun canPlaceInWater(world: Level, pos: BlockPos): Boolean {
        val fluidState = world.getFluidState(pos)
        val blockAbove = world.getBlockState(pos.above())

        return fluidState.`is`(FluidTags.WATER) && fluidState.isSource && blockAbove.isAir
    }
}