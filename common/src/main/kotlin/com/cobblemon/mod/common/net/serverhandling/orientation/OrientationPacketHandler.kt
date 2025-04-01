/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.serverhandling.orientation

import com.cobblemon.mod.common.OrientationControllable
import com.cobblemon.mod.common.api.net.ServerNetworkPacketHandler
import com.cobblemon.mod.common.net.messages.server.orientation.C2SUpdateOrientationPacket
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import kotlin.math.atan2

object OrientationPacketHandler : ServerNetworkPacketHandler<C2SUpdateOrientationPacket> {
    override fun handle(packet: C2SUpdateOrientationPacket, server: MinecraftServer, player: ServerPlayer) {
        if (player is OrientationControllable) {
            player.orientationController.updateOrientation { _ -> packet.orientation }

            println("Packet Received! Roll ${"%.2f".format(player.orientationController.roll)} degrees")

//            val orientation = packet.orientation
//            val roll = orientation?.let { atan2(it.m10, it.m00) } ?: 0f
//            val rollDegrees = Math.toDegrees(roll.toDouble())
//            println("Packet Received! Roll: ${"%.2f".format(rollDegrees)} degrees")

        }
    }
}
