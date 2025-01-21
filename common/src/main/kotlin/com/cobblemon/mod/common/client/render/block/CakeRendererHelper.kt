/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.block

import com.cobblemon.mod.common.api.cooking.getColor
import com.cobblemon.mod.common.block.entity.LureCakeBlockEntity
import com.cobblemon.mod.common.item.components.CookingComponent
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.VertexConsumer
import com.mojang.math.Axis
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.inventory.InventoryMenu.BLOCK_ATLAS
import net.minecraft.world.level.block.state.BlockState
import org.joml.Vector3d

const val PIXEL_SHIFT = 1.0 / 16.0
const val WHITE_OPAQUE_COLOR = 0xFFFFFFFF.toInt()

const val ATLAS_START = 0.0f
const val ATLAS_END = 1.0f
const val HEIGHT = 0.5f
const val TOP_LAYER_HEIGHT = 0.5625f
const val BOTTOM_LAYER_HEIGHT = 0.0f

val TOP_LAYER: TextureAtlasSprite = loadTexture("block/poke_snack_top_layer2")
val CHERRY_LAYER: TextureAtlasSprite = loadTexture("block/poke_snack_top_layer3")
val SIDE_LAYER_BOTTOM: TextureAtlasSprite = loadTexture("block/poke_snack_side_layer1")
val SIDE_LAYER_TOP: TextureAtlasSprite = loadTexture("block/poke_snack_side_layer2")
val SIDE_LAYER_MIDDLE: TextureAtlasSprite = loadTexture("block/poke_snack_side_layer3")
val BOTTOM_LAYER: TextureAtlasSprite = loadTexture("block/poke_snack_bottom_layer1")
val FIRST_INNER_LAYER: TextureAtlasSprite = loadTexture("block/poke_snack_inner_layer1")
val SECOND_INNER_LAYER: TextureAtlasSprite = loadTexture("block/poke_snack_inner_layer2")
val THIRD_INNER_LAYER: TextureAtlasSprite = loadTexture("block/poke_snack_inner_layer3")

fun loadTexture(textureName: String): TextureAtlasSprite {
    return Minecraft.getInstance().getTextureAtlas(BLOCK_ATLAS).apply(ResourceLocation("cobblemon", textureName))
}

fun renderCake(
    cookingComponent: CookingComponent?,
    poseStack: PoseStack,
    multiBufferSource: MultiBufferSource,
    light: Int,
    bites: Int,
) {
    val maxBites = 7f
    val cakeIsFull = bites.toFloat() == 0f
    val percentageToRender = if (cakeIsFull) 1.0f else PIXEL_SHIFT.toFloat() + (PIXEL_SHIFT.toFloat() * 2f * (maxBites - bites).toFloat())
    val clippedWidth = (ATLAS_END - ATLAS_START) * percentageToRender
    val uClipped = TOP_LAYER.u0 + (TOP_LAYER.u1 - TOP_LAYER.u0) * percentageToRender

    val vertexConsumer = multiBufferSource.getBuffer(RenderType.cutout())
    val primaryColor = cookingComponent?.seasoning1?.color?.let { getColor(it) } ?: WHITE_OPAQUE_COLOR
    val secondaryColor = cookingComponent?.seasoning2?.color?.let { getColor(it) } ?: WHITE_OPAQUE_COLOR
    val tertiaryColor = cookingComponent?.seasoning3?.color?.let { getColor(it) } ?: WHITE_OPAQUE_COLOR

    poseStack.pushPose()
    poseStack.translate(0.5, 0.5, 0.5)
    poseStack.mulPose(Axis.YP.rotationDegrees(180f))
    poseStack.translate(-0.5, -0.5, -0.5)

    poseStack.pushPose()
    drawQuad(
        vertexConsumer, poseStack,
        ATLAS_START, TOP_LAYER_HEIGHT, ATLAS_START, clippedWidth, TOP_LAYER_HEIGHT, ATLAS_END,
        TOP_LAYER.u0, TOP_LAYER.v0, uClipped, TOP_LAYER.v1,
        light, secondaryColor
    )

    drawQuad(
        vertexConsumer, poseStack,
        ATLAS_START, TOP_LAYER_HEIGHT, ATLAS_START, clippedWidth, TOP_LAYER_HEIGHT, ATLAS_END,
        CHERRY_LAYER.u0, CHERRY_LAYER.v0, uClipped, CHERRY_LAYER.v1,
        light, primaryColor
    )
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
            drawQuad(
                vertexConsumer, poseStack,
                ATLAS_START, HEIGHT, ATLAS_START, ATLAS_END, HEIGHT, ATLAS_END,
                FIRST_INNER_LAYER.u0, FIRST_INNER_LAYER.v0, FIRST_INNER_LAYER.u1, FIRST_INNER_LAYER.v1,
                light, tertiaryColor
            )
            drawQuad(
                vertexConsumer, poseStack,
                ATLAS_START, HEIGHT, ATLAS_START, ATLAS_END, HEIGHT, ATLAS_END,
                SECOND_INNER_LAYER.u0, SECOND_INNER_LAYER.v0, SECOND_INNER_LAYER.u1, SECOND_INNER_LAYER.v1,
                light, secondaryColor
            )
            drawQuad(
                vertexConsumer, poseStack,
                ATLAS_START, HEIGHT, ATLAS_START, ATLAS_END, HEIGHT, ATLAS_END,
                THIRD_INNER_LAYER.u0, THIRD_INNER_LAYER.v0, THIRD_INNER_LAYER.u1, THIRD_INNER_LAYER.v1,
                light, primaryColor
            )
        } else if (i == 0 || i == 1) {
            drawQuad(
                vertexConsumer, poseStack,
                ATLAS_START, HEIGHT, ATLAS_START, clippedWidth, HEIGHT, ATLAS_END,
                SIDE_LAYER_BOTTOM.u0, SIDE_LAYER_BOTTOM.v0, uClipped, SIDE_LAYER_BOTTOM.v1,
                light, tertiaryColor
            )
            drawQuad(
                vertexConsumer, poseStack,
                ATLAS_START, HEIGHT, ATLAS_START, clippedWidth, HEIGHT, ATLAS_END,
                SIDE_LAYER_TOP.u0, SIDE_LAYER_TOP.v0, uClipped, SIDE_LAYER_TOP.v1,
                light, secondaryColor
            )
            drawQuad(
                vertexConsumer, poseStack,
                ATLAS_START, HEIGHT, ATLAS_START, clippedWidth, HEIGHT, ATLAS_END,
                SIDE_LAYER_MIDDLE.u0, SIDE_LAYER_MIDDLE.v0, uClipped, SIDE_LAYER_MIDDLE.v1,
                light, primaryColor
            )
        } else if (i == 2) {
            drawQuad(
                vertexConsumer, poseStack,
                ATLAS_START, HEIGHT, ATLAS_START, ATLAS_END, HEIGHT, ATLAS_END,
                SIDE_LAYER_BOTTOM.u0, SIDE_LAYER_BOTTOM.v0, SIDE_LAYER_BOTTOM.u1, SIDE_LAYER_BOTTOM.v1,
                light, tertiaryColor
            )
            drawQuad(
                vertexConsumer, poseStack,
                ATLAS_START, HEIGHT, ATLAS_START, ATLAS_END, HEIGHT, ATLAS_END,
                SIDE_LAYER_TOP.u0, SIDE_LAYER_TOP.v0, SIDE_LAYER_TOP.u1, SIDE_LAYER_TOP.v1,
                light, secondaryColor
            )
            drawQuad(
                vertexConsumer, poseStack,
                ATLAS_START, HEIGHT, ATLAS_START, ATLAS_END, HEIGHT, ATLAS_END,
                SIDE_LAYER_TOP.u0, SIDE_LAYER_MIDDLE.v0, SIDE_LAYER_MIDDLE.u1, SIDE_LAYER_MIDDLE.v1,
                light, primaryColor
            )
        } else if (i == 3) {
            val clippedStartX = ATLAS_START + (ATLAS_END - ATLAS_START) * (1 - percentageToRender)
            val clippedUStart = SIDE_LAYER_BOTTOM.u0 + (SIDE_LAYER_BOTTOM.u1 - SIDE_LAYER_BOTTOM.u0) * (1 - percentageToRender)

            drawQuad(
                vertexConsumer, poseStack,
                clippedStartX, HEIGHT, ATLAS_START, ATLAS_END, HEIGHT, ATLAS_END,
                clippedUStart, SIDE_LAYER_BOTTOM.v0, SIDE_LAYER_BOTTOM.u1, SIDE_LAYER_BOTTOM.v1,
                light, tertiaryColor
            )
            drawQuad(
                vertexConsumer, poseStack,
                clippedStartX, HEIGHT, ATLAS_START, ATLAS_END, HEIGHT, ATLAS_END,
                clippedUStart, SIDE_LAYER_TOP.v0, SIDE_LAYER_TOP.u1, SIDE_LAYER_TOP.v1,
                light, secondaryColor
            )
            drawQuad(
                vertexConsumer, poseStack,
                clippedStartX, HEIGHT, ATLAS_START, ATLAS_END, HEIGHT, ATLAS_END,
                clippedUStart, SIDE_LAYER_MIDDLE.v0, SIDE_LAYER_MIDDLE.u1, SIDE_LAYER_MIDDLE.v1,
                light, primaryColor
            )
        }

        poseStack.popPose()
    }

    poseStack.pushPose()
    poseStack.translate(0.5, 0.0, 0.5)
    poseStack.mulPose(Axis.XP.rotationDegrees(180f))
    poseStack.translate(-0.5, 0.0, -0.5)
    drawQuad(
        vertexConsumer, poseStack,
        ATLAS_START, BOTTOM_LAYER_HEIGHT, ATLAS_START, clippedWidth, BOTTOM_LAYER_HEIGHT, ATLAS_END,
        BOTTOM_LAYER.u0, BOTTOM_LAYER.v0, uClipped, BOTTOM_LAYER.v1,
        light, tertiaryColor
    )
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