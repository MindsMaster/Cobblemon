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
            it.registered(MemoryModuleType.ATTACK_TARGET),
            it.present(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES)
        ).apply(it) { attackTarget, nearestVisibleLiving ->
            Trigger { world, entity, _ ->
                val currentTarget = entity.brain.getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null)

                if (currentTarget != null && currentTarget.isAlive) {
                    // Valid target already present
                    return@Trigger false
                }

                // Target is invalid or gone let's find a new one
                entity.brain.eraseMemory(MemoryModuleType.ATTACK_TARGET)

                val nearby = (entity.brain.getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).orElse(null)
                    ?: return@Trigger false).findAll { true}

                // Filter for hostile, non-player, living mobs. todo maybe we want to add Pokemon too?
                val hostile = nearby.firstOrNull { entity ->
                    entity is Mob && entity !is Player && entity is Enemy && entity.isAlive
                }

                if (hostile != null) {
                    entity.brain.setMemory(MemoryModuleType.ATTACK_TARGET, hostile)
                    true
                } else {
                    false
                }
            }
        }
    }
}
