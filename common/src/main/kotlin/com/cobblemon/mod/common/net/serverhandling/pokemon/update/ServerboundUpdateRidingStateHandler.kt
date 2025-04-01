package com.cobblemon.mod.common.net.serverhandling.pokemon.update

import com.cobblemon.mod.common.api.net.ServerNetworkPacketHandler
import com.cobblemon.mod.common.net.messages.server.pokemon.update.ServerboundUpdateRidingStatePacket
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer

object ServerboundUpdateRidingStateHandler : ServerNetworkPacketHandler<ServerboundUpdateRidingStatePacket> {

    override fun handle(packet: ServerboundUpdateRidingStatePacket, server: MinecraftServer, player: ServerPlayer) {
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
