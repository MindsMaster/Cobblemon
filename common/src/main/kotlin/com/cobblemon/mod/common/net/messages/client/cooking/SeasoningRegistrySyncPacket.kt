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
        buffer.writeUtf(entry.flavor)
        buffer.writeUtf(entry.color)
        buffer.writeInt(entry.quality)
    }

    override fun decodeEntry(buffer: RegistryFriendlyByteBuf): Seasoning {
        val ingredient = buffer.readIdentifier()
        val flavor = buffer.readUtf()
        val color = buffer.readUtf()
        val quality = buffer.readInt()
        return Seasoning(ingredient, flavor, color, quality)
    }

    override fun synchronizeDecoded(entries: Collection<Seasoning>) {
        Seasonings.reload(entries.associateBy { it.ingredient })
    }
}
