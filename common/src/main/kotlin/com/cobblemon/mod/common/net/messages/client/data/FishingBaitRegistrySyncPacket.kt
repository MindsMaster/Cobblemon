/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.client.data

import com.cobblemon.mod.common.api.fishing.SpawnBait
import com.cobblemon.mod.common.api.fishing.FishingBaits
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.network.RegistryFriendlyByteBuf

class FishingBaitRegistrySyncPacket(spawnBaits: List<SpawnBait>) : DataRegistrySyncPacket<SpawnBait, FishingBaitRegistrySyncPacket>(spawnBaits) {
    companion object {
        val ID = cobblemonResource("fishing_baits")
        fun decode(buffer: RegistryFriendlyByteBuf) = FishingBaitRegistrySyncPacket(emptyList()).apply { decodeBuffer(buffer) }
    }

    override val id = ID
    override fun encodeEntry(buffer: RegistryFriendlyByteBuf, entry: SpawnBait) {
        SpawnBait.STREAM_CODEC.encode(buffer, entry)
    }

    override fun decodeEntry(buffer: RegistryFriendlyByteBuf): SpawnBait {
        return SpawnBait.STREAM_CODEC.decode(buffer)
    }

    override fun synchronizeDecoded(entries: Collection<SpawnBait>) {
        FishingBaits.reload(entries.associateBy { it.item })
    }
}