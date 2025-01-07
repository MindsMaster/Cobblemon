package com.cobblemon.mod.common.block.entity

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.CobblemonBlockEntities
import com.cobblemon.mod.common.CobblemonItemComponents
import com.cobblemon.mod.common.api.fishing.FishingBait
import com.cobblemon.mod.common.api.spawning.BestSpawner
import com.cobblemon.mod.common.api.spawning.SpawnBucket
import com.cobblemon.mod.common.api.spawning.bait.BaitSpawnCause
import com.cobblemon.mod.common.api.spawning.detail.EntitySpawnResult
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.item.components.CookingComponent
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.core.BlockPos
import net.minecraft.core.HolderLookup
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtOps
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientGamePacketListener
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityTicker
import net.minecraft.world.level.block.state.BlockState

class LureCakeBlockEntity(
        pos: BlockPos,
        state: BlockState
) : BlockEntity(CobblemonBlockEntities.LURE_CAKE, pos, state) {

    var cookingComponent: CookingComponent? = null

    /**
     * Initializes the `CookingComponent` data from the given `ItemStack` when placed.
     */
    fun initializeFromItemStack(itemStack: ItemStack) {
        cookingComponent = itemStack.get(CobblemonItemComponents.COOKING_COMPONENT)
        markUpdated()
    }

    /**
     * Converts the block entity back into an `ItemStack` with the `CookingComponent` when broken.
     */
    fun toItemStack(): ItemStack {
        val stack = ItemStack(this.blockState.block)
        cookingComponent?.let { component ->
            // Use the appropriate method to set the component
            stack.getComponents().apply {
                stack.set(CobblemonItemComponents.COOKING_COMPONENT, component)
            }
        }
        return stack
    }

    /**
     * Generate a `FishingBait` by combining effects from all `RodBaitComponent` data in the `CookingComponent`.
     */
    fun getBaitFromLureCake(): FishingBait? {
        val component = cookingComponent ?: return null
        val combinedEffects = listOf(
                component.bait1.effects,
                component.bait2.effects,
                component.bait3.effects
        ).flatten()

        return FishingBait(
                item = cobblemonResource("lure_cake"), // Directly specify the lure_cake ResourceLocation
                effects = combinedEffects
        )
    }

    override fun saveAdditional(tag: CompoundTag, registries: HolderLookup.Provider) {
        super.saveAdditional(tag, registries)
        cookingComponent?.let { component ->
            CobblemonItemComponents.COOKING_COMPONENT.codec()
                    ?.encodeStart(NbtOps.INSTANCE, component)
                    ?.result()
                    ?.ifPresent { encodedTag ->
                        tag.put("CookingComponent", encodedTag)
                    }
        }
    }

    override fun loadAdditional(tag: CompoundTag, registries: HolderLookup.Provider) {
        super.loadAdditional(tag, registries)
        if (tag.contains("CookingComponent")) {
            CobblemonItemComponents.COOKING_COMPONENT.codec()
                    ?.parse(NbtOps.INSTANCE, tag.getCompound("CookingComponent"))
                    ?.result()
                    ?.ifPresent { component ->
                        cookingComponent = component
                    }
        }
    }

    /**
     * Synchronize block entity data with the client.
     */
    override fun getUpdatePacket(): Packet<ClientGamePacketListener>? {
        return ClientboundBlockEntityDataPacket.create(this)
    }

    override fun getUpdateTag(registryLookup: HolderLookup.Provider): CompoundTag {
        val tag = CompoundTag()
        saveAdditional(tag, registryLookup)
        return tag
    }

   /* override fun getUpdateTag(): CompoundTag {
        val tag = CompoundTag()
        saveAdditional(tag, HolderLookup.Provider.create(Stream.empty())) // Create a temporary empty provider
        return tag
    }*/


    /**
     * Mark the block entity as updated, forcing a save and client update.
     */
    private fun markUpdated() {
        setChanged()
        level?.sendBlockUpdated(worldPosition, blockState, blockState, 3)
    }

    /**
     * Spawns a Pokémon of the given species around the specified position.
     * @param world The world in which to spawn the Pokémon.
     * @param pos The position around which to spawn the Pokémon.
     * @param species The species of the Pokémon to spawn.
     * @param isMale Whether the Pokémon should be male or female.
     * @return The spawned Pokémon entity, or null if spawning failed.
     */
    fun spawnPokemon(
            world: Level,
            pos: BlockPos//,
            //species: PokemonSpecies
    ): Pair<PokemonEntity?,PokemonEntity?> {
        if (world !is ServerLevel) return null to null

        // Generate a random offset within the radius
        val randomOffset = {
            world.random.nextDouble() * SPAWN_RADIUS * 2 - SPAWN_RADIUS
        }
        val spawnPos = pos.offset(randomOffset().toInt(), 0, randomOffset().toInt())

        // todo Use Spawn files
        // todo CHANGE THIS! We probably want a brand new spawner similar to the fishing spawner
        val spawner = BestSpawner.baitSpawner

        val buckets = Cobblemon.bestSpawner.config.buckets

        val chosenBucket = chooseSpawnBucket(buckets)
        // todo WARNING! baitStack is null
        val spawnCause = BaitSpawnCause(
                spawner = spawner,
                bucket = chosenBucket,
                entity = null, // todo Crab Note: Do we want to use the lure cake as the entity being sent in?
                baitStack = this.toItemStack(), // todo Crab note: Maybe we can use baitStack as a spawnCondition so we can see what kind of bait is being used (cake or poke_bait)
                baitEffect = this.getBaitFromLureCake()
        )

        //
        val result = spawner.run(spawnCause, level as ServerLevel, spawnPos)

        var spawnedPokemon: PokemonEntity? = null
        var partnerPokemon: PokemonEntity? = null
        val resultingSpawn = result?.get()

        if (resultingSpawn is EntitySpawnResult) {
            for (entity in resultingSpawn.entities) {
                // we can query the spawned pokemon here
                spawnedPokemon = (entity as PokemonEntity)

                // CRAB IDEA: todo maybe some grass particles? as if they appeared from the grass or forest?
            }
        }

        return spawnedPokemon to partnerPokemon
    }
    fun chooseSpawnBucket(buckets: List<SpawnBucket>): SpawnBucket {
        val baseIncreases = listOf(2.5F, 1.0F, 0.6F)  // Base increases for the first three buckets beyond the first
        val adjustedWeights = buckets.mapIndexed { index, bucket ->
            if (index == 0) {
                // Placeholder, will be recalculated
                0.0F
            } else {
                val increase = if (index < baseIncreases.size) baseIncreases[index] else baseIncreases.last() + (index - baseIncreases.size + 1) * 0.15F
                bucket.weight + increase
            }
        }.toMutableList()

        // Recalculate the first bucket's weight to ensure the total is 100%
        val totalAdjustedWeight = adjustedWeights.sum() - adjustedWeights[0]  // Corrected to ensure the list contains Floats
        adjustedWeights[0] = 100.0F - totalAdjustedWeight + buckets[0].weight

        // Random selection based on adjusted weights
        val weightSum = adjustedWeights.sum()
        val chosenSum = kotlin.random.Random.nextDouble(weightSum.toDouble()).toFloat()  // Ensure usage of Random from kotlin.random package
        var sum = 0.0F
        adjustedWeights.forEachIndexed { index, weight ->
            sum += weight
            if (sum >= chosenSum) {
                return buckets[index]
            }
        }

        return buckets.first()  // Fallback
    }


    companion object {
        private const val RANDOM_SPAWN_CHANCE = 10 // 1 in 100 chance per random tick
        private const val SPAWN_RADIUS = 5.0 // Radius around the nest to spawn Pokémon

        val TICKER = BlockEntityTicker<LureCakeBlockEntity> { world, pos, state, blockEntity ->
            if (world.random.nextInt(RANDOM_SPAWN_CHANCE) == 0) {
                // spawn the lured pokemon
                blockEntity.spawnPokemon(world, pos)

                // todo age Cake block to make it seem eaten more
                world.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS)
            }
        }
    }
}