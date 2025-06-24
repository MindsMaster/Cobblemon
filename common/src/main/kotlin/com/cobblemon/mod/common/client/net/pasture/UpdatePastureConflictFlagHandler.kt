package com.cobblemon.mod.common.client.net.pasture

import com.cobblemon.mod.common.api.net.ClientNetworkPacketHandler
import com.cobblemon.mod.common.client.gui.pasture.PasturePCGUIConfiguration
import com.cobblemon.mod.common.client.gui.pasture.PasturePokemonScrollList
import com.cobblemon.mod.common.client.gui.pc.PCGUI
import com.cobblemon.mod.common.net.messages.client.pasture.UpdatePastureConflictFlagPacket
import net.minecraft.client.Minecraft

object UpdatePastureConflictFlagHandler : ClientNetworkPacketHandler<UpdatePastureConflictFlagPacket> {
    override fun handle(packet: UpdatePastureConflictFlagPacket, client: Minecraft) {
        val screen = client.screen as? PCGUI ?: return
        val scrollList = screen.storage.pastureWidget?.pastureScrollList ?: return

        for (slot in scrollList.children()) {
            if (slot.pokemon.pokemonId == packet.pokemonId) {
                // Update behavior flags
                slot.pokemon.behaviourFlags = if (packet.enabled) {
                    slot.pokemon.behaviourFlags + "cobblemon:pasture_conflict"
                } else {
                    slot.pokemon.behaviourFlags - "cobblemon:pasture_conflict"
                }

                slot.conflictButton.setEnabled(packet.enabled)
                break
            }
        }
    }
}