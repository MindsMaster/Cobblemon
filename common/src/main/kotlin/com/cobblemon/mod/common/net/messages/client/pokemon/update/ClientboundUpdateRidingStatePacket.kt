package com.cobblemon.mod.common.net.messages.client.pokemon.update

import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.api.riding.behaviour.RidingBehaviourState
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.network.RegistryFriendlyByteBuf

class ClientboundUpdateRidingStatePacket(val entity: Int, val state: RidingBehaviourState? = null, val data: RegistryFriendlyByteBuf? = null) : NetworkPacket<ClientboundUpdateRidingStatePacket> {

    override val id = ID

    override fun encode(buffer: RegistryFriendlyByteBuf) {
        if (state == null) error("Expected state to be populated for encoding")
        buffer.writeInt(entity)
        state.encode(buffer)
    }

    companion object {
        val ID = cobblemonResource("s2c_update_ride_controller")
        fun decode(buffer: RegistryFriendlyByteBuf) = ClientboundUpdateRidingStatePacket(
            entity = buffer.readInt(),
            data = buffer
        )
    }
}
