package com.cobblemon.mod.common.net.serverhandling.riding

import com.cobblemon.mod.common.api.net.ServerNetworkPacketHandler
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.net.messages.server.riding.DismountPokemonPacket
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer

object DismountPokemonPacketHandler : ServerNetworkPacketHandler<DismountPokemonPacket> {

    override fun handle(
        packet: DismountPokemonPacket,
        server: MinecraftServer,
        player: ServerPlayer
    ) {
        if (!(player.isPassenger && player.vehicle is PokemonEntity)) return

        val pokemon = player.vehicle as PokemonEntity
        if (!canPlayerStopRidingPokemon(pokemon, player)) return
        if (pokemon.controllingPassenger == player) {
            pokemon.passengers.forEach { it.stopRiding() }
        }
        else {
            player.stopRiding()
        }
    }

    private fun canPlayerStopRidingPokemon(pokemon: PokemonEntity, player: ServerPlayer): Boolean {
        return pokemon.ifRidingAvailableSupply(false) { behaviour, settings, state ->
            behaviour.canStopRiding(settings, state, pokemon, player)
        }
    }

}