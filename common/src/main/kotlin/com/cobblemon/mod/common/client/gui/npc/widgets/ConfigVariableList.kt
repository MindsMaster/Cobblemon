/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui.npc.widgets

import com.cobblemon.mod.common.api.npc.NPCClasses
import com.cobblemon.mod.common.api.npc.configuration.NPCConfigVariable
import com.cobblemon.mod.common.api.npc.configuration.NPCConfigVariable.NPCVariableType
import com.cobblemon.mod.common.client.gui.npc.NPCEditorScreen
import com.cobblemon.mod.common.client.gui.npc.widgets.ConfigVariableList.ConfigVariable
import com.cobblemon.mod.common.client.render.drawScaledText
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.ContainerObjectSelectionList
import net.minecraft.client.gui.components.CycleButton
import net.minecraft.client.gui.components.EditBox
import net.minecraft.client.gui.components.Tooltip
import net.minecraft.client.gui.components.events.GuiEventListener
import net.minecraft.client.gui.narration.NarratableEntry

class ConfigVariableList(
    val listX: Int,
    val listY: Int,
    val parent: NPCEditorScreen
) : ContainerObjectSelectionList<ConfigVariable>(
    Minecraft.getInstance(),
    WIDTH, // width
    HEIGHT, // height
    0, // top
    SLOT_HEIGHT + SLOT_SPACING
) {
    companion object {
        const val WIDTH = 200
        const val HEIGHT = 120
        const val SLOT_WIDTH = 160
        const val SLOT_HEIGHT = 33
        const val SLOT_SPACING = 0
    }

    private var scrolling = false

    override fun getRowWidth() = SLOT_WIDTH

    init {
        this.x = listX
        this.y = listY
        correctSize()
        setRenderHeader(false, 0)
        val npcClass = NPCClasses.getByIdentifier(parent.dto.npcClass)
        if (npcClass != null) {
            npcClass.config.forEach { t ->
                val value = parent.dto.variables[t.variableName] ?: t.defaultValue
                addEntry(ConfigVariable(t, value, this))
            }
        }
    }

    override fun getScrollbarPosition() = x + width - 3

    public override fun addEntry(entry: ConfigVariable) = super.addEntry(entry)
    public override fun removeEntry(entry: ConfigVariable) = super.removeEntry(entry)

    override fun renderWidget(context: GuiGraphics, mouseX: Int, mouseY: Int, partialTicks: Float) {
        correctSize()

        super.renderWidget(context, mouseX, mouseY, partialTicks)

        // Scroll Overlay
//        blitk(
//            matrixStack = context.pose(),
//            texture = scrollOverlayResource,
//            x = listX,
//            y = listY - 13,
//            height = 131,
//            width = WIDTH
//        )
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        updateScrollingState(mouseX, mouseY)
        if (scrolling) {
            focused = getEntryAtPosition(mouseX, mouseY)
            isDragging = true
        }
        return super.mouseClicked(mouseX, mouseY, button)
    }

    override fun mouseDragged(mouseX: Double, mouseY: Double, button: Int, deltaX: Double, deltaY: Double): Boolean {
        if (scrolling) {
            if (mouseY < this.listY) {
                scrollAmount = 0.0
            } else if (mouseY > bottom) {
                scrollAmount = maxScroll.toDouble()
            } else {
                scrollAmount += deltaY
            }
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)
    }

    private fun updateScrollingState(mouseX: Double, mouseY: Double) {
        scrolling = mouseX >= this.scrollbarPosition.toDouble()
                && mouseX < (this.scrollbarPosition + 3).toDouble()
                && mouseY >= listY
                && mouseY < bottom
    }

    override fun renderListBackground(context: GuiGraphics) {}

    private fun correctSize() {
        setRectangle(WIDTH, HEIGHT, listX, (listY - 4))
//        setX(listX)
    }

    fun isHovered(mouseX: Double, mouseY: Double) = mouseX.toFloat() in (x.toFloat()..(x.toFloat() + WIDTH)) && mouseY.toFloat() in (y.toFloat()..(y.toFloat() + HEIGHT))

    class ConfigVariable(val variable: NPCConfigVariable, val value: String, private val parent: ConfigVariableList) : Entry<ConfigVariable>() {
        val client: Minecraft = Minecraft.getInstance()
        var _focused = false
        var children = mutableListOf<GuiEventListener>()

        var textValue = value
        var booleanValue = value.let { it.toDoubleOrNull() == 1.0 || it.toBooleanStrictOrNull() == true }

        val editBox = EditBox(
            client.font,
            parent.listX,
            parent.listY + 4,
            if (variable.type == NPCVariableType.NUMBER) SLOT_WIDTH - 100 else SLOT_WIDTH,
            SLOT_HEIGHT - 8,
            variable.displayName,
        ).also {
            it.tooltip = Tooltip.create(variable.description)
            it.height = SLOT_HEIGHT - 16
            it.setMaxLength(250)
            it.value = value
            it.setResponder {
                textValue = it
                parent.parent.dto.variables[variable.variableName] = it
            }
            if (variable.type == NPCVariableType.NUMBER) {
                it.setFilter { value -> value.toDoubleOrNull() != null || value.isBlank() || value == "." || value == "-" }
            }
        }
        val cycleButton = CycleButton.onOffBuilder(booleanValue).create(variable.displayName) { _, value ->
            booleanValue = value
            parent.parent.dto.variables[variable.variableName] = if (value) "1" else "0"
        }.also {
            it.height = SLOT_HEIGHT - 14
            it.width = SLOT_WIDTH - 8
            it.tooltip = Tooltip.create(variable.description)
        }

        init {
            if (variable.type == NPCVariableType.BOOLEAN) {
                children.add(cycleButton)
            } else {
                children.add(editBox)
            }
        }

        override fun isFocused() = _focused
        override fun setFocused(focused: Boolean) {
            this._focused = focused
        }

        override fun children() = children
        override fun narratables(): List<NarratableEntry> {
            return children.filterIsInstance<NarratableEntry>()
        }

        override fun render(
            context: GuiGraphics,
            index: Int,
            rowTop: Int,
            rowLeft: Int,
            rowWidth: Int,
            rowHeight: Int,
            mouseX: Int,
            mouseY: Int,
            isHovered: Boolean,
            partialTicks: Float
        ) {
            val x = rowLeft - 4
            val y = rowTop
            val matrixStack = context.pose()
//            blitk(
//                matrixStack = matrixStack,
//                texture = slotResource,
//                x = x,
//                y = y,
//                height = SLOT_HEIGHT,
//                width = rowWidth,
//                vOffset = if (isHovered) SLOT_HEIGHT else 0,
//                textureHeight = SLOT_HEIGHT * 2
//            )

            if (variable.type == NPCVariableType.BOOLEAN) {
                cycleButton.x = x
                cycleButton.y = y + 6
                cycleButton.render(context, mouseX, mouseY, partialTicks)
            } else {
                drawScaledText(
                    context = context,
                    text = variable.displayName.visualOrderText,
                    x = x,
                    y = y + 4
                )
                editBox.x = x
                editBox.y = y + 14
                editBox.render(context, mouseX, mouseY, partialTicks)
            }
        }


//        override fun mouseClicked(mouseX: Double, mouseY: Double, delta: Int): Boolean {
//            if (moveButton.isHovered(mouseX, mouseY) && canUnpasture()) {
//                moveButton.onPress()
//                return true
//            }
//            return false
//        }
    }
}