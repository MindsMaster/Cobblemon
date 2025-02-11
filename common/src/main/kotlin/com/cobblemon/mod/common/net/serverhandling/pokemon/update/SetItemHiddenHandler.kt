/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.serverhandling.pokemon.update

import com.cobblemon.mod.common.api.net.ServerNetworkPacketHandler
import com.cobblemon.mod.common.api.storage.PokemonStore
import com.cobblemon.mod.common.net.messages.server.pokemon.update.SetItemHiddenPacket
import com.cobblemon.mod.common.util.party
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer

object SetItemHiddenHandler : ServerNetworkPacketHandler<SetItemHiddenPacket> {

    override fun handle(packet: SetItemHiddenPacket, server: MinecraftServer, player: ServerPlayer) {

        val pokemonStore: PokemonStore<*> = player.party()
        val pokemon = pokemonStore[packet.pokemonUUID] ?: return

        pokemon.isItemHidden = packet.isItemHidden

        // Send an update that visibility has changed
        // Used to fix the issue with client side not updating
//        CobblemonEvents.ITEM_VISIBILITY_CHANGED.postThen(
//            event = ItemVisibilityChangedEvent(
//                pokemon,
//                isItemHidden
//            ),
//            ifSucceeded = {
//                pokemon.isItemHidden = isItemHidden
//            },
//            ifCanceled = {
//                return player.sendPacket(ItemHiddenUpdatePacket({ pokemon }, isItemHidden))
//                //Use to undo update //return player.sendPacket(ItemHiddenUpdatePacket({ pokemon }, pokemon.isItemHidden))
//            }
//        )
    }
}