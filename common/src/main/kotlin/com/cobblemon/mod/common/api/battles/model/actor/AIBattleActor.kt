/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.battles.model.actor

import com.cobblemon.mod.common.Cobblemon.LOGGER
import com.cobblemon.mod.common.api.battles.model.ai.BattleAI
import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.battles.PassActionResponse
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon
import com.cobblemon.mod.common.exception.IllegalActionChoiceException
import com.cobblemon.mod.common.net.messages.client.battle.BattleFaintPacket
import com.cobblemon.mod.common.net.messages.client.battle.BattleHealthChangePacket
import com.cobblemon.mod.common.net.messages.client.battle.BattleMakeChoicePacket
import com.cobblemon.mod.common.net.messages.client.battle.BattlePersistentStatusPacket
import com.cobblemon.mod.common.net.messages.client.battle.BattleReplacePokemonPacket
import com.cobblemon.mod.common.net.messages.client.battle.BattleSwapPokemonPacket
import com.cobblemon.mod.common.net.messages.client.battle.BattleSwitchPokemonPacket
import com.cobblemon.mod.common.net.messages.client.battle.BattleTransformPokemonPacket
import java.util.UUID

abstract class AIBattleActor(
    gameId: UUID,
    pokemonList: List<BattlePokemon>,
    val battleAI: BattleAI
) : BattleActor(gameId, pokemonList.toMutableList()) {
    override fun sendUpdate(packet: NetworkPacket<*>) {
        super.sendUpdate(packet)

        when {
            packet is BattleMakeChoicePacket -> this.onChoiceRequested()
            packet is BattlePersistentStatusPacket -> {}
            packet is BattleFaintPacket -> {}
            packet is BattleSwitchPokemonPacket && !packet.isAlly -> {} //TODO this might be redundant; switch on opposing site should be visible during turn
            packet is BattleSwapPokemonPacket -> {} //TODO CONSIDER MULTI SHIFTING
            packet is BattleHealthChangePacket -> battleAI.onHealthChange(packet)
            packet is BattleTransformPokemonPacket && !packet.isAlly -> {} //TODO this might be redundant; switch on opposing site should be visible during turn
            packet is BattleReplacePokemonPacket && !packet.isAlly -> {} //TODO this might be redundant; switch on opposing site should be visible during turn
        }
    }

    /**
     * Called when the AI is requested to make a choice.
     */
    open fun onChoiceRequested() {
        try {
            setActionResponses(request!!.iterate(this.activePokemon) { battleMon, moveset, forceSwitch ->
                battleAI.choose(battleMon, battle, getSide(), moveset, forceSwitch)
            })
        } catch (exception: IllegalActionChoiceException) {
            LOGGER.error("AI was unable to choose a move, we're going to need to pass!")
            exception.printStackTrace()
            setActionResponses(request!!.iterate(this.activePokemon) { _, _, _ -> PassActionResponse })
        }
    }
}