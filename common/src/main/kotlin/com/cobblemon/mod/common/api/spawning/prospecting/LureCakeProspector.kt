/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.spawning.prospecting

import com.cobblemon.mod.common.api.spawning.influence.WorldSlicedSpatialSpawningInfluence
import com.cobblemon.mod.common.api.spawning.influence.WorldSlicedSpawningInfluence
import com.cobblemon.mod.common.block.LureCakeBlock
import com.cobblemon.mod.common.block.entity.LureCakeBlockEntity
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.block.state.BlockState

object LureCakeProspector : SpawningInfluenceProspector {
    @JvmField
    val RANGE: Int = 48

    override fun prospect(world: ServerLevel, pos: BlockPos, blockState: BlockState): WorldSlicedSpatialSpawningInfluence? {
        val block = blockState.block
        if (block !is LureCakeBlock) {
            return null
        }
        val blockEntity = world.getBlockEntity(pos) as? LureCakeBlockEntity ?: return null
        val bait = blockEntity.getBaitFromLureCake() ?: return null
        return WorldSlicedSpatialSpawningInfluence(pos, RANGE.toFloat(), influence = bait)
    }
}