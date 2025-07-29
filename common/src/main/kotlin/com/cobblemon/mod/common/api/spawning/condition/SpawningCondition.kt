/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.spawning.condition

import com.cobblemon.mod.common.api.conditional.RegistryLikeCondition
import com.cobblemon.mod.common.api.spawning.MoonPhaseRange
import com.cobblemon.mod.common.api.spawning.TimeRange
import com.cobblemon.mod.common.api.spawning.position.SpawnablePosition
import com.cobblemon.mod.common.util.Merger
import com.cobblemon.mod.common.util.math.orMax
import com.cobblemon.mod.common.util.math.orMin
import com.mojang.datafixers.util.Either
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import net.minecraft.world.level.biome.Biome
import net.minecraft.world.level.levelgen.WorldgenRandom
import net.minecraft.world.level.levelgen.structure.Structure

/**
 * The root of spawning conditions that can be applied to a spawnable position. What type
 * of spawnable position it can be applied to is relevant for any subclasses.
 *
 * @author Hiroku
 * @since January 24th, 2022
 */
abstract class SpawningCondition<T : SpawnablePosition> {
    companion object {
        val conditionTypes = mutableMapOf<String, Class<out SpawningCondition<*>>>()
        // 缓存类型兼容性检查结果，避免重复的反射调用
        private val compatibilityCache = mutableMapOf<Pair<Class<*>, Class<*>>, Boolean>()
        
        fun getByName(name: String) = conditionTypes[name]
        fun <T : SpawnablePosition, C : SpawningCondition<T>> register(name: String, clazz: Class<C>) {
            conditionTypes[name] = clazz
        }
        
        private fun isAssignableFromCached(targetClass: Class<*>, sourceClass: Class<*>): Boolean {
            val key = Pair(targetClass, sourceClass)
            return compatibilityCache.getOrPut(key) {
                targetClass.isAssignableFrom(sourceClass)
            }
        }
    }

    var dimensions: MutableList<ResourceLocation>? = null
    /** This gets checked in a precalculation but still needs to be checked for things like rarity multipliers. */
    var biomes: MutableSet<RegistryLikeCondition<Biome>>? = null
    var moonPhase: MoonPhaseRange? = null
    var canSeeSky: Boolean? = null
    var minX: Float? = null
    var minY: Float? = null
    var minZ: Float? = null
    var maxX: Float? = null
    var maxY: Float? = null
    var maxZ: Float? = null
    var minLight: Int? = null
    var maxLight: Int? = null
    var minSkyLight: Int? = null
    var maxSkyLight: Int? = null
    var isRaining: Boolean? = null
    var isThundering: Boolean? = null
    var timeRange: TimeRange? = null
    var structures: MutableList<Either<ResourceLocation, TagKey<Structure>>>? = null
    var isSlimeChunk: Boolean? = null
    var markers: MutableList<String>? = null

    @Transient
    var appendages = mutableListOf<AppendageCondition>()

    abstract fun spawnablePositionClass(): Class<out T>
    // 缓存spawnablePositionClass结果，避免重复调用
    private val cachedSpawnablePositionClass by lazy { spawnablePositionClass() }
    
    fun spawnablePositionMatches(spawnablePosition: SpawnablePosition) = isAssignableFromCached(cachedSpawnablePositionClass, spawnablePosition::class.java)

    fun isSatisfiedBy(spawnablePosition: SpawnablePosition): Boolean {
        return if (spawnablePositionMatches(spawnablePosition)) {
            fits(spawnablePosition as T)
        } else {
            false
        }
    }

    protected open fun fits(spawnablePosition: T): Boolean {
        // 缓存频繁访问的属性
        val position = spawnablePosition.position
        val world = spawnablePosition.world
        
        // 位置检查 - 最常见的过滤条件，优先检查
        if (position.x < minX.orMin() || position.x > maxX.orMax()) return false
        if (position.y < minY.orMin() || position.y > maxY.orMax()) return false
        if (position.z < minZ.orMin() || position.z > maxZ.orMax()) return false
        
        // 光照检查 - 第二常见的过滤条件
        if (spawnablePosition.light > maxLight.orMax() || spawnablePosition.light < minLight.orMin()) return false
        if (spawnablePosition.skyLight > maxSkyLight.orMax() || spawnablePosition.skyLight < minSkyLight.orMin()) return false
        
        // 简单属性检查
        moonPhase?.let { if (spawnablePosition.moonPhase !in it) return false }
        canSeeSky?.let { if (it != spawnablePosition.canSeeSky) return false }
        isRaining?.let { if (world.isRaining != it) return false }
        isThundering?.let { if (world.isThundering != it) return false }
        
        // 时间检查
        timeRange?.let { if (!it.contains((world.dayTime() % 24000).toInt())) return false }
        
        // 维度检查
        dimensions?.let { dims -> 
            if (dims.isNotEmpty() && world.dimension().location() !in dims) return false 
        }
        
        // 标记检查
        markers?.let { marks -> 
            if (marks.isNotEmpty() && marks.none { marker -> marker in spawnablePosition.markers }) return false 
        }
        
        // 生物群系检查
        biomes?.let { biomeConditions -> 
            if (biomeConditions.isNotEmpty() && biomeConditions.none { condition -> condition.fits(spawnablePosition.biomeHolder) }) return false 
        }
        
        // 附加条件检查
        if (appendages.any { !it.fits(spawnablePosition) }) return false
        
        // 结构检查
        structures?.let { structs ->
            if (structs.isNotEmpty()) {
                val structureAccess = world.structureManager()
                val cache = spawnablePosition.getStructureCache(position)
                if (structs.none {
                    it.map({ cache.check(structureAccess, position, it) }, { cache.check(structureAccess, position, it) })
                }) return false
            }
        }
        
        // 史莱姆区块检查
        isSlimeChunk?.let { slimeChunk ->
            if (slimeChunk) {
                val isSlimeChunk = WorldgenRandom.seedSlimeChunk(position.x shr 4, position.z shr 4, world.seed, 987234911L).nextInt(10) == 0
                if (!isSlimeChunk) return false
            }
        }
        
        return true
    }

    open fun copyFrom(other: SpawningCondition<*>, merger: Merger) {
        dimensions = merger.merge(dimensions, other.dimensions)?.toMutableList()
        biomes = merger.merge(biomes, other.biomes)?.toMutableSet()
        moonPhase = merger.mergeSingle(moonPhase, other.moonPhase)
        canSeeSky = merger.mergeSingle(canSeeSky, other.canSeeSky)
        minX = merger.mergeSingle(minX, other.minX)
        minY = merger.mergeSingle(minY, other.minY)
        minZ = merger.mergeSingle(minZ, other.minZ)
        maxX = merger.mergeSingle(maxX, other.maxX)
        maxY = merger.mergeSingle(maxY, other.maxY)
        maxZ = merger.mergeSingle(maxZ, other.maxZ)
        minLight = merger.mergeSingle(minLight, other.minLight)
        maxLight = merger.mergeSingle(maxLight, other.maxLight)
        minSkyLight = merger.mergeSingle(minSkyLight, other.minSkyLight)
        maxSkyLight = merger.mergeSingle(maxSkyLight, other.maxSkyLight)
        timeRange = merger.mergeSingle(timeRange, other.timeRange)
        structures = merger.merge(structures, other.structures)?.toMutableList()
    }

    open fun isValid(): Boolean {
        if (biomes != null && biomes!!.any { it == null })
            return false
        if (dimensions != null && dimensions!!.any { it == null })
            return false
        if (structures != null && structures!!.any { it == null })
            return false
        return true
    }
}
