/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.block

import com.cobblemon.mod.common.block.entity.CampfireBlockEntity
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider
import net.minecraft.world.item.ItemDisplayContext

class CampfireBlockEntityRenderer(ctx: BlockEntityRendererProvider.Context) : BlockEntityRenderer<CampfireBlockEntity> {

    override fun render(blockEntity: CampfireBlockEntity, tickDelta: Float, poseStack: PoseStack, multiBufferSource: MultiBufferSource, light: Int, overlay: Int ) {
        poseStack.pushPose()
        poseStack.translate(0.5, 0.65, 0.5)
        Minecraft.getInstance().itemRenderer.renderStatic(blockEntity.getItemStack(), ItemDisplayContext.GROUND, light, overlay, poseStack, multiBufferSource, blockEntity.level, 0)
        poseStack.popPose()
    }
}