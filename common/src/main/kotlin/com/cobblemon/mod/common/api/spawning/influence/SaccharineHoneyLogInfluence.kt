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

class SaccharineHoneyLogInfluence(val pos: BlockPos? = null) : SpawningInfluence {

    var used = false
    val chanceForHA = 0.90  // todo set this to like 5% or something after we test that it works

    private fun markUsed() {
        used = true
    }

    override fun affectSpawn(entity: Entity) {
        super.affectSpawn(entity)
        if (entity is PokemonEntity) {
            if (Math.random() <= chanceForHA) {
                FishingSpawnCause.alterHAAttempt(entity)
                markUsed()
            }
        }
    }

    override fun affectWeight(detail: SpawnDetail, ctx: SpawningContext, weight: Float): Float {
        return super.affectWeight(detail, ctx, weight)
    }

    fun wasUsed(): Boolean = used
}
