package com.cobblemon.mod.common.block.general

import net.minecraft.core.BlockPos
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.RotatedPillarBlock
import net.minecraft.world.level.block.state.BlockState


class BaleBlock(properties: Properties) : RotatedPillarBlock(properties) {
    override fun fallOn(level: Level, state: BlockState, pos: BlockPos, entity: Entity, fallDistance: Float) {
        entity.causeFallDamage(fallDistance, 0.3f, level.damageSources().fall())
    }
}