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
import net.minecraft.world.level.block.BeehiveBlock
import net.minecraft.world.phys.Vec3

object PlaceHoneyInHiveTask {
    fun create(): OneShot<PokemonEntity> {
        return BehaviorBuilder.create {
            it.group(
                    it.present(CobblemonMemories.HIVE_LOCATION),
                    it.registered(CobblemonMemories.HIVE_COOLDOWN)
            ).apply(it) { hiveMemory, hiveCooldown ->
                Trigger { world, entity, time ->
                    val hiveCooldown = 100L

                    // todo if cooldown is in affect then return early

                    if (entity !is PathfinderMob || !entity.isAlive) return@Trigger false

                    // todo have a better way to assign this task to BeeLike pokemon
                    if (entity.pokemon.species.name != "Combee" && entity.pokemon.species.name != "Vespiquen") {
                        return@Trigger false
                    }

                    val hiveLocation: BlockPos = (hiveMemory.value() as? IdF<BlockPos>)?.value() ?: return@Trigger false
                    val targetVec = Vec3.atCenterOf(hiveLocation)

                    if (entity.distanceToSqr(targetVec) > 2.0) {
                        return@Trigger false
                    }

                    val state = world.getBlockState(hiveLocation)
                    val block = state.block
                    if (block is BeehiveBlock) {
                        val currentLevel = state.getValue(BeehiveBlock.HONEY_LEVEL)
                        if (currentLevel < BeehiveBlock.MAX_HONEY_LEVELS) {
                            world.setBlock(hiveLocation, state.setValue(BeehiveBlock.HONEY_LEVEL, currentLevel + 1), 3)

                            // todo set cooldown memories to prevent repeated adding of honey in short periods
                            entity.brain.setMemory(CobblemonMemories.RECENTLY_ADDED_HONEY, true)
                            entity.brain.setMemoryWithExpiry(CobblemonMemories.HIVE_COOLDOWN, true, hiveCooldown)
                        }
                    }

                    return@Trigger true
                }
            }
        }
    }
}