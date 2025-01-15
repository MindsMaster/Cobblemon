/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.block

import com.cobblemon.mod.common.api.cooking.getTransparentColorMixFromSeasonings
import com.cobblemon.mod.common.block.entity.CampfireBlockEntity
import com.cobblemon.mod.common.block.entity.CampfireBlockEntity.Companion.IS_LID_OPEN_INDEX
import com.cobblemon.mod.common.client.CobblemonBakingOverrides
import com.cobblemon.mod.common.client.pot.PotTypes
import com.cobblemon.mod.common.net.messages.client.effect.SpawnSnowstormParticlePacket
import com.mojang.blaze3d.vertex.DefaultVertexFormat
import com.cobblemon.mod.common.item.PotItem
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.VertexConsumer
import com.mojang.blaze3d.vertex.VertexFormat
import com.mojang.math.Axis
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.RenderStateShard
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.core.Direction
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.inventory.InventoryMenu
import net.minecraft.world.item.ItemDisplayContext
import net.minecraft.world.level.block.HorizontalDirectionalBlock.FACING
import org.joml.Vector3f
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

class CampfireBlockEntityRenderer(ctx: BlockEntityRendererProvider.Context) : BlockEntityRenderer<CampfireBlockEntity> {

    override fun render(
        blockEntity: CampfireBlockEntity,
        tickDelta: Float,
        poseStack: PoseStack,
        multiBufferSource: MultiBufferSource,
        light: Int,
        overlay: Int
    ) {
        val potItem = blockEntity.getPotItem()?.item as PotItem?
        if (potItem == null) return

        poseStack.pushPose()

        val facing = blockEntity.blockState.getValue(FACING)
        val rotationAngle = when (facing) {
            Direction.NORTH -> 180f
            Direction.SOUTH -> 0f
            Direction.WEST -> 90f
            Direction.EAST -> -90f
            else -> 0f
        }

        poseStack.translate(0.5, 0.5, 0.5)
        poseStack.mulPose(Axis.YP.rotationDegrees(rotationAngle))
        poseStack.translate(-0.5, -0.5, -0.5)

        renderPot(potItem, blockEntity, tickDelta, poseStack, multiBufferSource, light, overlay)
        renderWater(blockEntity, tickDelta, poseStack, multiBufferSource, light, overlay)

        val isLidOpen = blockEntity.dataAccess.get(IS_LID_OPEN_INDEX) == 1
        if (isLidOpen) {
            renderSeasonings(blockEntity, tickDelta, poseStack, multiBufferSource, light, overlay)
        }

        poseStack.popPose()
    }

    private fun renderPot(
        potItem: PotItem,
        blockEntity: CampfireBlockEntity,
        tickDelta: Float,
        poseStack: PoseStack,
        multiBufferSource: MultiBufferSource,
        light: Int,
        overlay: Int
    ) {
        poseStack.pushPose()

        val isLidOpen = blockEntity.dataAccess.get(IS_LID_OPEN_INDEX) == 1
        val model = CobblemonBakingOverrides.getCampfirePotOverride(potItem.type, isLidOpen).getModel()
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

        // new custom Render Type to let us see the damn bubbles >:(
        val renderType = RenderType.create(
                "custom_translucent_water",
                DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP,
                VertexFormat.Mode.QUADS,
                256,
                false,
                true,
                RenderType.CompositeState.builder()
                        .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                        .setShaderState(RenderStateShard.RENDERTYPE_ENTITY_TRANSLUCENT_SHADER) // Use a valid shader
                        .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                        .setDepthTestState(RenderStateShard.NO_DEPTH_TEST)
                        .createCompositeState(false)
        )

        val vertexConsumer = multiBufferSource.getBuffer(renderType)

        poseStack.translate(0.0, 0.5, 0.0) // todo set y back to .4 (maybe make the quad smaller overall so it fits just inside the pot opening)
        poseStack.scale(1.0f, 1.0f, 1.0f)

        val stillTexture = ResourceLocation("minecraft", "block/water_still")
        val sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(stillTexture)
        val height = 0.5f
        val mixColor = getTransparentColorMixFromSeasonings(blockEntity.getSeasonings())
        val waterColor = if (mixColor != -1) mixColor else 0x803F76E4.toInt() //0xFF3F76E4 for fully opaque water
        drawQuad(vertexConsumer, poseStack, 0.125f, height, 0.125f, 0.875f, height, 0.875f, sprite.u0, sprite.v0, sprite.u1, sprite.v1, light, waterColor)

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
        val seasonings = blockEntity.getSeasonings()

        val gameTime = Minecraft.getInstance().level?.gameTime ?: 0

        val rotationSpeed = 1f
        val rotationAngle = (gameTime * rotationSpeed) % 360
        val jumpAmplitude = 0.1f
        val jumpSpeed = 0.1f

        seasonings.forEachIndexed { index, seasoning ->
            poseStack.pushPose()

            poseStack.scale(0.5f, 0.5f, 0.5f)

            val angleOffset = index * (360f / seasonings.size)
            val angleInRadians = Math.toRadians((rotationAngle + angleOffset).toDouble())

            val xOffset = cos(angleInRadians.toDouble()).toFloat() * 0.5f
            val zOffset = sin(angleInRadians.toDouble()).toFloat() * 0.5f
            val jumpOffset = sin(gameTime * jumpSpeed + index * 2) * jumpAmplitude
            poseStack.translate(1f + xOffset, 1.7f + jumpOffset, 1f + zOffset)

            val lookAtDirection = Vector3f(1f + xOffset - 1f, 1.8f - 1.8f, 1f + zOffset - 1f)
            poseStack.mulPose(Axis.YP.rotationDegrees((-Math.toDegrees(
                atan2(
                    lookAtDirection.z().toDouble(),
                    lookAtDirection.x().toDouble()
                )
            )).toFloat() + 90))

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

            poseStack.popPose()
        }
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