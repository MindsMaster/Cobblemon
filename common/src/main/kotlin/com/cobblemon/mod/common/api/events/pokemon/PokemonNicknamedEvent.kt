/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.events.pokemon

import com.bedrockk.molang.runtime.value.DoubleValue
import com.bedrockk.molang.runtime.value.MoValue
import com.bedrockk.molang.runtime.value.StringValue
import com.cobblemon.mod.common.api.molang.MoLangFunctions.asMoLangValue
import com.cobblemon.mod.common.api.events.Cancelable
import com.cobblemon.mod.common.api.molang.MoLangFunctions.moLangFunctionMap
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.util.getPlayer
import net.minecraft.network.chat.MutableComponent
import net.minecraft.server.level.ServerPlayer

/**
 * Event fired when a player attempts to nickname a Pokémon. The nickname that will be applied can be edited, or
 * the event itself can be cancelled to prevent the nickname from changing.
 *
 * If [nickname] is null, it means they're trying to remove the nickname.
 *
 * @author Hiroku
 * @since April 22nd, 2023
 */
class PokemonNicknamedEvent(val player: ServerPlayer, val pokemon: Pokemon, var nickname: MutableComponent?): Cancelable() {
    /** A shortcut to using [nickname].getString(). Learn how Text works! */
    val nicknameString: String?
        get() = nickname?.string

    fun getContext(): MutableMap<String, MoValue> {
        return mutableMapOf(
            "player" to (player.asMoLangValue() ?: DoubleValue.ZERO),
            "nickname" to StringValue(nicknameString),
            "pokemon" to pokemon.struct
        )
    }
    val functions = moLangFunctionMap(
        cancelFunc
    )
}