package com.cobblemon.mod.common.net.messages.server.pasture

import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.readUUID
import com.cobblemon.mod.common.util.writeUUID
import net.minecraft.network.RegistryFriendlyByteBuf
import java.util.UUID

class SetPastureConflictPacket(val pokemonId: UUID, val enabled: Boolean) : NetworkPacket<SetPastureConflictPacket> {
    companion object {
        val ID = cobblemonResource("set_pasture_conflict")
        fun decode(buf: RegistryFriendlyByteBuf) =
            SetPastureConflictPacket(buf.readUUID(), buf.readBoolean())
    }

    override val id = ID

    override fun encode(buf: RegistryFriendlyByteBuf) {
        buf.writeUUID(pokemonId)
        buf.writeBoolean(enabled)
    }
}
