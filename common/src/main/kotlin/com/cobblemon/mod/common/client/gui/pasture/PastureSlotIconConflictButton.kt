/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui.pasture

import com.cobblemon.mod.common.api.gui.blitk
import com.cobblemon.mod.common.client.gui.CobblemonRenderable
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Button
import net.minecraft.client.sounds.SoundManager
import net.minecraft.network.chat.Component

class PastureSlotIconConflictButton(
    var xPos: Int, var yPos: Int,
    onPress: OnPress
) : Button(xPos, yPos, (SIZE * SCALE).toInt(), (SIZE * SCALE).toInt(), Component.literal("Pasture Move"), onPress, DEFAULT_NARRATION), CobblemonRenderable {

    companion object {
        const val SIZE = 14
        private const val SCALE = 0.5F

        private val baseResource = cobblemonResource("textures/gui/pasture/pasture_slot_icon_defend.png")
    }
    private var enabled: Boolean = false

    fun setEnabled(enabled: Boolean) {
        this.enabled = enabled
    }

    fun isEnabled(): Boolean = enabled

    override fun renderWidget(context: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        val hovered = isHovered(mouseX.toDouble(), mouseY.toDouble())

        val vOffset = when {
            !enabled && !hovered -> 0          // disabled
            !enabled && hovered -> SIZE        // disabled + hover
            enabled && !hovered -> SIZE * 2    // enabled
            else -> SIZE * 3                   // enabled + hover
        }

        blitk(
            matrixStack = context.pose(),
            x = xPos / SCALE,
            y = yPos / SCALE,
            width = SIZE,
            height = SIZE,
            vOffset = vOffset,
            textureHeight = SIZE * 4,
            texture = baseResource,
            scale = SCALE
        )
    }

    fun setPos(x: Int, y: Int) {
        xPos = x
        yPos = y
    }

    override fun playDownSound(pHandler: SoundManager) {
    }

    fun isHovered(mouseX: Double, mouseY: Double) = mouseX.toFloat() in (xPos.toFloat()..(xPos.toFloat() + (SIZE * SCALE))) && mouseY.toFloat() in (yPos.toFloat()..(yPos.toFloat() + (SIZE * SCALE)))
}