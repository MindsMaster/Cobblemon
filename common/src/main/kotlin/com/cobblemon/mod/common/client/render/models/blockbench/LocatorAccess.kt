/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench

import com.cobblemon.mod.common.client.render.MatrixWrapper
import com.cobblemon.mod.common.client.render.models.blockbench.pose.Bone
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Axis
import net.minecraft.world.entity.Entity

/**
 * A storage of locator information for a model, attached to its [joint].
 * This is like a sister to ModelPart itself in its nesting structure, but
 * used specifically for Locators.
 *
 * @author Hiroku
 * @since February 10th, 2023
 */
class LocatorAccess(
    val joint: Bone,
    val locators: Map<String, Bone> = mapOf(),
    val children: List<LocatorAccess> = listOf()
) {
    companion object {
        const val PREFIX = "internal_locator__"

        fun resolve(part: Bone): LocatorAccess? {
            val (
                locatorChildren,
                nonLocatorChildren
            ) = part.children.entries.partition { it.key.startsWith(PREFIX) }

            val locators = locatorChildren.associate { (namePrefixed, part) ->
                namePrefixed.substringAfter(PREFIX) to part
            }

            val children = mutableListOf<LocatorAccess>()

            if (nonLocatorChildren.isEmpty()) {
                return if (locators.isEmpty()) {
                    null
                } else {
                    LocatorAccess(
                        joint = part,
                        locators = locators
                    )
                }
            } else {
                children.addAll(nonLocatorChildren.mapNotNull { (_, part) -> resolve(part) })
                return if (children.isEmpty() && locators.isEmpty()) {
                    null
                } else {
                    LocatorAccess(
                        joint = part,
                        locators = locators,
                        children = children
                    )
                }
            }
        }
    }

    /**
     * Updates all of the locator states with the position at this current frame.
     * This is the same logic as ModelPart uses, that's why we reuse ModelPart#rotate.
     */
    fun update(matrixStack: PoseStack, entity: Entity?, scale: Float, state: MutableMap<String, MatrixWrapper>, isRoot: Boolean = false) {
        matrixStack.pushPose()
        joint.transform(matrixStack)

        if (isRoot) {
            matrixStack.pushPose()
            matrixStack.mulPose(Axis.ZP.rotationDegrees(180f)) //Undo rotation from previous flip
            state.getOrPut("root") { MatrixWrapper() }.updateMatrix(matrixStack.last().pose())
            matrixStack.popPose()

            if (entity != null) {
                // Put in an approximation of the target locator. If the model has one defined,
                // this will be overridden.
                matrixStack.pushPose()
                matrixStack.mulPose(Axis.ZP.rotationDegrees(180f)) //Undo rotation from previous flip
                matrixStack.translate(0.0, entity.boundingBox.ysize / 2.0 / scale, -entity.bbWidth * 0.6 / scale)
                state.getOrPut("target") { MatrixWrapper() }.updateMatrix(matrixStack.last().pose())
                state.getOrPut("special_attack") { MatrixWrapper() }.updateMatrix(matrixStack.last().pose())
                matrixStack.popPose()

                // If we have the entity, put in a "middle" locator for center of mass
                // this will be overridden.
                matrixStack.pushPose()
                matrixStack.mulPose(Axis.ZP.rotationDegrees(180f)) //Undo rotation from previous flip
                matrixStack.translate(0.0, entity.boundingBox.ysize / 2.0 / scale, 0.0)
                state.getOrPut("middle") { MatrixWrapper() }.updateMatrix(matrixStack.last().pose())
                matrixStack.popPose()

                // If we have the entity, put in a "top" locator for top center of hitbox.
                matrixStack.pushPose()
                matrixStack.mulPose(Axis.ZP.rotationDegrees(180f)) //Undo rotation from previous flip
                matrixStack.translate(0.0, entity.boundingBox.ysize, 0.0)
                state.getOrPut("top") { MatrixWrapper() }.updateMatrix(matrixStack.last().pose())
                matrixStack.popPose()
            }
        }

        for ((name, locator) in locators) {
            matrixStack.pushPose()
            locator.transform(matrixStack)
            matrixStack.mulPose(Axis.ZP.rotationDegrees(180f)) //Undo rotation from previous flip
            state.getOrPut(name) { MatrixWrapper() }.updateMatrix(matrixStack.last().pose())
            matrixStack.popPose()
        }

        children.forEach {
            it.update(matrixStack, entity, scale, state, isRoot = false)
        }

        matrixStack.popPose()
    }
}