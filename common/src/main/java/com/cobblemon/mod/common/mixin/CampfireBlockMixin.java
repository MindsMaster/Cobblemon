/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.mixin;

import com.cobblemon.mod.common.CobblemonBlocks;
import com.cobblemon.mod.common.block.entity.CampfireBlockEntity;
import com.cobblemon.mod.common.item.PotItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CampfireBlock.class)
public abstract class CampfireBlockMixin {

    @Inject(method = "useItemOn", at = @At(value = "HEAD"), cancellable = true)
    private void cobblemon$useItemOn(ItemStack itemStack, BlockState blockState, Level world, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult, CallbackInfoReturnable<ItemInteractionResult> cir) {
        if (!world.isClientSide && itemStack.getItem() instanceof PotItem) {
            BlockEntity blockEntity = world.getBlockEntity(blockPos);

            // Check if the block entity is an instance of the vanilla CampfireBlockEntity and skip if so
            if (blockEntity instanceof net.minecraft.world.level.block.entity.CampfireBlockEntity
                    && blockState.getBlock() instanceof LecternBlock) return;

            // Remove the existing block entity and replace with the custom CampfireBlockEntity
            Direction facing = blockState.getValue(HorizontalDirectionalBlock.FACING);
            itemStack.consumeAndReturn(1, player);
            if (blockEntity != null) {
                blockEntity.setRemoved();
            }
            BlockState newBlockState = CobblemonBlocks.CAMPFIRE.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, facing);
            world.setBlockAndUpdate(blockPos, newBlockState);

            // Retrieve the new block entity and set the PotItem
            BlockEntity newBlockEntity = world.getBlockEntity(blockPos);
            if (newBlockEntity instanceof CampfireBlockEntity) {
                CampfireBlockEntity campfireEntity = (CampfireBlockEntity) newBlockEntity;

                if (campfireEntity.getPotItem() == null || campfireEntity.getPotItem().isEmpty()) {
                    campfireEntity.setPotItem(itemStack.split(1));
                    world.playSound(null, blockPos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.7F, 1.0F);
                    cir.setReturnValue(ItemInteractionResult.SUCCESS);
                    return;
                }
            }

            // If unable to place the item, fail the interaction
            cir.setReturnValue(ItemInteractionResult.FAIL);
        }
    }
}



