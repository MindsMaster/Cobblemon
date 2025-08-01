/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.util.codec.internal

import com.cobblemon.mod.common.api.types.tera.TeraType
import com.cobblemon.mod.common.client.settings.ServerSettings
import com.cobblemon.mod.common.net.messages.client.pokemon.update.evolution.AddEvolutionPacket.Companion.convertToDisplay
import com.cobblemon.mod.common.pokeball.PokeBall
import com.cobblemon.mod.common.pokemon.*
import com.cobblemon.mod.common.pokemon.activestate.ShoulderedState
import com.cobblemon.mod.common.pokemon.evolution.CobblemonEvolutionProxy
import com.cobblemon.mod.common.pokemon.evolution.controller.ClientEvolutionController
import com.cobblemon.mod.common.pokemon.evolution.controller.ServerEvolutionController
import com.cobblemon.mod.common.pokemon.status.PersistentStatusContainer
import com.cobblemon.mod.common.util.DataKeys
import com.cobblemon.mod.common.util.codec.CodecUtils
import com.cobblemon.mod.common.util.server
import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import java.util.*
import net.minecraft.core.UUIDUtil
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.item.ItemStack

internal data class ClientPokemonP2(
    val state: Optional<ShoulderedState>,
    val status: Optional<PersistentStatusContainer>,
    val caughtBall: PokeBall,
    val faintedTimer: Int,
    val healTimer: Int,
    val shiny: Boolean,
    val nature: Nature,
    val mintedNature: Optional<Nature>,
    val heldItem: ItemStack,
    val persistentData: CompoundTag,
    val tetheringId: Optional<UUID>,
    val teraType: TeraType,
    val dmaxLevel: Int,
    val gmaxFactor: Boolean,
    val tradeable: Boolean,
    val evolutionController: Optional<ClientEvolutionController.Intermediate>
) : Partial<Pokemon> {

    override fun into(other: Pokemon): Pokemon {
        this.state.ifPresent { other.state = it }
        this.status.ifPresent { other.status = it }
        other.caughtBall = this.caughtBall
        other.faintedTimer = this.faintedTimer
        other.healTimer = this.healTimer
        other.shiny = this.shiny
        other.nature = this.nature
        this.mintedNature.ifPresent { other.mintedNature = it }
        other.heldItem = this.heldItem.copy()
        other.persistentData = this.persistentData
        this.tetheringId.ifPresent { other.tetheringId = it }
        other.teraType = this.teraType
        other.dmaxLevel = this.dmaxLevel
        other.gmaxFactor = this.gmaxFactor
        other.tradeable = this.tradeable
        this.evolutionController.ifPresent {
            (other.evolutionProxy as? CobblemonEvolutionProxy)?.overrideController(it.create(other))
        }
        return other
    }

    companion object {
        /**
         * do not use cobblemon.config in here, as this is used by the client whose config is different to server, always use [ServerSettings]
         */
        internal val CODEC: MapCodec<ClientPokemonP2> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                ShoulderedState.CODEC.optionalFieldOf(DataKeys.POKEMON_STATE).forGetter(ClientPokemonP2::state),
                PersistentStatusContainer.CODEC.optionalFieldOf(DataKeys.POKEMON_STATUS).forGetter(ClientPokemonP2::status),
                PokeBall.BY_IDENTIFIER_CODEC.fieldOf(DataKeys.POKEMON_CAUGHT_BALL).forGetter(ClientPokemonP2::caughtBall),
                Codec.INT.fieldOf(DataKeys.POKEMON_FAINTED_TIMER).forGetter(ClientPokemonP2::faintedTimer),
                Codec.INT.fieldOf(DataKeys.POKEMON_HEALING_TIMER).forGetter(ClientPokemonP2::healTimer),
                Codec.BOOL.fieldOf(DataKeys.POKEMON_SHINY).forGetter(ClientPokemonP2::shiny),
                Nature.BY_IDENTIFIER_CODEC.fieldOf(DataKeys.POKEMON_NATURE).forGetter(ClientPokemonP2::nature),
                Nature.BY_IDENTIFIER_CODEC.optionalFieldOf(DataKeys.POKEMON_MINTED_NATURE).forGetter(ClientPokemonP2::mintedNature),
                ItemStack.CODEC.optionalFieldOf(DataKeys.HELD_ITEM, ItemStack.EMPTY).forGetter(ClientPokemonP2::heldItem),
                CompoundTag.CODEC.fieldOf(DataKeys.POKEMON_PERSISTENT_DATA).forGetter(ClientPokemonP2::persistentData),
                UUIDUtil.LENIENT_CODEC.optionalFieldOf(DataKeys.TETHERING_ID).forGetter(ClientPokemonP2::tetheringId),
                TeraType.BY_IDENTIFIER_CODEC.fieldOf(DataKeys.POKEMON_TERA_TYPE).forGetter(ClientPokemonP2::teraType),
                CodecUtils.dynamicIntRange(0) { ServerSettings.maxDynamaxLevel }.fieldOf(DataKeys.POKEMON_DMAX_LEVEL).forGetter(ClientPokemonP2::dmaxLevel),
                Codec.BOOL.fieldOf(DataKeys.POKEMON_GMAX_FACTOR).forGetter(ClientPokemonP2::gmaxFactor),
                Codec.BOOL.fieldOf(DataKeys.POKEMON_TRADEABLE).forGetter(ClientPokemonP2::tradeable),
                ClientEvolutionController.CODEC.optionalFieldOf(DataKeys.POKEMON_EVOLUTIONS).forGetter(ClientPokemonP2::evolutionController),
            ).apply(instance, ::ClientPokemonP2)
        }

        internal fun from(pokemon: Pokemon): ClientPokemonP2 = ClientPokemonP2(
            Optional.ofNullable(pokemon.state as? ShoulderedState),
            Optional.ofNullable(pokemon.status),
            pokemon.caughtBall,
            pokemon.faintedTimer,
            pokemon.healTimer,
            pokemon.shiny,
            pokemon.nature,
            Optional.ofNullable(pokemon.mintedNature),
            pokemon.heldItemNoCopy(),
            pokemon.persistentData,
            Optional.ofNullable(pokemon.tetheringId),
            pokemon.teraType,
            pokemon.dmaxLevel,
            pokemon.gmaxFactor,
            pokemon.tradeable,
            Optional.ofNullable((pokemon.evolutionProxy.current() as? ServerEvolutionController)?.let {
                //TOOD figure out a closer registry access (might have to break some method signatures for this (1.7?)
                ClientEvolutionController.Intermediate(it.map { it.convertToDisplay(pokemon, registryAccess = server()?.registryAccess() ?: throw IllegalStateException("No registry access available")) }.toSet())
            })
        )
    }

}