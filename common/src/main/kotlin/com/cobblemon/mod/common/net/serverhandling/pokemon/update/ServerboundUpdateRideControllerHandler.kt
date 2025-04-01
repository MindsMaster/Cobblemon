package com.cobblemon.mod.common.net.serverhandling.pokemon.update

import com.cobblemon.mod.common.CobblemonNetwork
import com.cobblemon.mod.common.api.net.ServerNetworkPacketHandler
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.net.messages.client.pokemon.update.ClientboundUpdateRideControllerPacket
import com.cobblemon.mod.common.net.messages.server.pokemon.update.ServerboundUpdateRideControllerPacket
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer

object ServerboundUpdateRideControllerHandler : ServerNetworkPacketHandler<ServerboundUpdateRideControllerPacket> {

    override fun handle(packet: ServerboundUpdateRideControllerPacket, server: MinecraftServer, player: ServerPlayer) {
//        val entity = player.level().getEntity(packet.entity) ?: return
//        if (entity !is PokemonEntity) return
//        if (entity.controllingPassenger != player) return
//        val buffer = packet.data ?: return
//        val controllerId = buffer.readResourceLocation()
//        if (entity.ridingController?.key != controllerId) return
//        entity.ridingController?.decode(buffer)
//        CobblemonNetwork.sendToAllPlayers(ClientboundUpdateRideControllerPacket(packet.entity, entity.ridingController))
    }

}
