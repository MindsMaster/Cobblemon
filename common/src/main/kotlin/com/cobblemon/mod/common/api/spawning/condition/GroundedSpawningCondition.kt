/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.spawning.condition

import com.cobblemon.mod.common.api.conditional.RegistryLikeCondition
import com.cobblemon.mod.common.api.spawning.position.GroundedSpawnablePosition
import com.cobblemon.mod.common.util.Merger
import net.minecraft.world.level.block.Block

/**
 * Base type for a spawning condition that applies to some kind of [GroundedSpawnablePosition]. This
 * can be extended for subclasses of [GroundedSpawnablePosition].
 *
 * @author Hiroku
 * @since February 7th, 2022
 */
abstract class GroundedTypeSpawningCondition<T : GroundedSpawnablePosition> : AreaTypeSpawningCondition<T>() {
    var neededBaseBlocks: MutableList<RegistryLikeCondition<Block>>? = null

    override fun fits(spawnablePosition: T): Boolean {
        return if (!super.fits(spawnablePosition)) {
            false
        } else if (neededBaseBlocks != null && neededBaseBlocks!!.none { it.fits(spawnablePosition.baseBlockHolder) }) {
            return false
        } else {
            return true
        }
    }

    override fun copyFrom(other: SpawningCondition<*>, merger: Merger) {
        super.copyFrom(other, merger)
        if (other is GroundedTypeSpawningCondition) {
            neededBaseBlocks = merger.merge(neededBaseBlocks, other.neededBaseBlocks)?.toMutableList()
        }
    }

    override fun isValid(): Boolean {
        val containsNullValues = neededBaseBlocks != null && neededBaseBlocks!!.any {it == null}
        return super.isValid() && !containsNullValues
    }
}

/**
 * A spawning condition for a [GroundedSpawnablePosition].
 *
 * @author Hiroku
 * @since February 7th, 2022
 */
open class GroundedSpawningCondition : GroundedTypeSpawningCondition<GroundedSpawnablePosition>() {
    override fun spawnablePositionClass() = GroundedSpawnablePosition::class.java
    companion object {
        const val NAME = "grounded"
    }
}