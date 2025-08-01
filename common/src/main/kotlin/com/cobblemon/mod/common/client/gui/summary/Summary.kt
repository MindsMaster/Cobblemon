/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui.summary

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.CobblemonItems
import com.cobblemon.mod.common.CobblemonNetwork.sendToServer
import com.cobblemon.mod.common.CobblemonSounds
import com.cobblemon.mod.common.api.gui.blitk
import com.cobblemon.mod.common.api.moves.Move
import com.cobblemon.mod.common.api.reactive.Observable.Companion.stopAfter
import com.cobblemon.mod.common.api.scheduling.Schedulable
import com.cobblemon.mod.common.api.scheduling.SchedulingTracker
import com.cobblemon.mod.common.api.storage.party.PartyPosition
import com.cobblemon.mod.common.api.text.bold
import com.cobblemon.mod.common.api.text.text
import com.cobblemon.mod.common.client.CobblemonClient
import com.cobblemon.mod.common.client.CobblemonResources
import com.cobblemon.mod.common.client.gui.CobblemonRenderable
import com.cobblemon.mod.common.client.gui.ExitButton
import com.cobblemon.mod.common.client.gui.TypeIcon
import com.cobblemon.mod.common.client.gui.summary.widgets.*
import com.cobblemon.mod.common.client.gui.summary.widgets.screens.SummaryTab
import com.cobblemon.mod.common.client.gui.summary.widgets.screens.info.InfoWidget
import com.cobblemon.mod.common.client.gui.summary.widgets.screens.marks.MarksWidget
import com.cobblemon.mod.common.client.gui.summary.widgets.screens.moves.MoveSwapScreen
import com.cobblemon.mod.common.client.gui.summary.widgets.screens.moves.MovesWidget
import com.cobblemon.mod.common.client.gui.summary.widgets.screens.stats.StatWidget
import com.cobblemon.mod.common.client.render.drawScaledText
import com.cobblemon.mod.common.client.render.models.blockbench.repository.VaryingModelRepository
import com.cobblemon.mod.common.net.messages.server.pokemon.update.SetItemHiddenPacket
import com.cobblemon.mod.common.net.messages.server.storage.party.MovePartyPokemonPacket
import com.cobblemon.mod.common.net.messages.server.storage.party.SwapPartyPokemonPacket
import com.cobblemon.mod.common.pokemon.Gender
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.isInventoryKeyPressed
import com.cobblemon.mod.common.util.lang
import com.mojang.blaze3d.platform.InputConstants
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.components.Renderable
import net.minecraft.client.gui.components.events.GuiEventListener
import net.minecraft.client.gui.narration.NarratableEntry
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.resources.sounds.SimpleSoundInstance
import net.minecraft.network.chat.Component
import net.minecraft.sounds.SoundEvent

/**
 * The screen responsible for displaying various information regarding a Pokémon team.
 *
 * @property party The party that will be displayed.
 * @property editable Whether you shall be able to edit Pokémon with operations such as reordering their move set.
 *
 * @param selection The index the [party] will have as the base [selectedPokemon].
 */
class Summary private constructor(party: Collection<Pokemon?>, private val editable: Boolean, private val selection: Int): Screen(
    Component.translatable("cobblemon.ui.summary.title")), Schedulable, CobblemonRenderable {

    companion object {
        const val BASE_WIDTH = 331
        const val BASE_HEIGHT = 161
        private const val PORTRAIT_SIZE = 66
        private const val SCALE = 0.5F

        // Main Screen Index
        private const val INFO = 0
        private const val MOVES = 1
        private const val STATS = 2
        private const val MARKS = 3

        // Side Screen Index
        const val PARTY = 0
        const val MOVE_SWAP = 1
        const val EVOLVE = 2

        // Resources
        private val baseResource = cobblemonResource("textures/gui/summary/summary_base.png")
        private val portraitBackgroundResource = cobblemonResource("textures/gui/summary/portrait_background.png")
        private val typeSpacerResource = cobblemonResource("textures/gui/summary/type_spacer.png")
        private val typeSpacerDoubleResource = cobblemonResource("textures/gui/summary/type_spacer_double.png")
        private val sideSpacerResource = cobblemonResource("textures/gui/summary/summary_side_spacer.png")
        private val evolveButtonResource = cobblemonResource("textures/gui/summary/summary_evolve_button.png")
        private val itemVisibleResource = cobblemonResource("textures/gui/summary/item_visible.png")
        private val itemHiddenResource = cobblemonResource("textures/gui/summary/item_hidden.png")
        private val tabIconInfo = cobblemonResource("textures/gui/summary/summary_tab_icon_info.png")
        private val tabIconMoves = cobblemonResource("textures/gui/summary/summary_tab_icon_moves.png")
        private val tabIconStats = cobblemonResource("textures/gui/summary/summary_tab_icon_stats.png")
        private val tabIconMarks = cobblemonResource("textures/gui/summary/summary_tab_icon_marks.png")
        val iconShinyResource = cobblemonResource("textures/gui/summary/icon_shiny.png")
        val iconHeldItemResource = cobblemonResource("textures/gui/summary/icon_item_held.png")
        val iconCosmeticItemResource = cobblemonResource("textures/gui/summary/icon_item_cosmetic.png")

        /**
         * Attempts to open this screen for a client.
         * If an exception is thrown this screen will not open.
         *
         * @param party The party to be displayed.
         * @param editable Whether you shall be able to edit Pokémon with operations such as reordering their move set.
         * @param selection The index to start as the selected party member, based on the [party].
         *
         * @throws IllegalArgumentException If the [party] is empty or contains more than 6 members.
         * @throws IndexOutOfBoundsException If the [selection] is not a possible index of [party].
         */
        fun open(party: Collection<Pokemon?>, editable: Boolean = true, selection: Int = 0) {
            val mc = Minecraft.getInstance()
            val screen = Summary(party, editable, selection)
            mc.setScreen(screen)
        }
    }

    override val schedulingTracker = SchedulingTracker()

    private val party = ArrayList(party)
    internal var selectedPokemon: Pokemon = this.party[selection] ?: this.party.first { it != null }!!
    private lateinit var mainScreen: AbstractWidget
    lateinit var sideScreen: GuiEventListener
    private lateinit var modelWidget: ModelWidget
    private lateinit var nicknameEntryWidget: NicknameEntryWidget
    private lateinit var markingsWidget: MarkingsWidget
    private val summaryTabs = mutableListOf<SummaryTab>()
    private var showCosmeticItem = false
    private lateinit var heldItemVisibilityButton: SummaryButton
    private var mainScreenIndex = INFO
    var sideScreenIndex = PARTY

    override fun renderBlurredBackground(delta: Float) { }

    /**
     * Initializes the Summary Screen
     */
    override fun init() {
        super.init()
        if (this.party.isEmpty()) {
            throw IllegalArgumentException("Summary UI cannot display zero Pokemon")
        }
        if (this.party.size > 6) {
            throw IllegalArgumentException("Summary UI cannot display more than six Pokemon")
        }
        switchSelection(selection)
        this.listenToMoveSet()

        val x = (width - BASE_WIDTH) / 2
        val y = (height - BASE_HEIGHT) / 2

        displayMainScreen(mainScreenIndex)
        displaySideScreen(PARTY)

        markingsWidget = MarkingsWidget(
            x + 29,
            y + 102,
            selectedPokemon
        )
        addRenderableWidget(markingsWidget)

        // Evolve Button
        addRenderableWidget(
            SummaryButton(
                buttonX = x + 12F,
                buttonY = y + 145F,
                buttonWidth = 54,
                buttonHeight = 15,
                clickAction = {
                    momentarily { displaySideScreen(if (sideScreenIndex == EVOLVE) PARTY else EVOLVE) }
                },
                text = lang("ui.evolve"),
                resource = evolveButtonResource,
                renderRequirement = {
                    selectedPokemon.evolutionProxy.client().isNotEmpty()
                        && CobblemonClient.battle == null
                        && selectedPokemon.heldItem.item != CobblemonItems.EVERSTONE
                },
                clickRequirement = {
                    selectedPokemon.evolutionProxy.client().isNotEmpty()
                        && CobblemonClient.battle == null
                        && selectedPokemon.heldItem.item != CobblemonItems.EVERSTONE
                }
            )
        )

        //Item Visibility Button
        heldItemVisibilityButton = SummaryButton(
            buttonX = x + 3F,
            buttonY = y + 104F,
            buttonWidth = 32,
            buttonHeight = 32,
            scale = 0.5F,
            resource = itemVisibleResource,
            activeResource = itemHiddenResource,
            clickAction = {
                selectedPokemon.heldItemVisible = !selectedPokemon.heldItemVisible
                heldItemVisibilityButton.buttonActive = !selectedPokemon.heldItemVisible
                // Send item visibility update to server
                sendToServer(SetItemHiddenPacket(selectedPokemon.uuid, selectedPokemon.heldItemVisible))
            },
            renderRequirement = { !selectedPokemon.heldItemNoCopy().isEmpty && !showCosmeticItem },
            clickRequirement = { !selectedPokemon.heldItemNoCopy().isEmpty && !showCosmeticItem }
        )
        heldItemVisibilityButton.buttonActive = !selectedPokemon.heldItemVisible
        addRenderableWidget(heldItemVisibilityButton)

        // Held/Cosmetic Item Button
        addRenderableWidget(
            SummaryButton(
                buttonX = x + 67F,
                buttonY = y + 113F,
                buttonWidth = 12,
                buttonHeight = 12,
                scale = 0.5F,
                resource = iconCosmeticItemResource,
                activeResource = iconHeldItemResource,
                clickAction = {
                    showCosmeticItem = !showCosmeticItem
                    (it as SummaryButton).buttonActive = showCosmeticItem
                }
            )
        )

        // Init Tabs
        summaryTabs.clear()
        summaryTabs.add(
            SummaryTab(pX = x + 78, pY = y - 1, icon = tabIconInfo) {
                if (mainScreenIndex != INFO) displayMainScreen(INFO)
            }
        )

        summaryTabs.add(
            SummaryTab(pX = x + 109, pY = y - 1, icon = tabIconMoves) {
                if (mainScreenIndex != MOVES) displayMainScreen(MOVES)
            }
        )

        summaryTabs.add(
            SummaryTab(pX = x + 140, pY = y - 1, icon = tabIconStats) {
                if (mainScreenIndex != STATS) displayMainScreen(STATS)
            }
        )

        summaryTabs.add(
            SummaryTab(pX = x + 171, pY = y - 1, icon = tabIconMarks) {
                if (mainScreenIndex != MARKS) displayMainScreen(MARKS)
            }
        )

        summaryTabs[mainScreenIndex].toggleTab()
        summaryTabs.forEach { addRenderableWidget(it) }

        // Add Exit Button
        addRenderableWidget(
            ExitButton(pX = x + 302, pY = y + 145) {
                playSound(CobblemonSounds.GUI_CLICK)
                if (sideScreenIndex != PARTY) {
                    displaySideScreen(PARTY)
                } else {
                    saveActiveMark()
                    saveMarkings()
                    Minecraft.getInstance().setScreen(null)
                }
            }
        )

        // Add Nickname Entry
        nicknameEntryWidget = NicknameEntryWidget(
            selectedPokemon,
            x = x + 12,
            y = (y + 14.5).toInt(),
            width = 50,
            height = 10,
            isParty = true,
            lang("ui.nickname")
        )
        focused = nicknameEntryWidget
        nicknameEntryWidget.isFocused = false
        addRenderableWidget(nicknameEntryWidget)

        // Add Model Preview
        modelWidget = ModelWidget(
            pX = x + 6,
            pY = y + 32,
            pWidth = PORTRAIT_SIZE,
            pHeight = PORTRAIT_SIZE,
            pokemon = selectedPokemon.asRenderablePokemon(),
            baseScale = 2F,
            rotationY = 325F,
            offsetY = -10.0,
            shouldFollowCursor = true,
        )
        addRenderableOnly(this.modelWidget)
    }

    fun swapPartySlot(sourceIndex: Int, targetIndex: Int) {
        if (sourceIndex >= this.party.size || targetIndex >= this.party.size) {
            return
        }

        val sourcePokemon = this.party.getOrNull(sourceIndex)

        if (sourcePokemon != null) {
            val targetPokemon = this.party.getOrNull(targetIndex)

            val sourcePosition = PartyPosition(sourceIndex)
            val targetPosition = PartyPosition(targetIndex)

            val packet = targetPokemon?.let { SwapPartyPokemonPacket(it.uuid, targetPosition, sourcePokemon.uuid, sourcePosition) }
                    ?: MovePartyPokemonPacket(sourcePokemon.uuid, sourcePosition, targetPosition)
            packet.sendToServer()

            // Update change in UI
            this.party[targetIndex] = sourcePokemon
            this.party[sourceIndex] = targetPokemon
            displaySideScreen(PARTY)
            (sideScreen as PartyWidget).enableSwap()
        }
    }

    /**
     * Switches the selected PKM
     */
    fun switchSelection(newSelection: Int) {
        saveMarkings()
        this.selectedPokemon.moveSet.changeFunction = {}
        this.party.getOrNull(newSelection)?.let { newPokemon ->
            newPokemon.changeObservable.pipe( stopAfter { Minecraft.getInstance().screen != this || this.selectedPokemon != newPokemon } ).subscribe {
                updatePokemonInfo()
            }
            this.selectedPokemon = newPokemon
        }
        updatePokemonInfo()
        listenToMoveSet()
        displayMainScreen(mainScreenIndex)
        if (::markingsWidget.isInitialized) markingsWidget.setActivePokemon(selectedPokemon)
        children().find { it is EvolutionSelectScreen }?.let(this::removeWidget)
    }

    fun updatePokemonInfo() {
        val pokemon = selectedPokemon.asRenderablePokemon()
        if (::modelWidget.isInitialized && modelWidget.pokemon != pokemon) {
            modelWidget.pokemon = pokemon
            heldItemVisibilityButton.buttonActive = !selectedPokemon.heldItemVisible
            if (::nicknameEntryWidget.isInitialized) nicknameEntryWidget.setSelectedPokemon(selectedPokemon)
        }
    }

    /**
     * Start observing the MoveSet of the current PKM for changes
     */
    private fun listenToMoveSet() {
        selectedPokemon.moveSet.changeFunction = {
            if (mainScreen is MovesWidget) {
                displayMainScreen(MOVES)
            }
        }
    }

    private fun saveMarkings() {
        if (::markingsWidget.isInitialized) markingsWidget.saveMarkingsToPokemon()
    }

    private fun saveActiveMark() {
        if (mainScreenIndex == MARKS && mainScreen is MarksWidget) (mainScreen as MarksWidget).saveActiveMarkToPokemon()
    }

    /**
     * Returns if this Screen is open or not
     */
    private fun isOpen() = Minecraft.getInstance().screen == this

    /**
     * Switch center screen
     */
    private fun displayMainScreen(screen: Int) {
        // Save mark to Pokémon if any changes when switching out of marks screen
        saveActiveMark()

        // Get stat tab index if currently displaying stat screen
        val subIndex = if (mainScreenIndex == STATS && mainScreen is StatWidget) (mainScreen as StatWidget).statTabIndex else 0

        mainScreenIndex = screen
        if (::mainScreen.isInitialized) removeWidget(mainScreen)
        if (sideScreenIndex == MOVE_SWAP) displaySideScreen(PARTY)

        summaryTabs.forEachIndexed { index, item ->
            if (index == screen) item.toggleTab() else item.toggleTab(false)
        }

        val x = (width - BASE_WIDTH) / 2
        val y = (height - BASE_HEIGHT) / 2

        when (screen) {
            INFO -> {
                mainScreen = InfoWidget(
                    pX = x + 77,
                    pY = y + 12,
                    pokemon = this.selectedPokemon
                )
            }

            MOVES -> {
                mainScreen = MovesWidget(
                    pX = x + 77,
                    pY = y + 12,
                    summary = this
                )
            }

            STATS -> {
                mainScreen = StatWidget(
                    pX = x + 77,
                    pY = y + 12,
                    pokemon = this.selectedPokemon,
                    tabIndex = subIndex
                )
            }

            MARKS -> {
                mainScreen = MarksWidget(
                    pX = x + 77,
                    pY = y + 12,
                    pokemon = this.selectedPokemon
                )
            }
        }
        addRenderableWidget(mainScreen)
    }

    /**
     * Switch right screen
     */
    fun displaySideScreen(screen: Int, move: Move? = null) {
        sideScreenIndex = screen
        if (::sideScreen.isInitialized) removeWidget(sideScreen)

        val x = (width - BASE_WIDTH) / 2
        val y = (height - BASE_HEIGHT) / 2

        when (screen) {
            PARTY -> {
                sideScreen = PartyWidget(
                        pX = x + 216,
                        pY = y + 24,
                        isParty = selectedPokemon in CobblemonClient.storage.party,
                        summary = this,
                        partyList = this.party
                )
            }

            MOVE_SWAP -> {
                val movesWidget = mainScreen
                if (movesWidget is MovesWidget) {
                    sideScreen = MoveSwapScreen(
                        x + 216,
                        y + 23,
                        movesWidget = movesWidget,
                        replacedMove = move
                    ).also { switchPane ->
                        val pokemon = selectedPokemon
                        var moveSlotList = pokemon.relearnableMoves.map { template ->
                            val benched = pokemon.benchedMoves.find { it.moveTemplate == template }
                            MoveSwapScreen.MoveSlot(switchPane, template, benched?.ppRaisedStages ?: 0, pokemon)
                        }
                        if (pokemon.moveSet.getMoves().size > 1 && move != null) {
                            // Adds the "Forget" slot
                            moveSlotList += MoveSwapScreen.MoveSlot(switchPane, null, 0, pokemon)
                        }
                        moveSlotList.forEach { switchPane.addEntry(it) }
                    }
                }
            }

            EVOLVE -> {
                saveActiveMark()
                saveMarkings()
                sideScreen = EvolutionSelectScreen(
                        x + 216,
                        y + 23,
                        pokemon = selectedPokemon
                )
            }
        }
        val element = sideScreen
        if (element is Renderable && element is NarratableEntry) {
            addRenderableWidget(element)
        }
    }

    override fun renderMenuBackground(context: GuiGraphics) {}

    override fun render(context: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        schedulingTracker.update(delta / 20F)

        val x = (width - BASE_WIDTH) / 2
        val y = (height - BASE_HEIGHT) / 2
        val matrices = context.pose()

        // Render Portrait Background
        blitk(
            matrixStack = matrices,
            texture = portraitBackgroundResource,
            x = x + 6,
            y = y + 32,
            width = PORTRAIT_SIZE,
            height = PORTRAIT_SIZE
        )

        //modelWidget.render(context, pMouseX, pMouseY, pPartialTicks)

        // Render Base Resource
        blitk(
            matrixStack = matrices,
            texture = baseResource,
            x = x,
            y = y,
            width = BASE_WIDTH,
            height = BASE_HEIGHT
        )

        // Status
        val status = selectedPokemon.status?.status
        if (selectedPokemon.isFainted() || status != null) {
            val statusName = if (selectedPokemon.isFainted()) "fnt" else status?.showdownName
            blitk(
                matrixStack = matrices,
                texture = cobblemonResource("textures/gui/battle/battle_status_$statusName.png"),
                x = x + 34,
                y = y + 4,
                height = 7,
                width = 39,
                uOffset = 35,
                textureWidth = 74
            )

            blitk(
                matrixStack = matrices,
                texture = cobblemonResource("textures/gui/summary/status_trim.png"),
                x = x + 34,
                y = y + 5,
                height = 6,
                width = 3
            )

            drawScaledText(
                context = context,
                font = CobblemonResources.DEFAULT_LARGE,
                text = lang("ui.status.$statusName").bold(),
                x = x + 39,
                y = y + 3
            )
        }

        // Poké Ball
        val ballResource = cobblemonResource("textures/item/poke_balls/" + selectedPokemon.caughtBall.name.path + ".png")
        blitk(
            matrixStack = matrices,
            texture = ballResource,
            x = (x + 3.5) / SCALE,
            y = (y + 15) / SCALE,
            width = 16,
            height = 16,
            scale = SCALE
        )

        if (selectedPokemon.gender != Gender.GENDERLESS) {
            val isMale = selectedPokemon.gender == Gender.MALE
            val textSymbol = if (isMale) "♂".text().bold() else "♀".text().bold()
            drawScaledText(
                context = context,
                font = CobblemonResources.DEFAULT_LARGE,
                text = textSymbol,
                x = x + 69, // 64 when tag icon is implemented
                y = y + 14.5,
                colour = if (isMale) 0x32CBFF else 0xFC5454,
                shadow = true
            )
        }

        drawScaledText(
            context = context,
            font = CobblemonResources.DEFAULT_LARGE,
            text = lang("ui.lv").bold(),
            x = x + 6,
            y = y + 4.5,
            shadow = true
        )

        drawScaledText(
            context = context,
            font = CobblemonResources.DEFAULT_LARGE,
            text = selectedPokemon.level.toString().text().bold(),
            x = x + 19,
            y = y + 4.5,
            shadow = true
        )

        // Shiny Icon
        if (selectedPokemon.shiny) {
            blitk(
                matrixStack = matrices,
                texture = iconShinyResource,
                x = (x + 62.5) / SCALE,
                y = (y + 33.5) / SCALE,
                width = 16,
                height = 16,
                scale = SCALE
            )
        }

        // Type Icon(s)
        blitk(
            matrixStack = matrices,
            texture = if (selectedPokemon.secondaryType != null) typeSpacerDoubleResource else typeSpacerResource,
            x = (x + 5.5) / SCALE,
            y = (y + 126) / SCALE,
            width = 134,
            height = 24,
            scale = SCALE
        )

        // Held/Cosmetic Item
        val displayedItem = if (showCosmeticItem) selectedPokemon.cosmeticItem else selectedPokemon.heldItemNoCopy()
        val itemX = x + 3
        val itemY = y + 104
        if (!displayedItem.isEmpty) {
            context.renderItem(displayedItem, itemX, itemY)
            context.renderItemDecorations(Minecraft.getInstance().font, displayedItem, itemX, itemY)
        }

        drawScaledText(
            context = context,
            text = lang("${if (showCosmeticItem) "cosmetic" else "held"}_item"),
            x = x + 24,
            y = y + 114.5,
            scale = SCALE
        )

        TypeIcon(
            x = x + 39,
            y = y + 123,
            type = selectedPokemon.primaryType,
            secondaryType = selectedPokemon.secondaryType,
            centeredX = true
        ).render(context)

        blitk(
            matrixStack = matrices,
            texture = sideSpacerResource,
            x = (x + 217) / SCALE,
            y = (y + 141) / SCALE,
            width = 144,
            height = 14,
            scale = SCALE
        )

        matrices.pushPose()
        // Prevent widgets from being overlapped by other components
        matrices.translate(0.0, 0.0, 1000.0)

        // Render all added Widgets
        super.render(context, mouseX, mouseY, delta)

        // Render Item Tooltip
        if (!displayedItem.isEmpty) {
            val itemHovered = mouseX.toFloat() in (itemX.toFloat()..(itemX.toFloat() + 16)) && mouseY.toFloat() in (itemY.toFloat()..(itemY.toFloat() + 16))
            if (itemHovered) context.renderTooltip(Minecraft.getInstance().font, displayedItem, mouseX, mouseY)
        }
        matrices.popPose()
    }

    /**
     * Whether this Screen should pause the Game in SinglePlayer
     */
    override fun isPauseScreen(): Boolean = false

    override fun mouseScrolled(mouseX: Double, mouseY: Double, amount: Double, verticalAmount: Double): Boolean {
        return children().any { it.mouseScrolled(mouseX, mouseY, amount, verticalAmount) }
    }

    override fun mouseDragged(mouseX: Double, mouseY: Double, button: Int, deltaX: Double, deltaY: Double): Boolean {
        if (sideScreenIndex == MOVE_SWAP || sideScreenIndex == EVOLVE) sideScreen.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)
        if (mainScreenIndex == MOVES) mainScreen.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        val nicknameSelected = this::nicknameEntryWidget.isInitialized && nicknameEntryWidget.isFocused

        if (keyCode == InputConstants.KEY_ESCAPE) {
            saveActiveMark()
            saveMarkings()
        }


        if (isInventoryKeyPressed(minecraft, keyCode, scanCode) && !nicknameSelected) {
            saveActiveMark()
            saveMarkings()
            Minecraft.getInstance().setScreen(null)
            return true
        }

        if ((keyCode == InputConstants.KEY_RETURN || keyCode == InputConstants.KEY_NUMPADENTER)
            && this::nicknameEntryWidget.isInitialized
            && this.nicknameEntryWidget.isFocused
        ) {
            this.focused = null
        }
        if (Cobblemon.config.enableDebugKeys) {
            val model = VaryingModelRepository.getPoser(selectedPokemon.species.resourceIdentifier, modelWidget.state)
            if (keyCode == InputConstants.KEY_UP) {
                model.profileTranslation = model.profileTranslation.add(0.0, -0.01, 0.0)
            }
            if (keyCode == InputConstants.KEY_DOWN) {
                model.profileTranslation = model.profileTranslation.add(0.0, 0.01, 0.0)
            }
            if (keyCode == InputConstants.KEY_LEFT) {
                model.profileTranslation = model.profileTranslation.add(-0.01, 0.0, 0.0)
            }
            if (keyCode == InputConstants.KEY_RIGHT) {
                model.profileTranslation = model.profileTranslation.add(0.01, 0.0, 0.0)
            }
            if (keyCode == InputConstants.KEY_EQUALS) {
                model.profileScale += 0.01F
            }
            if (keyCode == InputConstants.KEY_MINUS) {
                model.profileScale -= 0.01F
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers)
    }

    fun playSound(soundEvent: SoundEvent) {
        Minecraft.getInstance().soundManager.play(SimpleSoundInstance.forUI(soundEvent, 1.0F))
    }

    override fun onClose() {
        if (Cobblemon.config.enableDebugKeys) {
            val model = VaryingModelRepository.getPoser(selectedPokemon.species.resourceIdentifier, modelWidget.state)
            Minecraft.getInstance().player?.sendSystemMessage(Component.literal("Profile Translation: ${model.profileTranslation}"))
            Minecraft.getInstance().player?.sendSystemMessage(Component.literal("Profile Scale: ${model.profileScale}"))
            Cobblemon.LOGGER.info("override var profileTranslation = Vec3d(${model.profileTranslation.x}, ${model.profileTranslation.y}, ${model.profileTranslation.z})")
            Cobblemon.LOGGER.info("override var profileScale = ${model.profileScale}F")
        }
        super.onClose()
    }
}