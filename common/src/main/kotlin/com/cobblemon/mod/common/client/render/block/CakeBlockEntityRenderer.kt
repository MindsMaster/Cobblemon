/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.block

import com.cobblemon.mod.common.api.cooking.getColor
import com.cobblemon.mod.common.block.entity.CakeBlockEntity
import com.cobblemon.mod.common.block.entity.CakeBlockEntity.Companion.MAX_NUMBER_OF_BITES
import com.cobblemon.mod.common.item.components.CookingComponent
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.VertexConsumer
import com.mojang.math.Axis
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.inventory.InventoryMenu.BLOCK_ATLAS
import org.joml.Vector3d

class CakeBlockEntityRenderer(ctx: BlockEntityRendererProvider.Context) : BlockEntityRenderer<CakeBlockEntity> {

    companion object {
        const val PIXEL_SHIFT = 1.0 / 16.0
        const val WHITE_OPAQUE_COLOR = 0xFFFFFFFF.toInt()

        const val ATLAS_START = 0.0f
        const val ATLAS_END = 1.0f
        const val HEIGHT = 0.5f
        const val TOP_LAYER_HEIGHT = 0.5625f
        const val BOTTOM_LAYER_HEIGHT = 0.0f

        object DefaultCake : RenderableCake {
            override val topLayers: List<LayerTexture> = mutableListOf(
                LayerTexture(loadTexture("block/poke_snack_top_layer2"), LayerColor.SECONDARY),
                LayerTexture(loadTexture("block/poke_snack_top_layer3"), LayerColor.PRIMARY),
            )
            override val sideLayers: List<LayerTexture> = mutableListOf(
                LayerTexture(loadTexture("block/poke_snack_side_layer1"), LayerColor.TERTIARY),
                LayerTexture(loadTexture("block/poke_snack_side_layer2"), LayerColor.SECONDARY),
                LayerTexture(loadTexture("block/poke_snack_side_layer3"), LayerColor.PRIMARY),
            )
            override val bottomLayers: List<LayerTexture> = mutableListOf(
                LayerTexture(loadTexture("block/poke_snack_bottom_layer1"), LayerColor.TERTIARY),
            )
            override val innerLayers: List<LayerTexture> = mutableListOf(
                LayerTexture(loadTexture("block/poke_snack_inner_layer1"), LayerColor.TERTIARY),
                LayerTexture(loadTexture("block/poke_snack_inner_layer2"), LayerColor.SECONDARY),
                LayerTexture(loadTexture("block/poke_snack_inner_layer3"), LayerColor.PRIMARY),
            )
        }

        fun loadTexture(textureName: String): TextureAtlasSprite {
            return Minecraft.getInstance().getTextureAtlas(BLOCK_ATLAS).apply(ResourceLocation("cobblemon", textureName))
        }
    }

    interface RenderableCake {
        val topLayers: List<LayerTexture>
        val sideLayers: List<LayerTexture>
        val bottomLayers: List<LayerTexture>
        val innerLayers: List<LayerTexture>
    }

    class LayerTexture(val texture: TextureAtlasSprite, val color: LayerColor) {
        fun getColor(primaryColor: Int, secondaryColor: Int, tertiaryColor: Int): Int {
            return when (color) {
                LayerColor.PRIMARY -> primaryColor
                LayerColor.SECONDARY -> secondaryColor
                LayerColor.TERTIARY -> tertiaryColor
            }
        }
    }

    enum class LayerColor {
        PRIMARY, SECONDARY, TERTIARY
    }

    override fun render(
        blockEntity: CakeBlockEntity,
        partialTick: Float,
        poseStack: PoseStack,
        bufferSource: MultiBufferSource,
        packedLight: Int,
        packedOverlay: Int
    ) {
        val cookingComponent = blockEntity.cookingComponent
        renderCake(cookingComponent, poseStack, bufferSource, packedLight, blockEntity.bites)
    }

    fun renderCake(
        cookingComponent: CookingComponent?,
        poseStack: PoseStack,
        multiBufferSource: MultiBufferSource,
        light: Int,
        bites: Int,
    ) {
        val renderableCake: RenderableCake = DefaultCake
        val maxBites = MAX_NUMBER_OF_BITES + 1f
        val cakeIsFull = bites.toFloat() == 0f
        val percentageToRender = if (cakeIsFull) 1.0f else PIXEL_SHIFT.toFloat() + (PIXEL_SHIFT.toFloat() * 2f * (maxBites - bites).toFloat())
        val clippedWidth = (ATLAS_END - ATLAS_START) * percentageToRender
        val topLayer = renderableCake.topLayers.first().texture
        val uClipped = topLayer.u0 + (topLayer.u1 - topLayer.u0) * percentageToRender

        val vertexConsumer = multiBufferSource.getBuffer(RenderType.cutout())
        val primaryColor = cookingComponent?.seasoning1?.color?.let { getColor(it) } ?: WHITE_OPAQUE_COLOR
        val secondaryColor = cookingComponent?.seasoning2?.color?.let { getColor(it) } ?: WHITE_OPAQUE_COLOR
        val tertiaryColor = cookingComponent?.seasoning3?.color?.let { getColor(it) } ?: WHITE_OPAQUE_COLOR

        poseStack.pushPose()
        poseStack.translate(0.5, 0.5, 0.5)
        poseStack.mulPose(Axis.YP.rotationDegrees(180f))
        poseStack.translate(-0.5, -0.5, -0.5)

        poseStack.pushPose()
        for (layer in renderableCake.topLayers) {
            val texture = layer.texture
            drawQuad(
                vertexConsumer, poseStack,
                ATLAS_START, TOP_LAYER_HEIGHT, ATLAS_START, clippedWidth, TOP_LAYER_HEIGHT, ATLAS_END,
                texture.u0, texture.v0, uClipped, texture.v1,
                light, layer.getColor(primaryColor, secondaryColor, tertiaryColor)
            )
        }
        poseStack.popPose()

        val translations = arrayOf(
            Vector3d(-1.0, 1.0, 0.5 - PIXEL_SHIFT),
            Vector3d(0.0, 1.0, 0.5 - PIXEL_SHIFT),
            Vector3d(0.0, 1.0, -0.5 - PIXEL_SHIFT),
            Vector3d(-1.0, 1.0, -0.5 - PIXEL_SHIFT),
        )

        val rotationsY = arrayOf(
            Axis.YP.rotationDegrees(90f),
            Axis.YP.rotationDegrees(0f),
            Axis.YP.rotationDegrees(-90f),
            Axis.YP.rotationDegrees(-180f)
        )

        for (i in translations.indices) {
            poseStack.pushPose()
            poseStack.mulPose(rotationsY[i])
            poseStack.translate(translations[i].x, translations[i].y, translations[i].z)
            poseStack.mulPose(Axis.XP.rotationDegrees(90f))

            if (i == 0 && !cakeIsFull) {
                poseStack.translate(0.0, -bites * PIXEL_SHIFT * 2, 0.0)
                for (layer in renderableCake.innerLayers) {
                    val texture = layer.texture
                    drawQuad(
                        vertexConsumer, poseStack,
                        ATLAS_START, HEIGHT, ATLAS_START, ATLAS_END, HEIGHT, ATLAS_END,
                        texture.u0, texture.v0, texture.u1, texture.v1,
                        light, layer.getColor(primaryColor, secondaryColor, tertiaryColor)
                    )
                }
            } else if (i == 0 || i == 1) {
                for (layer in renderableCake.sideLayers) {
                    val texture = layer.texture
                    drawQuad(
                        vertexConsumer, poseStack,
                        ATLAS_START, HEIGHT, ATLAS_START, clippedWidth, HEIGHT, ATLAS_END,
                        texture.u0, texture.v0, uClipped, texture.v1,
                        light, layer.getColor(primaryColor, secondaryColor, tertiaryColor)
                    )
                }
            } else if (i == 2) {
                for (layer in renderableCake.sideLayers) {
                    val texture = layer.texture
                    drawQuad(
                        vertexConsumer, poseStack,
                        ATLAS_START, HEIGHT, ATLAS_START, ATLAS_END, HEIGHT, ATLAS_END,
                        texture.u0, texture.v0, texture.u1, texture.v1,
                        light, layer.getColor(primaryColor, secondaryColor, tertiaryColor)
                    )
                }
            } else if (i == 3) {
                val clippedStartX = ATLAS_START + (ATLAS_END - ATLAS_START) * (1 - percentageToRender)

                for (layer in renderableCake.sideLayers) {
                    val texture = layer.texture
                    val clippedUStart = texture.u0 + (texture.u1 - texture.u0) * (1 - percentageToRender)
                    drawQuad(
                        vertexConsumer, poseStack,
                        clippedStartX, HEIGHT, ATLAS_START, ATLAS_END, HEIGHT, ATLAS_END,
                        clippedUStart, texture.v0, texture.u1, texture.v1,
                        light, layer.getColor(primaryColor, secondaryColor, tertiaryColor)
                    )
                }
            }

            poseStack.popPose()
        }

        poseStack.pushPose()
        poseStack.translate(0.5, 0.0, 0.5)
        poseStack.mulPose(Axis.XP.rotationDegrees(180f))
        poseStack.translate(-0.5, 0.0, -0.5)

        for (layer in renderableCake.bottomLayers) {
            val texture = layer.texture
            drawQuad(
                vertexConsumer, poseStack,
                ATLAS_START, BOTTOM_LAYER_HEIGHT, ATLAS_START, clippedWidth, BOTTOM_LAYER_HEIGHT, ATLAS_END,
                texture.u0, texture.v0, uClipped, texture.v1,
                light, layer.getColor(primaryColor, secondaryColor, tertiaryColor)
            )
        }

        poseStack.popPose()

        poseStack.popPose()
    }

    private fun drawQuad(
        builder: VertexConsumer,
        poseStack: PoseStack,
        x0: Float, y0: Float, z0: Float,
        x1: Float, y1: Float, z1: Float,
        u0: Float, v0: Float, u1: Float, v1: Float,
        packedLight: Int, color: Int,
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