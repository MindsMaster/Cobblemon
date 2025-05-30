/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.entity.pokemon.ai.tasks


import com.cobblemon.mod.common.CobblemonMemories
import com.cobblemon.mod.common.entity.PlatformType
import com.cobblemon.mod.common.entity.pokemon.PokemonBehaviourFlag
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.google.common.collect.ImmutableMap
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.ai.behavior.Behavior
import net.minecraft.world.entity.ai.memory.MemoryStatus

class BattleFlightTask : Behavior<PokemonEntity>(
    ImmutableMap.of(
            CobblemonMemories.POKEMON_BATTLE, MemoryStatus.VALUE_PRESENT,
    )
){
    override fun start(world: ServerLevel, entity: PokemonEntity, time: Long) {
        entity.navigation.stop()
    }

    override fun canStillUse(world: ServerLevel, entity: PokemonEntity, time: Long): Boolean {
        return entity.isBattling && entity.brain.checkMemory(CobblemonMemories.HAS_MOVED_TO_BATTLE_POSITION, MemoryStatus.VALUE_ABSENT)
    }


    override fun tick(world: ServerLevel, entity: PokemonEntity, time: Long) {
        if (entity.exposedForm.behaviour.moving.fly.canFly) {

            if (entity.ticksLived > 0 && !entity.getBehaviourFlag(PokemonBehaviourFlag.FLYING) && entity.navigation.isAirborne(entity.level(), entity.blockPosition())) {
                // Let flyers fly in battle if they're in the air
                entity.setBehaviourFlag(PokemonBehaviourFlag.FLYING, true)
            }

            if (entity.brain.checkMemory(CobblemonMemories.HAS_MOVED_TO_BATTLE_POSITION, MemoryStatus.VALUE_ABSENT) && entity.ticksLived > 5) {
                entity.navigation.moveTo(entity.x, entity.y + 0.3, entity.z, 1.0)
                entity.brain.setMemory(CobblemonMemories.HAS_MOVED_TO_BATTLE_POSITION, true)
            }
        }
    }

}