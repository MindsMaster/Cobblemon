package com.cobblemon.mod.common.entity.pokemon.ai.sensors

import com.cobblemon.mod.common.CobblemonMemories
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.ai.sensing.Sensor
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.pathfinder.PathType
import kotlin.math.min

class FlowerSensor : Sensor<PokemonEntity>(300) {
    override fun requires() = setOf(CobblemonMemories.NEARBY_FLOWER)

    override fun doTick(world: ServerLevel, entity: PokemonEntity) {
        val brain = entity.brain
        val currentFlower = brain.getMemory(CobblemonMemories.NEARBY_FLOWER).orElse(null)

        if (currentFlower != null) {
            val state = world.getBlockState(currentFlower)
            if (isFlower(state) && isPathfindableTo(entity, currentFlower)) {
                return
            } else {
                brain.eraseMemory(CobblemonMemories.NEARBY_FLOWER)
            }
        }

        val searchRadius = 16
        val centerPos = entity.blockPosition()

        var closestFlower: BlockPos? = null
        var closestDistance = Double.MAX_VALUE

        BlockPos.betweenClosedStream(
            centerPos.offset(-searchRadius, -2, -searchRadius),
            centerPos.offset(searchRadius, 2, searchRadius)
        ).forEach { pos ->
            val state = world.getBlockState(pos)
            if (isFlower(state) && isPathfindableTo(entity, pos)) {
                val distance = pos.distToCenterSqr(centerPos.x + 0.5, centerPos.y + 0.5, centerPos.z + 0.5)
                if (distance < closestDistance) {
                    closestDistance = distance
                    closestFlower = pos.immutable()
                }
            }
        }

        if (closestFlower != null) {
            brain.setMemory(CobblemonMemories.NEARBY_FLOWER, closestFlower)
        }
    }

    private fun isFlower(state: net.minecraft.world.level.block.state.BlockState): Boolean {
        return state.`is`(net.minecraft.tags.BlockTags.FLOWERS)
    }

    private fun isPathfindableTo(entity: PokemonEntity, pos: BlockPos): Boolean {
        val nav = entity.navigation
        val path = nav.createPath(pos, 0)
        return path != null && path.canReach()
    }
}
