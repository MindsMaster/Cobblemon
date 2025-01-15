/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.client.cooking

import com.cobblemon.mod.common.api.cooking.Seasoning
import com.cobblemon.mod.common.api.cooking.Seasonings
import com.cobblemon.mod.common.net.messages.client.data.DataRegistrySyncPacket
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.readIdentifier
import com.cobblemon.mod.common.util.writeIdentifier
import net.minecraft.network.RegistryFriendlyByteBuf

class SeasoningRegistrySyncPacket(seasonings: List<Seasoning>) :
    DataRegistrySyncPacket<Seasoning, SeasoningRegistrySyncPacket>(seasonings) {

    companion object {
        val ID = cobblemonResource("seasonings")
        fun decode(buffer: RegistryFriendlyByteBuf) =
            SeasoningRegistrySyncPacket(emptyList()).apply { decodeBuffer(buffer) }
    }

    override val id = ID

    override fun encodeEntry(buffer: RegistryFriendlyByteBuf, entry: Seasoning) {
        buffer.writeIdentifier(entry.ingredient)

        // Write the flavors map
        buffer.writeInt(entry.flavors.size) // Write the size of the map
        entry.flavors.forEach { (key, value) ->
            buffer.writeUtf(key) // Write each flavor key as a UTF string
            buffer.writeInt(value) // Write each flavor value as an integer
        }

        buffer.writeUtf(entry.color)
        buffer.writeInt(entry.quality)
    }

    override fun decodeEntry(buffer: RegistryFriendlyByteBuf): Seasoning {
        val ingredient = buffer.readIdentifier()

        // Read the flavors map
        val flavorsSize = buffer.readInt()
        val flavors = mutableMapOf<String, Int>()
        repeat(flavorsSize) {
            val key = buffer.readUtf() // Read each flavor key
            val value = buffer.readInt() // Read each flavor value
            flavors[key] = value
        }

        val color = buffer.readUtf()
        val quality = buffer.readInt()

        return Seasoning(ingredient, flavors, color, quality)
    }

    override fun synchronizeDecoded(entries: Collection<Seasoning>) {
        Seasonings.reload(entries.associateBy { it.ingredient })
    }
}
