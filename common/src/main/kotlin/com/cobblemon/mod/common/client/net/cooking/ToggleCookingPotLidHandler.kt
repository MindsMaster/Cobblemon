package com.cobblemon.mod.common.client.net.cooking

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.net.ServerNetworkPacketHandler
import com.cobblemon.mod.common.block.entity.CampfireBlockEntity.Companion.IS_LID_OPEN_INDEX
import com.cobblemon.mod.common.client.gui.cookingpot.CookingPotMenu
import com.cobblemon.mod.common.net.messages.client.cooking.ToggleCookingPotLidPacket
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer

object ToggleCookingPotLidHandler : ServerNetworkPacketHandler<ToggleCookingPotLidPacket> {
    override fun handle(
        packet: ToggleCookingPotLidPacket,
        server: MinecraftServer,
        player: ServerPlayer
    ) {
        if (player.containerMenu !is CookingPotMenu) {
            Cobblemon.LOGGER.debug("Player {} interacted with invalid menu {}", player, player.containerMenu);
            return
        }

        val menu = player.containerMenu as? CookingPotMenu ?: return
        val isLidOpen = if (packet.value) 1 else 0
        menu.containerData.set(IS_LID_OPEN_INDEX, isLidOpen)
    }
}