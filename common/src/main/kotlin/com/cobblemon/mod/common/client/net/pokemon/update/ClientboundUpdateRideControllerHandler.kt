package com.cobblemon.mod.common.client.net.pokemon.update

import com.cobblemon.mod.common.api.net.ClientNetworkPacketHandler
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.net.messages.client.pokemon.update.ClientboundUpdateRideControllerPacket
import net.minecraft.client.Minecraft

object ClientboundUpdateRideControllerHandler : ClientNetworkPacketHandler<ClientboundUpdateRideControllerPacket> {
    override fun handle(packet: ClientboundUpdateRideControllerPacket, client: Minecraft) {
        val level = client.level ?: return
        val player = client.player ?: return
        val entity = level.getEntity(packet.entity) ?: return
        if (entity !is PokemonEntity) return
        if (entity.controllingPassenger == player) return
        val buffer = packet.data ?: return
        val controllerId = buffer.readResourceLocation()
        if (entity.ridingController?.key != controllerId) return
        entity.ridingController?.decode(buffer)
    }
}
