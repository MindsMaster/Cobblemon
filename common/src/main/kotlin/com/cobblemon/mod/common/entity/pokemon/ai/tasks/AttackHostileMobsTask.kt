package com.cobblemon.mod.common.entity.ai

import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.Mob
import net.minecraft.world.entity.ai.behavior.OneShot
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder
import net.minecraft.world.entity.ai.behavior.declarative.Trigger
import net.minecraft.world.entity.ai.memory.MemoryModuleType
import net.minecraft.world.entity.monster.Enemy
import net.minecraft.world.entity.player.Player
import net.minecraft.world.phys.AABB

object AttackHostileMobsTask {
    fun create(): OneShot<LivingEntity> = BehaviorBuilder.create {
        it.group(
            it.absent(MemoryModuleType.ATTACK_TARGET),
            it.present(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES)
        ).apply(it) { attackTarget, nearestVisibleLiving ->
            Trigger { world, entity, _ ->
                val nearby = it.get(nearestVisibleLiving)
                val hostile = nearby.firstOrNull { entity ->
                    entity is Mob && entity !is Player && entity is Enemy && entity.isAlive
                }

                if (hostile != null) {
                    entity.brain.setMemory(MemoryModuleType.ATTACK_TARGET, hostile)
                    true
                } else false
            }
        }
    }
}
