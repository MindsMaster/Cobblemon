/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.net.orientation

import com.cobblemon.mod.common.OrientationControllable
import com.cobblemon.mod.common.api.net.ClientNetworkPacketHandler
import com.cobblemon.mod.common.net.messages.client.orientation.S2CUpdateOrientationPacket
import net.minecraft.client.Minecraft

object S2CUpdateOrientationHandler : ClientNetworkPacketHandler<S2CUpdateOrientationPacket> {
    override fun handle(packet: S2CUpdateOrientationPacket, client: Minecraft) {
        val level = client.level ?: return
        val entity = level.getEntity(packet.entityId)
        if (entity is OrientationControllable) {
            entity.orientationController.updateOrientation { _ -> packet.orientation }
            packet.active?.let {
                entity.orientationController.active = it
            }
        }
    }
}