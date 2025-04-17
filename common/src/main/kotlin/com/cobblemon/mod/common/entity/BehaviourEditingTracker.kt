package com.cobblemon.mod.common.entity

import com.google.common.collect.HashBiMap
import java.util.UUID
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.LivingEntity

/**
 * Used to keep track of which players are editing which entities, mainly for retaining an understanding of
 * the player having permission.
 *
 * @author Hiroku
 * @since April 13th, 2025
 */
object BehaviourEditingTracker {
    @JvmStatic
    val playerToEditingEntity = HashBiMap.create<UUID, UUID>()

    fun isPlayerEditing(player: ServerPlayer, entity: LivingEntity): Boolean {
        return playerToEditingEntity[player.uuid] == entity.uuid
    }

    fun startEditing(player: ServerPlayer, entity: LivingEntity) {
        playerToEditingEntity[player.uuid] = entity.uuid
    }

    fun stopEditing(playerId: UUID) {
        playerToEditingEntity.remove(playerId)
    }

    fun getPlayerIdEditing(entity: LivingEntity): UUID? {
        return playerToEditingEntity.inverse()[entity.uuid]
    }
}