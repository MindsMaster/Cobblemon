/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.net.pokemon.update

import com.cobblemon.mod.common.api.net.ClientNetworkPacketHandler
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.net.messages.client.pokemon.update.ClientboundUpdateRidingStatePacket
import net.minecraft.client.Minecraft

object ClientboundUpdateRidingStateHandler : ClientNetworkPacketHandler<ClientboundUpdateRidingStatePacket> {
    override fun handle(packet: ClientboundUpdateRidingStatePacket, client: Minecraft) {
        client.executeIfPossible {
            val player = client.player ?: return@executeIfPossible
            val entity = player.level().getEntity(packet.entity) ?: return@executeIfPossible
            if (entity !is PokemonEntity) return@executeIfPossible
            if (entity.controllingPassenger == player) return@executeIfPossible
            val buffer = packet.data ?: return@executeIfPossible
            if (entity.riding?.key != packet.behaviour) return@executeIfPossible
            entity.ridingState?.decode(buffer)
        }
    }
}
