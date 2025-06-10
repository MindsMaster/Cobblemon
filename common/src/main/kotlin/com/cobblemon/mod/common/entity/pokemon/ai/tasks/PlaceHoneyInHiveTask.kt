package com.cobblemon.mod.common.entity.pokemon.ai.tasks

import com.cobblemon.mod.common.CobblemonMemories
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.mojang.datafixers.kinds.IdF
import net.minecraft.core.BlockPos
import net.minecraft.world.entity.PathfinderMob
import net.minecraft.world.entity.ai.behavior.BehaviorControl
import net.minecraft.world.entity.ai.behavior.OneShot
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder
import net.minecraft.world.entity.ai.behavior.declarative.Trigger
import net.minecraft.world.entity.ai.memory.MemoryModuleType
import net.minecraft.world.level.block.BeehiveBlock
import net.minecraft.world.phys.Vec3

object PlaceHoneyInHiveTask {
    fun create(): OneShot<PokemonEntity> {
        return BehaviorBuilder.create {
            it.group(
                it.present(MemoryModuleType.LOOK_TARGET),
                it.absent(MemoryModuleType.WALK_TARGET),
                it.present(CobblemonMemories.POLLINATED),
                it.present(CobblemonMemories.HIVE_LOCATION),
                it.absent(CobblemonMemories.HIVE_COOLDOWN)
            ).apply(it) { lookTarget, walkTarget, pollinated, hiveMemory, hiveCooldown ->
                Trigger { world, entity, time ->
                    if (entity !is PathfinderMob || !entity.isAlive) return@Trigger false

                    val hiveCooldown = 1200L

                    // if on hive cooldown then end early
                    if (entity.brain.getMemory(CobblemonMemories.HIVE_COOLDOWN).orElse(false) == true) {
                        return@Trigger false
                    }

                    // if not pollinated then end early
                    if (entity.brain.getMemory(CobblemonMemories.POLLINATED).orElse(false) != true) {
                        return@Trigger false
                    }

                    val hiveLocation: BlockPos = (hiveMemory.value() as? IdF<BlockPos>)?.value() ?: return@Trigger false
                    val targetVec = Vec3.atCenterOf(hiveLocation)

                    // if we are not close to it then end early
                    if (entity.distanceToSqr(targetVec) > 2.0) {
                        return@Trigger false
                    }

                    val state = world.getBlockState(hiveLocation)
                    val block = state.block
                    if (block is BeehiveBlock) {
                        val currentLevel = state.getValue(BeehiveBlock.HONEY_LEVEL)
                        if (currentLevel < BeehiveBlock.MAX_HONEY_LEVELS) {
                            world.setBlock(hiveLocation, state.setValue(BeehiveBlock.HONEY_LEVEL, currentLevel + 1), 3)

                            entity.brain.setMemory(CobblemonMemories.RECENTLY_ADDED_HONEY, true)
                            entity.brain.setMemoryWithExpiry(CobblemonMemories.HIVE_COOLDOWN, true, hiveCooldown)
                            entity.brain.eraseMemory(CobblemonMemories.POLLINATED)
                        }
                    } else if (block is com.cobblemon.mod.common.block.SaccharineLeafBlock) {
                        val currentAge = state.getValue(com.cobblemon.mod.common.block.SaccharineLeafBlock.AGE)
                        if (currentAge < com.cobblemon.mod.common.block.SaccharineLeafBlock.MAX_AGE) {
                            world.setBlock(hiveLocation, state.setValue(com.cobblemon.mod.common.block.SaccharineLeafBlock.AGE, currentAge + 1), 3)

                            entity.brain.setMemory(CobblemonMemories.RECENTLY_ADDED_HONEY, true)
                            entity.brain.setMemoryWithExpiry(CobblemonMemories.HIVE_COOLDOWN, true, hiveCooldown)
                            entity.brain.eraseMemory(CobblemonMemories.POLLINATED)
                        }
                    }

                    return@Trigger true
                }
            }
        }
    }
}