/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.fishing

import com.cobblemon.mod.common.Cobblemon.LOGGER
import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.cobblemon.mod.common.api.events.fishing.BaitEffectFunctionRegistryEvent
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies
import com.cobblemon.mod.common.api.pokemon.egg.EggGroup
import com.cobblemon.mod.common.api.pokemon.stats.Stats
import com.cobblemon.mod.common.api.spawning.context.SpawningContext
import com.cobblemon.mod.common.api.spawning.detail.PokemonSpawnDetail
import com.cobblemon.mod.common.api.spawning.detail.SpawnDetail
import com.cobblemon.mod.common.api.spawning.fishing.FishingSpawnCause
import com.cobblemon.mod.common.api.spawning.influence.SpawningInfluence
import com.cobblemon.mod.common.api.types.ElementalTypes
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.util.cobblemonResource
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import java.util.Optional
import net.minecraft.core.Registry
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.Entity
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack

data class SpawnBait(
    val item: ResourceLocation,
    val effects: List<Effect>,
): SpawningInfluence {
    fun toItemStack(itemRegistry: Registry<Item>) = item.let(itemRegistry::get)?.let { ItemStack(it) } ?: ItemStack.EMPTY

    data class Effect(
        val type: ResourceLocation,
        val subcategory: ResourceLocation?,
        val chance: Double = 0.0,
        val value: Double = 0.0
    ) {
        constructor(type: ResourceLocation, subcategory: Optional<ResourceLocation>, chance: Double, value: Double) : this(type, subcategory.orElse(null), chance, value)

        companion object {
            val CODEC = RecordCodecBuilder.create<Effect> { instance ->
                instance.group(
                    ResourceLocation.CODEC.fieldOf("type").forGetter { it.type },
                    ResourceLocation.CODEC.optionalFieldOf("subcategory").forGetter { Optional.ofNullable(it.subcategory) },
                    Codec.DOUBLE.fieldOf("chance").forGetter { it.chance },
                    Codec.DOUBLE.fieldOf("value").forGetter { it.value }
                ).apply(instance, ::Effect)
            }
        }
    }

    companion object {
        val CODEC = RecordCodecBuilder.create<SpawnBait> { instance ->
            instance.group(
                ResourceLocation.CODEC.fieldOf("item").forGetter { it.item },
                Effect.CODEC.listOf().fieldOf("effects").forGetter {it.effects}
            ).apply(instance, ::SpawnBait)
        }

        val STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC)

        val BLANK_BAIT = SpawnBait(
            cobblemonResource("blank"),
            emptyList()
        )
    }

    object Effects {
        private val EFFECT_FUNCTIONS: MutableMap<ResourceLocation, (PokemonEntity, Effect) -> Unit> = mutableMapOf()
        val NATURE = cobblemonResource("nature")
        val IV = cobblemonResource("iv")
        val EV = cobblemonResource("ev")
        val BITE_TIME = cobblemonResource("bite_time")
        val GENDER_CHANCE = cobblemonResource("gender_chance")
        val LEVEL_RAISE = cobblemonResource("level_raise")
        val TYPING = cobblemonResource("typing")
        val EGG_GROUP = cobblemonResource("egg_group")
        val SHINY_REROLL = cobblemonResource("shiny_reroll")
        val HIDDEN_ABILITY_CHANCE = cobblemonResource("ha_chance")
        val POKEMON_CHANCE = cobblemonResource("pokemon_chance")
        val FRIENDSHIP = cobblemonResource("friendship")
        val INERT = cobblemonResource("inert")

        fun registerEffect(type: ResourceLocation, effect: (PokemonEntity, Effect) -> Unit) {
            EFFECT_FUNCTIONS[type] = effect
        }

        fun getEffectFunction(type: ResourceLocation): ((PokemonEntity, Effect) -> Unit)? {
            return EFFECT_FUNCTIONS[type]
        }

        fun setupEffects() {
            EFFECT_FUNCTIONS[NATURE] = { entity, effect -> FishingSpawnCause.alterNatureAttempt(entity, effect) }
            EFFECT_FUNCTIONS[IV] = { entity, effect -> FishingSpawnCause.alterIVAttempt(entity, effect) }
            EFFECT_FUNCTIONS[SHINY_REROLL] = { entity, effect -> FishingSpawnCause.shinyReroll(entity, effect) }
            EFFECT_FUNCTIONS[GENDER_CHANCE] = { entity, effect -> FishingSpawnCause.alterGenderAttempt(entity, effect) }
            EFFECT_FUNCTIONS[LEVEL_RAISE] = { entity, effect -> FishingSpawnCause.alterLevelAttempt(entity, effect) }
            EFFECT_FUNCTIONS[HIDDEN_ABILITY_CHANCE] = { entity, _ -> FishingSpawnCause.alterHAAttempt(entity) }
            EFFECT_FUNCTIONS[FRIENDSHIP] = { entity, effect -> FishingSpawnCause.alterFriendshipAttempt(entity, effect) }
            CobblemonEvents.BAIT_EFFECT_REGISTRATION.post(BaitEffectFunctionRegistryEvent()) { event ->
                EFFECT_FUNCTIONS.putAll(event.functions)
            }
        }
    }

    override fun affectSpawn(entity: Entity) {
        super.affectSpawn(entity)
        if (entity is PokemonEntity) {
            effects.forEach { it ->
                if (Math.random() <= it.chance) {
                    Effects.getEffectFunction(it.type)?.invoke(entity, it)
                }
            }

            // Some of the bait actions might have changed the aspects and we need it to be
            // in the entityData IMMEDIATELY otherwise it will flash as what it would be
            // with the old aspects.
            // New aspects copy into the entity data only on the next tick.
            entity.entityData.set(PokemonEntity.ASPECTS, entity.pokemon.aspects)
        }
    }

    // EV related bait effects
    override fun affectWeight(detail: SpawnDetail, ctx: SpawningContext, weight: Float): Float {
        // if bait exists and any effects are related to EV yields
        if (effects.any{ it.type == Effects.EV }){
            if (detail is PokemonSpawnDetail) {
                val detailSpecies = detail.pokemon.species?.let { PokemonSpecies.getByName(it) }
                val baitEVStat = effects.firstOrNull { it.type == Effects.EV }?.subcategory?.path?.let { Stats.getStat(it) }

                if (detailSpecies != null && baitEVStat != null) {
                    val evYieldValue = detailSpecies.evYield[baitEVStat]?.toFloat() ?: 0f
                    return when {
                        evYieldValue > 0 -> super.affectWeight(detail, ctx, weight) // use original weight if EV yield is greater than 0
                        else -> super.affectWeight(detail, ctx, 0f) // use spawn weight of 0 if EV yield is 0
                    }
                }
            }
        }
        // if bait exists and any effects are related to Typing
        if (effects.any{ it.type == Effects.TYPING }){
            if (detail is PokemonSpawnDetail) {
                val detailSpecies = detail.pokemon.species?.let { PokemonSpecies.getByName(it) }
                val baitEffect = effects.firstOrNull { it.type == Effects.TYPING }
                val baitTypingEffect = baitEffect?.subcategory?.path?.let { ElementalTypes.get(it) }

                if (detailSpecies != null && baitTypingEffect != null) {
                    val isMatchingType = detailSpecies.types.contains(baitTypingEffect)
                    return when {
                        isMatchingType -> super.affectWeight(detail, ctx, weight * baitEffect.value.toFloat()) // multiply weight by multiplier of bait effect if typing is found
                        else -> super.affectWeight(detail, ctx, weight) // use base spawn weight if typing is not same as bait
                    }
                }
            }
        }
        // if bait exists and any effects are related to Egg Groups
        if (effects.any { it.type == Effects.EGG_GROUP }) {
            if (detail is PokemonSpawnDetail) {
                val detailSpecies = detail.pokemon.species?.let { PokemonSpecies.getByName(it) }

                if (detailSpecies != null) {
                    // Collect all the egg group effects
                    val eggGroupEffects = effects.filter { it.type == Effects.EGG_GROUP }

                    // Check if any of the egg group effects match the species' egg groups
                    val matchingEffect = eggGroupEffects.firstOrNull { effect ->
                        val effectEggGroupKey = effect.subcategory?.path ?: return@firstOrNull false
                        val eggGroup = EggGroup.fromIdentifier(effectEggGroupKey)
                        if (eggGroup == null) {
                            LOGGER.warn("Unknown egg group identifier: $effectEggGroupKey")
                            return@firstOrNull false
                        }
                        detailSpecies.eggGroups.contains(eggGroup)
                    }

                    if (matchingEffect != null) {
                        val multiplier = matchingEffect.value
                        return super.affectWeight(detail, ctx, (weight * multiplier).toFloat())
                    }
                }
            }
        }
        return super.affectWeight(detail, ctx, weight)
    }
}


