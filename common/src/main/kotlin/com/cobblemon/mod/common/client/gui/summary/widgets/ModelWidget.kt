/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui.summary.widgets

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.client.gui.drawProfilePokemon
import com.cobblemon.mod.common.client.gui.calculateHeadYawAndPitch
import com.cobblemon.mod.common.client.render.models.blockbench.FloatingState
import com.cobblemon.mod.common.client.render.item.HeldItemRenderer
import com.cobblemon.mod.common.pokemon.RenderablePokemon
import com.cobblemon.mod.common.util.math.fromEulerXYZDegrees
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack
import org.joml.Quaternionf
import org.joml.Vector3f

class ModelWidget(
    pX: Int, pY: Int,
    pWidth: Int, pHeight: Int,
    pokemon: RenderablePokemon,
    val baseScale: Float = 2.7F,
    var rotationY: Float = 35F,
    var offsetY: Double = 0.0,
    val playCryOnClick: Boolean = false,
    val shouldFollowCursor: Boolean = false,
    var heldItem: ItemStack? = null
): SoundlessWidget(pX, pY, pWidth, pHeight, Component.literal("Summary - ModelWidget")) {

    companion object {
        var render = true
        const val MAX_SECONDS_LOOKING = 6L
    }

    var pokemon: RenderablePokemon = pokemon
        set (value) {
            field = value
            currentYawAndPitch = Pair(0f, 0f)
            state = FloatingState()
        }

    private val heldItemRenderer = HeldItemRenderer()

    var state = FloatingState()
    var lookStartTime: Long? = null
    var currentYawAndPitch: Pair<Float, Float> = Pair(0f, 0f)
    val rotationVector = Vector3f(13F, rotationY, 0F)

    override fun renderWidget(context: GuiGraphics, pMouseX: Int, pMouseY: Int, partialTicks: Float) {
        if (!render) {
            return
        }
        isHovered = pMouseX >= x && pMouseY >= y && pMouseX < x + width && pMouseY < y + height
        renderPKM(context, partialTicks, pMouseX, pMouseY)
    }

    private fun renderPKM(context: GuiGraphics, partialTicks: Float, mouseX: Int, mouseY: Int) {
        val matrices = context.pose()
        matrices.pushPose()

        context.enableScissor(
            x,
            y,
            x + width,
            y + height
        )

        matrices.translate(x + width * 0.5, y.toDouble() + offsetY, 0.0)
        matrices.scale(baseScale, baseScale, baseScale)
        matrices.pushPose()

        if (isHovered) {
            lookStartTime = System.currentTimeMillis()
        }

        val timeLooking = lookStartTime?.let { (System.currentTimeMillis() - it) / 1000 } ?: MAX_SECONDS_LOOKING
        val lookedTooMuch = timeLooking >= MAX_SECONDS_LOOKING
        val resetToCenter = (!isHovered && lookedTooMuch) || !shouldFollowCursor || !Cobblemon.config.summaryPokemonFollowCursor

        if (resetToCenter) {
            lookStartTime = null
        }

        val rotation = Quaternionf().fromEulerXYZDegrees(rotationVector)

        if (shouldFollowCursor) {
            currentYawAndPitch = calculateHeadYawAndPitch(
                x + width / 2f,
                y + height / 2f,
                rotation,
                mouseX,
                mouseY,
                currentYawAndPitch.first,
                currentYawAndPitch.second,
                resetToCenter
            )
        }

        drawProfilePokemon(
            renderablePokemon = pokemon,
            matrixStack = matrices,
            rotation = rotation,
            state = state,
            partialTicks = partialTicks,
            headYaw = currentYawAndPitch.first,
            headPitch = currentYawAndPitch.second,
        )

        heldItemRenderer.renderOnModel(
            heldItem?: ItemStack.EMPTY,
            state.currentModel!!,
            state,
            matrices,
            context.bufferSource(),
            light = 0xF000F0,
            true
        )

        matrices.popPose()
        context.disableScissor()

        matrices.popPose()
    }

    override fun mouseClicked(pMouseX: Double, pMouseY: Double, pButton: Int): Boolean {
        if (this.isHovered) {
            playCry()
        }

        return super.mouseClicked(pMouseX, pMouseY, pButton)
    }

    private fun playCry() {
        if (playCryOnClick) {
            state.activeAnimations.clear()
            state.addFirstAnimation(setOf("cry"))
        }
    }
}