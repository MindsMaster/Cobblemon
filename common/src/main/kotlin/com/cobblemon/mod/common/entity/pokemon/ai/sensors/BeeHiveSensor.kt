package com.cobblemon.mod.common.entity.pokemon.ai.sensors

import com.cobblemon.mod.common.CobblemonMemories
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.PathfinderMob
import net.minecraft.world.entity.ai.memory.MemoryModuleType
import net.minecraft.world.entity.ai.sensing.Sensor
import net.minecraft.world.level.block.BeehiveBlock
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.pathfinder.PathType
import kotlin.math.min

class BeeHiveSensor : Sensor<PokemonEntity>(100) {
    override fun requires() = setOf(
            CobblemonMemories.HIVE_LOCATION
    )

    override fun doTick(world: ServerLevel, entity: PokemonEntity) {
        val brain = entity.brain
        val currentHive = brain.getMemory(CobblemonMemories.HIVE_LOCATION).orElse(null)

        if (entity.pokemon.species.name == "Vespiquen") {
            val test = 1
        }

        // todo have a better way to assign this sensor to BeeLike pokemon
        if (entity.pokemon.species.name != "Combee" && entity.pokemon.species.name != "Vespiquen") {
            return
        }

        if (currentHive != null) {
            val state = world.getBlockState(currentHive)
            if (isHiveBlock(state) && isPathfindableTo(entity, currentHive) && state.getValue(BeehiveBlock.HONEY_LEVEL) == BeehiveBlock.MAX_HONEY_LEVELS) {
                return
            } else {
                // we want to clear the memory if that nest is no longer accessible or gone
                brain.eraseMemory(CobblemonMemories.HIVE_LOCATION)
            }
        }

        // Search for nearest hive/nest
        val searchRadius = 32  // is this too big for us to use?
        val centerPos = entity.blockPosition()

        var closestHivePos: BlockPos? = null
        var closestDistance = Double.MAX_VALUE

        BlockPos.betweenClosedStream(
                centerPos.offset(-searchRadius, -2, -searchRadius),
                centerPos.offset(searchRadius, 2, searchRadius)
        ).forEach { pos ->
            val state = world.getBlockState(pos)
            if (isHiveBlock(state) && isPathfindableTo(entity, pos) && state.getValue(BeehiveBlock.HONEY_LEVEL) != BeehiveBlock.MAX_HONEY_LEVELS) {
                val distance = pos.distToCenterSqr(centerPos.x + 0.5, centerPos.y + 0.5, centerPos.z + 0.5)
                if (distance < closestDistance) {
                    closestDistance = distance
                    closestHivePos = pos.immutable()
                }
            }
        }

        if (closestHivePos != null) {
            brain.setMemory(CobblemonMemories.HIVE_LOCATION, closestHivePos)
        }
    }

    private fun isHiveBlock(state: net.minecraft.world.level.block.state.BlockState): Boolean {
        return state.`is`(Blocks.BEEHIVE) || state.`is`(Blocks.BEE_NEST)
    }

    private fun isPathfindableTo(entity: PokemonEntity, pos: BlockPos): Boolean {
        val nav = entity.navigation
        val path = nav.createPath(pos, 0)
        return path != null && path.canReach()
    }
}