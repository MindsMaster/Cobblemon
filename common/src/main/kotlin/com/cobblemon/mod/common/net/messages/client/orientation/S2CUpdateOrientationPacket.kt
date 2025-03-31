/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.client.orientation

import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.readMatrix3f
import com.cobblemon.mod.common.util.writeMatrix3f
import net.minecraft.network.RegistryFriendlyByteBuf
import org.joml.Matrix3f

/**
 * Packet sent from the server to update the current orientation of the
 * given entity for the remote entity receiving this packet.
 *
 * @author Jackowes
 * @since March 30th, 2025
 */

class S2CUpdateOrientationPacket internal constructor(val orientation: Matrix3f?, val entityId: Int) : NetworkPacket<S2CUpdateOrientationPacket> {
    override val id = ID
    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeVarInt(entityId)
        buffer.writeBoolean(orientation != null)
        if (orientation != null) {
            buffer.writeMatrix3f(orientation)
        }
    }

    companion object {
        val ID = cobblemonResource("s2c_update_orientation")
        fun decode(buffer: RegistryFriendlyByteBuf): S2CUpdateOrientationPacket {
            val entityId = buffer.readVarInt()
            val orientation = if (buffer.readBoolean()) buffer.readMatrix3f() else null
            return S2CUpdateOrientationPacket(orientation, entityId)
        }
    }
}
