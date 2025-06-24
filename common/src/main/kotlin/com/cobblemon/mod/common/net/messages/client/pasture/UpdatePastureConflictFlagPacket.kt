package com.cobblemon.mod.common.net.messages.client.pasture

import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.resources.ResourceLocation
import java.util.*

data class UpdatePastureConflictFlagPacket(
    val pokemonId: UUID,
    val enabled: Boolean
) : NetworkPacket<UpdatePastureConflictFlagPacket> {
    override val id = ID

    companion object {
        val ID = cobblemonResource("update_pasture_conflict_flag")

        fun decode(buf: RegistryFriendlyByteBuf): UpdatePastureConflictFlagPacket {
            return UpdatePastureConflictFlagPacket(buf.readUUID(), buf.readBoolean())
        }
    }

    override fun encode(buf: RegistryFriendlyByteBuf) {
        buf.writeUUID(pokemonId)
        buf.writeBoolean(enabled)
    }


}