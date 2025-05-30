package com.cobblemon.mod.common.entity.pokemon.ai.tasks

import com.cobblemon.mod.common.CobblemonMemories
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.mojang.datafixers.kinds.IdF
import net.minecraft.core.BlockPos
import net.minecraft.world.entity.PathfinderMob
import net.minecraft.world.entity.ai.behavior.BehaviorControl
import net.minecraft.world.entity.ai.behavior.BlockPosTracker
import net.minecraft.world.entity.ai.behavior.OneShot
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder
import net.minecraft.world.entity.ai.behavior.declarative.Trigger
import net.minecraft.world.entity.ai.memory.MemoryModuleType
import net.minecraft.world.entity.ai.memory.WalkTarget
import net.minecraft.world.phys.Vec3

object PathToBeeHiveTask {
    fun create(): OneShot<PokemonEntity> {
        return BehaviorBuilder.create {
            it.group(
                    it.present(MemoryModuleType.LOOK_TARGET),
                    it.absent(MemoryModuleType.WALK_TARGET),
                    it.present(CobblemonMemories.HIVE_LOCATION),
                    it.registered(CobblemonMemories.HIVE_COOLDOWN)
            ).apply(it) { lookTarget, walkTarget, hiveMemory, hiveCooldown ->
                Trigger { world, entity, time ->
                    if (entity !is PathfinderMob || !entity.isAlive) return@Trigger false

                    // todo if cooldown is in affect then return early

                    // todo have a better way to assign this task to BeeLike pokemon
                    if (entity.pokemon.species.name != "Combee" && entity.pokemon.species.name != "Vespiquen") {
                        return@Trigger false
                    }

                    val hiveLocation: BlockPos = (hiveMemory.value() as? IdF<BlockPos>)?.value() ?: return@Trigger false
                    val targetVec = Vec3.atCenterOf(hiveLocation)

                    if (entity.distanceToSqr(targetVec) <= 2.0) {
                        return@Trigger false
                    }

                    // Set path target toward hive
                    walkTarget.set(WalkTarget(targetVec, 1.0F, 1))
                    lookTarget.set(BlockPosTracker(targetVec.add(0.0, entity.eyeHeight.toDouble(), 0.0)))

                    return@Trigger true
                }
            }
        }
    }
}