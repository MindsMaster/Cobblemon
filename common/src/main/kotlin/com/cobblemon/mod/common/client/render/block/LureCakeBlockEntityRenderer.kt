/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.block

import com.cobblemon.mod.common.block.entity.LureCakeBlockEntity
import com.cobblemon.mod.common.util.cobblemonResource
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider
import net.minecraft.client.renderer.block.model.ItemTransforms
import net.minecraft.world.item.BlockItem
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockState

class LureCakeBlockEntityRenderer(ctx: BlockEntityRendererProvider.Context) : BlockEntityRenderer<LureCakeBlockEntity> {

    override fun render(
            blockEntity: LureCakeBlockEntity,
            tickDelta: Float,
            poseStack: PoseStack,
            multiBufferSource: MultiBufferSource,
            light: Int,
            overlay: Int
    ) {
        // Ensure the renderer runs only on the client
        val isClientSide = blockEntity.level?.isClientSide ?: false
        if (!isClientSide) return

        // Retrieve the block state of the Lure Cake
        val blockState: BlockState = blockEntity.blockState
        if (blockState.isAir) {
            // Log an error or warning to ensure this isn't the problem
            return
        }

        /*val baseTexture = cobblemonResource("textures/lure_cake/lure_cake_base.png")
        val crustTexture = cobblemonResource("textures/lure_cake/lure_cake_crust_overlay.png")
        val fillingTexture = cobblemonResource("textures/lure_cake/lure_cake_filling_overlay.png")
        val frostingTexture = cobblemonResource("textures/lure_cake/lure_cake_frosting_overlay.png")*/


        // Push the current transformation matrix
        poseStack.pushPose()
        //println("Rendering block state: $blockState")


        // Apply necessary transformations (if needed)
        // For example, positioning or scaling adjustments

        // Render the block using the default rendering pipeline
        Minecraft.getInstance().blockRenderer.renderSingleBlock(
                blockState,
                poseStack,
                multiBufferSource,
                light,
                overlay
        )

        // Pop the transformation matrix to restore the original state
        poseStack.popPose()
    }
}