/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.item

import com.cobblemon.mod.common.api.tags.CobblemonItemTags
import com.cobblemon.mod.common.api.tags.CobblemonItemTags.WEARABLE_FACE_ITEMS
import com.cobblemon.mod.common.api.tags.CobblemonItemTags.WEARABLE_HAT_ITEMS
import com.cobblemon.mod.common.client.entity.NPCClientDelegate
import com.cobblemon.mod.common.client.entity.PokemonClientDelegate
import com.cobblemon.mod.common.client.render.MatrixWrapper
import com.cobblemon.mod.common.client.render.models.blockbench.FloatingState
import com.cobblemon.mod.common.client.render.models.blockbench.PosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.PosableState
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Axis
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.LightTexture
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.entity.ItemRenderer
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemDisplayContext
import net.minecraft.world.item.ItemStack
import org.joml.Vector3f

class HeldItemRenderer {
    private val itemRenderer: ItemRenderer = Minecraft.getInstance().itemRenderer
    private var displayContext: ItemDisplayContext = ItemDisplayContext.FIXED

    companion object {
        const val ITEM_FACE = "item_face"
        const val ITEM_HAT = "item_hat"
        const val ITEM = "item"

        fun getWearableModel3d(id: String): ResourceLocation? {
            val itemName = id.substringAfterLast(":")
            WearableItemModels.entries.toList().forEach { if (it.name.lowercase() == itemName ) return it.getItemModelPath() }
            return null
        }
        fun getWearableModel2d(id: String): ResourceLocation? {
            val itemName = id.substringAfterLast(":")
            WearableItemModels.entries.toList().forEach { if (it.name.lowercase() == itemName ) return it.getItemSpritePath() }
            return null
        }
    }

    fun renderOnModel(
        item: ItemStack,
        model: PosableModel,
        state: PosableState,
        poseStack: PoseStack,
        buffer: MultiBufferSource,
        light: Int = LightTexture.pack(11, 7),
        frontLight: Boolean = false,
        entity: LivingEntity? = null
    ) {
        if (!item.`is`(CobblemonItemTags.HIDDEN_ITEMS)) renderAtLocator(item, model, state, entity, poseStack, buffer, light, 0, frontLight)
        state.animationItems.forEach { (targetLocator, item) -> renderAtLocator(item, model, state, entity, poseStack, buffer, light, 0, frontLight, targetLocator) }
    }

    private fun renderAtLocator(
        item: ItemStack,
        model: PosableModel,
        state: PosableState,
        entity: LivingEntity?,
        poseStack: PoseStack,
        buffer: MultiBufferSource,
        light: Int,
        seed: Int,
        frontLight: Boolean = false,
        targetLocator: String =
            if (item.`is`(WEARABLE_FACE_ITEMS) && state.locatorStates.containsKey(ITEM_FACE)) ITEM_FACE
            else if (item.`is`(WEARABLE_HAT_ITEMS) && state.locatorStates.containsKey(ITEM_HAT)) ITEM_HAT
            else ITEM
    ) {
        if (item.isEmpty) return

        if (state.locatorStates.containsKey(targetLocator)) {
            poseStack.pushPose()
            RenderSystem.applyModelViewMatrix()

            displayContext = model.getLocatorDisplayContext(targetLocator)?:
                if ((item.`is`(WEARABLE_FACE_ITEMS) && targetLocator==ITEM_FACE) || (item.`is`(WEARABLE_HAT_ITEMS)&& targetLocator== ITEM_HAT)) ItemDisplayContext.HEAD
                else ItemDisplayContext.FIXED

            poseStack.mulPose(state.locatorStates[targetLocator]!!.matrix)
            when (displayContext) {
                ItemDisplayContext.FIXED -> {
                    poseStack.mulPose(Axis.XP.rotationDegrees(90.0f))
                    poseStack.scale(.5f, .5f, .5f )
                    poseStack.translate(0f, 0.01666f, 0f)
                }
                ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, ItemDisplayContext.THIRD_PERSON_LEFT_HAND -> {
                    if (state is NPCClientDelegate) {
                        poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F))
                    }
                    else if (state is PokemonClientDelegate || state is FloatingState) {
                        poseStack.mulPose(Axis.YP.rotationDegrees(-90.0F))
                        poseStack.mulPose(Axis.ZP.rotationDegrees(90.0F))
                    }
                    poseStack.translate(0.025f * if (displayContext==ItemDisplayContext.THIRD_PERSON_LEFT_HAND) 1 else -1 , 0.0f, 0.0f)
                }
                ItemDisplayContext.HEAD -> {
                    if (state is NPCClientDelegate) poseStack.mulPose(Axis.XP.rotationDegrees(90.0f))
                    if (item.`is`(WEARABLE_FACE_ITEMS)) {
                        poseStack.translate(0f, 0f, 0.28f)
                        poseStack.scale(0.7f, 0.7f, 0.7f)
                    }
                    else if (item.`is`(WEARABLE_HAT_ITEMS)) {
                        poseStack.translate(0f, -0.26f, 0f)
                        poseStack.scale(.68f, .68f, .68f)
                    }
                }
                else -> {}
            }

            if (frontLight) {
                val light1 = Vector3f(0F,0F,1F)
                val light2 = Vector3f(0F,0F,1F)
                RenderSystem.setShaderLights(light1, light2)
            }

            itemRenderer.renderStatic(entity, item, displayContext, (displayContext==ItemDisplayContext.THIRD_PERSON_LEFT_HAND), poseStack, buffer, null, light, OverlayTexture.NO_OVERLAY, seed)

            poseStack.popPose()
        }
    }
}