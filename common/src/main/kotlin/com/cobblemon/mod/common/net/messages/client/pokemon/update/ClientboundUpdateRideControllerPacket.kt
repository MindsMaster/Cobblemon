package com.cobblemon.mod.common.net.messages.client.pokemon.update

import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.api.riding.controller.RideController
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.network.RegistryFriendlyByteBuf

class ClientboundUpdateRideControllerPacket(val entity: Int, val controller: RideController? = null, val data: RegistryFriendlyByteBuf? = null) : NetworkPacket<ClientboundUpdateRideControllerPacket> {

    override val id = ID

    override fun encode(buffer: RegistryFriendlyByteBuf) {
        if (controller == null) error("Expected controller to be populated for encoding")
        buffer.writeInt(entity)
        controller.encode(buffer)
    }

    companion object {
        val ID = cobblemonResource("s2c_update_ride_controller")
        fun decode(buffer: RegistryFriendlyByteBuf) = ClientboundUpdateRideControllerPacket(
            entity = buffer.readInt(),
            data = buffer
        )
    }
}
