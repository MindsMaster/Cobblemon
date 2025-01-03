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

        val blockBelow = world.getBlockState(pos)
        val fluidState = world.getFluidState(pos)

        // Prioritize placement in 1-block deep source water
        if (fluidState.`is`(FluidTags.WATER) && fluidState.isSource) {
            val blockAbove = world.getBlockState(pos.above())
            if (blockAbove.isAir) { // Ensure it's only 1 block deep
                if (world.setBlock(pos, this.block.defaultBlockState().setValue(HeartyGrainsBlock.WATERLOGGED, true), Block.UPDATE_CLIENTS)) {
                    context.itemInHand.shrink(1)
                    return InteractionResult.SUCCESS
                }
            }
        }

        // Fallback to placement on farmland or mud
        if (blockBelow.`is`(Blocks.FARMLAND) || blockBelow.`is`(Blocks.MUD)) {
            if (world.setBlock(pos.above(), this.block.defaultBlockState(), Block.UPDATE_CLIENTS)) {
                context.itemInHand.shrink(1)
                return InteractionResult.SUCCESS
            }
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
        val targetState = this.block.defaultBlockState().setValue(HeartyGrainsBlock.WATERLOGGED, true)

        if (world.setBlock(targetPos, targetState, Block.UPDATE_CLIENTS)) {
            context.itemInHand.shrink(1)
            return InteractionResult.SUCCESS
        }
        return InteractionResult.FAIL
    }


    // todo is this needed like this? should it be simpler?
    override fun use(world: Level, user: Player, hand: InteractionHand): InteractionResultHolder<ItemStack> {
        // Target both solid blocks and water
        val blockHitResult = PlaceOnWaterBlockItem.getPlayerPOVHitResult(world, user, ClipContext.Fluid.ANY)
        val stack = user.getItemInHand(hand)

        if (world.isClientSide) {
            return InteractionResultHolder.success(stack)
        }

        val pos = blockHitResult.blockPos
        val blockBelow = world.getBlockState(pos)
        val fluidState = world.getFluidState(pos)

        // Prioritize placement in 1-block deep source water
        if (fluidState.`is`(FluidTags.WATER) && fluidState.isSource) {
            val blockAbove = world.getBlockState(pos.above())
            if (blockAbove.isAir) { // Ensure it's only 1 block deep
                if (world.setBlock(pos, this.block.defaultBlockState().setValue(HeartyGrainsBlock.WATERLOGGED, true), Block.UPDATE_CLIENTS)) {
                    stack.shrink(1)
                    return InteractionResultHolder.success(stack)
                }
            }
        }

        // Fallback to placement on farmland or mud
        if (blockBelow.`is`(Blocks.FARMLAND) || blockBelow.`is`(Blocks.MUD)) {
            if (world.setBlock(pos.above(), this.block.defaultBlockState(), Block.UPDATE_CLIENTS)) {
                stack.shrink(1)
                return InteractionResultHolder.success(stack)
            }
        }

        return InteractionResultHolder.fail(stack)
    }
}