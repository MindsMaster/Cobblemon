/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui.summary.widgets.screens.moves

import com.cobblemon.mod.common.CobblemonNetwork
import com.cobblemon.mod.common.api.gui.blitk
import com.cobblemon.mod.common.api.moves.Move
import com.cobblemon.mod.common.api.text.text
import com.cobblemon.mod.common.client.CobblemonClient
import com.cobblemon.mod.common.client.gui.summary.Summary
import com.cobblemon.mod.common.client.gui.summary.widgets.SoundlessWidget
import com.cobblemon.mod.common.client.render.drawScaledText
import com.cobblemon.mod.common.net.messages.server.RequestMoveSwapPacket
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.lang
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.network.chat.Component
import java.math.RoundingMode
import java.text.DecimalFormat

class MovesWidget(
    pX: Int, pY: Int,
    val summary: Summary
): SoundlessWidget(pX, pY, WIDTH, HEIGHT, Component.literal("MovesWidget")) {
    companion object {
        private const val WIDTH = 134
        private const val HEIGHT = 148
        const val MOVE_ICON_SIZE = 10
        const val SCALE = 0.5F

        private val decimalFormat = DecimalFormat("#.##").also {
            it.roundingMode = RoundingMode.CEILING
        }

        private val movesBaseResource = cobblemonResource("textures/gui/summary/summary_moves_base.png")
        val movesPowerIconResource = cobblemonResource("textures/gui/summary/summary_moves_icon_power.png")
        val movesAccuracyIconResource = cobblemonResource("textures/gui/summary/summary_moves_icon_accuracy.png")
        val movesEffectIconResource = cobblemonResource("textures/gui/summary/summary_moves_icon_effect.png")
    }

    var selectedMove: Move? = null

    private var index = -1
    private val moves = (
            if (summary.selectedPokemon.relearnableMoves.count() > 0)
                summary.selectedPokemon.moveSet.getMovesWithNulls()
            else summary.selectedPokemon.moveSet
        ).map { move ->
        index++
        MoveSlotWidget(
            x + 13,
            y + 6 + (MoveSlotWidget.MOVE_HEIGHT + 3) * index,
            move,
            this,
            summary.selectedPokemon
        )
    }.toMutableList().onEach {
        addWidget(it)
    }

    private var descriptionScrollList = MoveDescriptionScrollList(
        x + 69,
        y + 113,
        5
    )


    override fun renderWidget(context: GuiGraphics, pMouseX: Int, pMouseY: Int, pPartialTicks: Float) {
        val matrices = context.pose()

        blitk(
            matrixStack = matrices,
            texture = movesBaseResource,
            x= x,
            y = y,
            width = width,
            height = height
        )

        moves.forEach {
            it.render(context, pMouseX, pMouseY, pPartialTicks)
        }

        // Move icons
        blitk(
            matrixStack = matrices,
            texture = movesPowerIconResource,
            x= (x + 7) / SCALE,
            y = (y + 114.5) / SCALE,
            width = MOVE_ICON_SIZE,
            height = MOVE_ICON_SIZE,
            scale = SCALE
        )

        blitk(
            matrixStack = matrices,
            texture = movesAccuracyIconResource,
            x= (x + 7) / SCALE,
            y = (y + 125.5) / SCALE,
            width = MOVE_ICON_SIZE,
            height = MOVE_ICON_SIZE,
            scale = SCALE
        )

        blitk(
            matrixStack = matrices,
            texture = movesEffectIconResource,
            x= (x + 7) / SCALE,
            y = (y + 136.5) / SCALE,
            width = MOVE_ICON_SIZE,
            height = MOVE_ICON_SIZE,
            scale = SCALE
        )

        drawScaledText(
            context = context,
            text = lang("ui.power"),
            x = x + 14,
            y = y + 115,
            scale = SCALE,
            shadow = true
        )

        drawScaledText(
            context = context,
            text = lang("ui.accuracy"),
            x = x + 14,
            y = y + 126,
            scale = SCALE,
            shadow = true
        )

        drawScaledText(
            context = context,
            text = lang("ui.effect"),
            x = x + 14,
            y = y + 137,
            scale = SCALE,
            shadow = true
        )

        val mcFont = Minecraft.getInstance().font
        val movePower = if (selectedMove != null && selectedMove!!.power.toInt() > 0) selectedMove!!.power.toInt().toString().text() else "—".text()
        drawScaledText(
            context = context,
            text = movePower,
            x = (x + 62.5) - (mcFont.width(movePower) * SCALE),
            y = y + 115,
            scale = SCALE,
            shadow = true
        )

        val moveAccuracy = if (selectedMove != null) format(selectedMove!!.accuracy).text() else "—".text()
        drawScaledText(
            context = context,
            text = moveAccuracy,
            x = (x + 62.5) - (mcFont.width(moveAccuracy) * SCALE),
            y = y + 126,
            scale = SCALE,
            shadow = true
        )

        val moveEffect = if (selectedMove != null) format(selectedMove!!.effectChances.firstOrNull() ?: 0.0).text() else "—".text()
        drawScaledText(
            context = context,
            text = moveEffect,
            x = (x + 62.5) - (mcFont.width(moveEffect) * SCALE),
            y = y + 137,
            scale = SCALE,
            shadow = true
        )

         // Render move description
        if (selectedMove != null) {
            descriptionScrollList.renderWidget(context, pMouseX, pMouseY, pPartialTicks)
        }
    }

    fun reorderMove(move: MoveSlotWidget, up: Boolean) {
        val movePos = moves.indexOf(move)
        if (moves.size <= movePos || movePos == -1) {
            return
        }
        var targetSlot: Int
        if (up) {
            targetSlot = movePos - 1
            if (targetSlot == -1)
                targetSlot = moves.size - 1
        } else {
            targetSlot = movePos + 1
            if (targetSlot >= moves.size)
                targetSlot = 0
        }

        CobblemonNetwork.sendToServer(
            RequestMoveSwapPacket(
                move1 = movePos,
                move2 = targetSlot,
                slot = CobblemonClient.storage.party.getPosition(summary.selectedPokemon.uuid)
            )
        )
    }

    fun format(input: Double): String {
        if (input <= 0) return "—"
        return "${decimalFormat.format(input)}%"
    }

    fun selectMove(move: Move?) {
        selectedMove = if (selectedMove == move || move == null) null else move
        descriptionScrollList.setMoveDescription(selectedMove?.description ?: "".text())
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (descriptionScrollList.isHovered) descriptionScrollList.mouseClicked(mouseX, mouseY, button)
        return super.mouseClicked(mouseX, mouseY, button)
    }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, horizontalAmount: Double, verticalAmount: Double): Boolean {
        if (descriptionScrollList.isHovered) descriptionScrollList.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)
    }

    override fun mouseDragged(mouseX: Double, mouseY: Double, button: Int, deltaX: Double, deltaY: Double): Boolean {
        if (descriptionScrollList.isHovered) descriptionScrollList.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)
    }
}