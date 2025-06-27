package com.cobblemon.mod.common.net.messages.server.riding

import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.network.RegistryFriendlyByteBuf

class DismountPokemonPacket : NetworkPacket<DismountPokemonPacket> {
    override val id = ID

    override fun encode(buffer: RegistryFriendlyByteBuf) {}

    companion object {
        val ID = cobblemonResource("dismount_pokemon")
        fun decode(buffer: RegistryFriendlyByteBuf) = DismountPokemonPacket()
    }
}