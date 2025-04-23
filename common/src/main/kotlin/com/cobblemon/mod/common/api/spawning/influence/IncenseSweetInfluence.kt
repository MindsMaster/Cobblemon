package com.cobblemon.mod.common.api.spawning.influence

import com.cobblemon.mod.common.api.pokemon.PokemonSpecies
import com.cobblemon.mod.common.api.spawning.context.SpawningContext
import com.cobblemon.mod.common.api.spawning.detail.PokemonSpawnDetail
import com.cobblemon.mod.common.api.spawning.detail.SpawnDetail
import com.cobblemon.mod.common.api.spawning.fishing.FishingSpawnCause
import com.cobblemon.mod.common.api.types.ElementalTypes
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import net.minecraft.core.BlockPos
import net.minecraft.world.entity.Entity

class IncenseSweetInfluence(val pos: BlockPos? = null) : SpawningInfluence {

    override fun affectSpawn(entity: Entity) {
        super.affectSpawn(entity)
    }

    override fun affectWeight(detail: SpawnDetail, ctx: SpawningContext, weight: Float): Float {
        return super.affectWeight(detail, ctx, weight)
    }
}
