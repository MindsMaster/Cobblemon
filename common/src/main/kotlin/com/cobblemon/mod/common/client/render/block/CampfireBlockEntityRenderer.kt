/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.block

import com.cobblemon.mod.common.block.entity.CampfireBlockEntity
import com.cobblemon.mod.common.block.entity.CampfireBlockEntity.Companion.IS_LID_OPEN_INDEX
import com.cobblemon.mod.common.client.CobblemonBakingOverrides
import com.cobblemon.mod.common.client.pot.PotTypes
import com.cobblemon.mod.common.client.render.models.blockbench.pose.Pose
import com.mojang.blaze3d.vertex.DefaultVertexFormat
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.Tesselator
import com.mojang.blaze3d.vertex.VertexConsumer
import com.mojang.blaze3d.vertex.VertexFormat
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.inventory.InventoryMenu
import net.minecraft.world.item.ItemDisplayContext

class CampfireBlockEntityRenderer(ctx: BlockEntityRendererProvider.Context) : BlockEntityRenderer<CampfireBlockEntity> {

    override fun render(
        blockEntity: CampfireBlockEntity,
        tickDelta: Float,
        poseStack: PoseStack,
        multiBufferSource: MultiBufferSource,
        light: Int,
        overlay: Int
    ) {
        val potItem = blockEntity.getPotItem()
        if (potItem?.isEmpty == true) return

        renderPot(blockEntity, tickDelta, poseStack, multiBufferSource, light, overlay)
        renderWater(blockEntity, tickDelta, poseStack, multiBufferSource, light, overlay)
        renderSeasonings(blockEntity, tickDelta, poseStack, multiBufferSource, light, overlay)
    }

    private fun renderPot(
        blockEntity: CampfireBlockEntity,
        tickDelta: Float,
        poseStack: PoseStack,
        multiBufferSource: MultiBufferSource,
        light: Int,
        overlay: Int
    ) {
        poseStack.pushPose()

        val isLidOpen = blockEntity.dataAccess.get(IS_LID_OPEN_INDEX) == 1
        val model = CobblemonBakingOverrides.getCampfirePotOverride(PotTypes.RED, isLidOpen).getModel()
        val buffer = multiBufferSource.getBuffer(RenderType.cutout())

        Minecraft.getInstance().blockRenderer.modelRenderer.renderModel(
            poseStack.last(),
            buffer,
            blockEntity.blockState,
            model,
            1.0f, 1.0f, 1.0f,
            light,
            overlay
        )

        poseStack.popPose()
    }

    private fun renderWater(
        blockEntity: CampfireBlockEntity,
        tickDelta: Float,
        poseStack: PoseStack,
        multiBufferSource: MultiBufferSource,
        light: Int,
        overlay: Int
    ) {
        poseStack.pushPose()

        val vertexConsumer = multiBufferSource.getBuffer(RenderType.translucent())

        poseStack.translate(0.0, 0.4, 0.0)
        poseStack.scale(1.0f, 1.0f, 1.0f)

        val stillTexture = ResourceLocation("minecraft", "block/water_still")
        val sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(stillTexture)
        val height = 0.5f
        drawQuad(vertexConsumer, poseStack, 0.125f, height, 0.125f, 0.875f, height, 0.875f, sprite.u0, sprite.v0, sprite.u1, sprite.v1, light, 0xFF3F76E4.toInt());

        poseStack.popPose()
    }

    private fun renderSeasonings(
        blockEntity: CampfireBlockEntity,
        tickDelta: Float,
        poseStack: PoseStack,
        multiBufferSource: MultiBufferSource,
        light: Int,
        overlay: Int
    ) {
        poseStack.pushPose()

        val seasonings = blockEntity.getSeasonings()
        seasonings.forEach { seasoning ->
            Minecraft.getInstance().itemRenderer.renderStatic(
                seasoning,
                ItemDisplayContext.GROUND,
                light,
                OverlayTexture.NO_OVERLAY,
                poseStack,
                multiBufferSource,
                blockEntity.level,
                0
            )
        }

        poseStack.popPose()
    }

    private fun drawQuad(
        builder: VertexConsumer,
        poseStack: PoseStack,
        x0: Float, y0: Float, z0: Float,
        x1: Float, y1: Float, z1: Float,
        u0: Float, v0: Float, u1: Float, v1: Float,
        packedLight: Int, color: Int
    ) {
        drawVertex(builder, poseStack, x0, y0, z0, u0, v0, packedLight, color)
        drawVertex(builder, poseStack, x0, y1, z1, u0, v1, packedLight, color)
        drawVertex(builder, poseStack, x1, y1, z1, u1, v1, packedLight, color)
        drawVertex(builder, poseStack, x1, y0, z0, u1, v0, packedLight, color)
    }

    private fun drawVertex(
        builder: VertexConsumer,
        poseStack: PoseStack,
        x: Float, y: Float, z: Float,
        u: Float, v: Float,
        packedLight: Int, color: Int
    ) {
        builder.addVertex(poseStack.last().pose(), x, y, z)
            .setColor(color)
            .setUv(u, v)
            .setLight(packedLight)
            .setNormal(0f, 1f, 0f)
    }
}