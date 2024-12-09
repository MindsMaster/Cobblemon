/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.util

import com.cobblemon.mod.common.api.text.yellow
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.google.common.collect.ImmutableMap
import com.mojang.serialization.Dynamic
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.nbt.NbtOps
import net.minecraft.nbt.Tag
import net.minecraft.network.syncher.EntityDataAccessor
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import net.minecraft.world.phys.shapes.BooleanOp
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.Shapes
import java.util.*
import kotlin.math.ceil
import kotlin.math.min
import kotlin.time.measureTime


fun Entity.makeEmptyBrainDynamic() = Dynamic(
    NbtOps.INSTANCE,
    NbtOps.INSTANCE.createMap(ImmutableMap.of(NbtOps.INSTANCE.createString("memories"), NbtOps.INSTANCE.emptyMap() as Tag)) as Tag
)

fun Entity.effectiveName() = this.displayName ?: this.name

fun Entity.setPositionSafely(pos: Vec3): Boolean {
    // TODO: Rework this function. Best way to do it would be to define a vertical and horizontal min/max shift based on the Pokemon's hitbox.
    // Then loop through horizontal/vertical shifts, and detect collisions in three categories: suffocation, damaging blocks, and general collision
    // The closest position with the least severe collision types will be selected to move the Pokemon to
    // The throw could be cancelled if there are no viable locations without severe problems

    // Optional: use getBlockCollisions iterator and VoxelShapes.combineAndSimplify to create a single cube to represent collision area
    // Use that cube to "push" the Pokemon out of the wall at an angle
    // Note: may not work well with L shape wall collisions
//    val collisions = world.getBlockCollisions(this, box).iterator()
//    if (collisions.hasNext()) {
//        var collisionShape = collisions.next()
//        while (collisions.hasNext()) {
//            collisionShape = VoxelShapes.union(collisionShape, collisions.next())
//            println(collisionShape)
//        }
//    } else {
//        setPosition(pos)
//        return true
//    }


    fun isPositionValid(level: Level, entity: Entity, pos: BlockPos): Boolean {
        if(!level.getBlockState(pos).getCollisionShape(level, pos, CollisionContext.empty()).isEmpty) {
            // do not consider positions where the base pos has collision
            // This helps in ruling out positions in which the pokemon would spawn in the ground
            // Also avoids a great deal of unnecessary AABB checks
            return false
        }
        val boundingBox = entity.boundingBox.move(Vec3(pos.x + 0.5, pos.y.toDouble(), pos.z + 0.5).subtract(entity.boundingBox.bottomCenter))
        return level.noBlockCollision(entity, boundingBox)
    }

    val directions = listOf(
            BlockPos(0, 1, 0), BlockPos(0, -1, 0), // Up and down
            BlockPos(1, 0, 0), BlockPos(-1, 0, 0), // East and west
            BlockPos(0, 0, 1), BlockPos(0, 0, -1)  // North and south
    )


    fun findPerfectPositionBFS(
            entity: Entity,
            pos: Vec3,
            level: Level,
            maxRadius: Int = min(ceil(entity.bbWidth * 1.5).toInt(), 7)
    ): BlockPos? {
        val queue = ArrayDeque<BlockPos>()
        val visited = mutableSetOf<BlockPos>()
        val centerPos = BlockPos(pos.x.toInt(), pos.y.toInt(), pos.z.toInt())
        queue.add(centerPos)

        while (queue.isNotEmpty()) {
            val current = queue.removeFirst()
            if (current in visited) continue
            visited.add(current)

            if (isPositionValid(level, entity, current)) {
                return current
            }

            // Add neighbors (up to maxRadius)
            for (dir in directions) {
                val neighbor = current.offset(dir)
                if (neighbor.distManhattan(centerPos) <= maxRadius) {
                    queue.add(neighbor)
                }
            }
        }
        return null
    }

    fun getPosScore(level: Level, entity: Entity, pos: BlockPos, minScore : Int = Int.MAX_VALUE): Int {
        var score = 0
        if(!level.getBlockState(pos).getCollisionShape(level, pos, CollisionContext.empty()).isEmpty) {
            // do not consider positions where the base pos has collision
            // This helps in ruling out positions in which the pokemon would spawn in the ground
            // Also avoids a great deal of unnecessary AABB checks
            return Int.MAX_VALUE
        }
        val boundingBox = entity.boundingBox.move(Vec3(pos.x + 0.5, pos.y.toDouble(), pos.z + 0.5).subtract(entity.boundingBox.bottomCenter))

        if(entity.bbWidth > 3 && !level.noBlockCollision(entity, boundingBox.contract(0.0, (bbHeight - 1).toDouble(), 0.0))) {
            score += (1 * bbWidth * bbWidth).toInt()
        }
        return if (score < minScore) {
            score + level.getBlockCollisions(entity, boundingBox).count()
        } else {
            score
        }
    }

//    fun canSeeOwner(entity: Entity, pos: Vec3) : Boolean {
//        if (entity is PokemonEntity) {
//           val actor = entity.owner
//            if(actor is ServerPlayer) {
//
//                val result = actor.traceBlockCollision(
//                        maxDistance = actor.position().distanceTo(pos).toFloat(),
//                        direction = pos.subtract(actor.position()).normalize(),
//                ) == null
//                return result
//            }
//        }
//        return false
//    }

    fun findBestBlockPosBFS(
            entity: Entity,
            pos: Vec3,
            level: Level,
            maxRadius: Int = min(ceil(entity.bbWidth * 1.5).toInt(), 8)
    ): BlockPos {
        val queue = ArrayDeque<BlockPos>()
        val visited = mutableSetOf<BlockPos>()
        val centerPos = BlockPos(pos.x.toInt(), pos.y.toInt(), pos.z.toInt())
        queue.add(centerPos)
        var bestScore = Int.MAX_VALUE
        var bestPos = centerPos

        while (queue.isNotEmpty()) {
            val current = queue.removeFirst()
            if (current in visited) continue
            visited.add(current)

            val score = getPosScore(level, entity, current, bestScore)
            if (score < 1) {
                return current
            } else if (bestScore > score) {
                bestPos = current
                bestScore = score
            }

            // Add neighbors (up to maxRadius)
            for (dir in directions) {
                val neighbor = current.offset(dir)
                if (neighbor.distManhattan(centerPos) <= maxRadius) {
                    queue.add(neighbor)
                }
            }
        }
        return bestPos
    }



    val box = boundingBox.move(pos.subtract(boundingBox.bottomCenter))


    if (level().noBlockCollision(this, box)) {
        // Given position is valid so no need to do extra work
        setPos(pos)
        return true
    }
//    var elapsedTime = measureTime {
//        perfectPosition = findPerfectPositionBFS(this, pos, level())
//    }
//    println(elapsedTime)
//
//    if (perfectPosition != null) {
//       setPos(Vec3(perfectPosition!!.x + 0.5, perfectPosition!!.y.toDouble(), perfectPosition!!.z + 0.5))
//        return true
//    }

    var bestPosition: BlockPos
    var elapsedTime = measureTime {
        bestPosition = findBestBlockPosBFS(this, pos, level())
        setPos(Vec3(bestPosition.x + 0.5, bestPosition.y.toDouble(), bestPosition.z + 0.5))
    }
    println(elapsedTime)
    server()?.playerList?.players?.forEach {
        it.sendSystemMessage("Send out for ${(this as PokemonEntity).pokemon.species.name} completed in $elapsedTime".yellow())
    }

    val result = Vec3(bestPosition.x.toDouble(), bestPosition.y.toDouble(), bestPosition.z.toDouble())

    // This final check guarantees that the sendout will return to the original position if the Pokemon will suffocate in the new one
    // This will only happen if the horizontal shift moved the Pokemon into a suffocating position, and there was no valid vertical shift
    val resultEyes = result.with(Direction.Axis.Y, result.y + this.eyeHeight)
    val resultEyeBox = AABB.ofSize(resultEyes, bbWidth.toDouble(), 1.0E-6, bbWidth.toDouble())
    var collides = false

    for (target in BlockPos.betweenClosedStream(resultEyeBox)) {
        val blockState = this.level().getBlockState(target)
        collides = !blockState.isAir &&
                blockState.isSuffocating(this.level(), target) &&
                Shapes.joinIsNotEmpty(
                    blockState.getCollisionShape(this.level(), target)
                        .move(target.x.toDouble(), target.y.toDouble(), target.z.toDouble()),
                    Shapes.create(resultEyeBox),
                    BooleanOp.AND
                )
        if (collides) break
    }
    if (collides) {
//        this.setPos(pos)
        return false
    } else {
//        this.setPos(result)
        return true
    }
}

fun Entity.isStandingOnSandOrRedSand(): Boolean {
    val sandDepth = 2 // Define the depth you want to check
    for (a in 1..sandDepth) {
        val sandBlockState = this.level().getBlockState(blockPosition().below(a))
        val sandBlock = sandBlockState.block
        if (sandBlock == Blocks.SAND && !sandBlockState.isAir && sandBlockState.isCollisionShapeFullBlock(this.level(), blockPosition().below(a))) {
            return true
        }
        if (sandBlock == Blocks.RED_SAND && !sandBlockState.isAir && sandBlockState.isCollisionShapeFullBlock(this.level(), blockPosition().below(a))) {
            return true
        }
    }
    return false
}

fun Entity.isDusk(): Boolean {
    val time = level().dayTime % 24000
    return time in 12000..13000
}

fun Entity.isStandingOnSand(): Boolean {
    val sandDepth = 2 // Define the depth you want to check
    for (a in 1..sandDepth) {
        val sandBlockState = this.level().getBlockState(blockPosition().below(a))
        val sandBlock = sandBlockState.block
        if (sandBlock == Blocks.SAND && !sandBlockState.isAir && sandBlockState.isCollisionShapeFullBlock(this.level(), blockPosition().below(a))) {
            return true
        }
    }
    return false
}

fun Entity.isStandingOnRedSand(): Boolean {
    val redSandDepth = 2 // Define the depth you want to check
    for (i in 1..redSandDepth) {
        val redSandBlockState = this.level().getBlockState(blockPosition().below(i))
        val redSandBlock = redSandBlockState.block
        if (redSandBlock == Blocks.RED_SAND && !redSandBlockState.isAir && redSandBlockState.isCollisionShapeFullBlock(this.level(), blockPosition().below(i))) {
            return true
        }
    }
    return false
}

fun Entity.distanceTo(pos: BlockPos): Double {
    val difference = pos.toVec3d().subtract(this.position())
    return difference.length()
}

fun Entity.closestPosition(positions: Iterable<BlockPos>, filter: (BlockPos) -> Boolean = { true }): BlockPos? {
    var closest: BlockPos? = null
    var closestDistance = Double.MAX_VALUE

    val iterator = positions.iterator()
    while (iterator.hasNext()) {
        val position = iterator.next()
        if (filter(position)) {
            val distance = distanceTo(position)
            if (distance < closestDistance) {
                closest = BlockPos(position)
                closestDistance = distance
            }
        }
    }

    return closest
}

fun Entity.getIsSubmerged() = isInLava || isUnderWater

fun <T> SynchedEntityData.update(data: EntityDataAccessor<T>, mutator: (T) -> T) {
    val value = get(data)
    val newValue = mutator(value)
    if (value != newValue) {
        set(data, newValue)
    }
}