/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.pokemon

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.Priority
import com.cobblemon.mod.common.api.abilities.Abilities
import com.cobblemon.mod.common.api.abilities.AbilityPool
import com.cobblemon.mod.common.api.abilities.CommonAbility
import com.cobblemon.mod.common.api.abilities.PotentialAbility
import com.cobblemon.mod.common.api.ai.config.BehaviourConfig
import com.cobblemon.mod.common.api.data.ShowdownIdentifiable
import com.cobblemon.mod.common.api.drop.DropTable
import com.cobblemon.mod.common.api.moves.MoveTemplate
import com.cobblemon.mod.common.api.net.Decodable
import com.cobblemon.mod.common.api.net.Encodable
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies
import com.cobblemon.mod.common.api.pokemon.effect.ShoulderEffect
import com.cobblemon.mod.common.api.pokemon.egg.EggGroup
import com.cobblemon.mod.common.api.pokemon.evolution.Evolution
import com.cobblemon.mod.common.api.pokemon.evolution.PreEvolution
import com.cobblemon.mod.common.api.pokemon.experience.ExperienceGroup
import com.cobblemon.mod.common.api.pokemon.experience.ExperienceGroups
import com.cobblemon.mod.common.api.pokemon.moves.Learnset
import com.cobblemon.mod.common.api.pokemon.stats.Stat
import com.cobblemon.mod.common.api.riding.RidingProperties
import com.cobblemon.mod.common.api.types.ElementalType
import com.cobblemon.mod.common.api.types.ElementalTypes
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.net.IntSize
import com.cobblemon.mod.common.pokemon.abilities.HiddenAbility
import com.cobblemon.mod.common.pokemon.ai.FormPokemonBehaviour
import com.cobblemon.mod.common.pokemon.lighthing.LightingData
import com.cobblemon.mod.common.util.*
import com.google.gson.annotations.SerializedName
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.world.entity.EntityDimensions
import net.minecraft.resources.ResourceLocation

class FormData(
    name: String = "Normal",
    // Internal for the sake of the base stat provider
    @SerializedName("baseStats")
    internal var _baseStats: MutableMap<Stat, Int>? = null,
    @SerializedName("maleRatio")
    private var _maleRatio: Float? = null,
    @SerializedName("baseScale")
    private var _baseScale: Float? = null,
    @SerializedName("hitbox")
    private var _hitbox: EntityDimensions? = null,
    @SerializedName("catchRate")
    private var _catchRate: Int? = null,
    @SerializedName("experienceGroup")
    private var _experienceGroup: ExperienceGroup? = null,
    @SerializedName("baseExperienceYield")
    private var _baseExperienceYield: Int? = null,
    @SerializedName("_baseFriendship")
    private var _baseFriendship: Int? = null,
    @SerializedName("evYield")
    private var _evYield: MutableMap<Stat, Int>? = null,
    @SerializedName("primaryType")
    private var _primaryType: ElementalType? = null,
    @SerializedName("secondaryType")
    private var _secondaryType: ElementalType? = null,
    @SerializedName("shoulderMountable")
    private val _shoulderMountable: Boolean? = null,
    @SerializedName("shoulderEffects")
    private val _shoulderEffects: MutableList<ShoulderEffect>? = null,
    @SerializedName("moves")
    private var _moves: Learnset? = null,
    @SerializedName("evolutions")
    private val _evolutions: MutableSet<Evolution>? = null,
    @SerializedName("abilities")
    private var _abilities: AbilityPool? = null,
    @SerializedName("drops")
    private var _drops: DropTable? = null,
    @SerializedName("pokedex")
    private var _pokedex: MutableList<String>? = null,
    @SerializedName("preEvolution")
    private val _preEvolution: PreEvolution? = null,
    private var standingEyeHeight: Float? = null,
    private var swimmingEyeHeight: Float? = null,
    private var flyingEyeHeight: Float? = null,
    @SerializedName("labels")
    private val _labels: Set<String>? = null,
    @SerializedName("dynamaxBlocked")
    private var _dynamaxBlocked: Boolean? = null,
    @SerializedName("eggGroups")
    private val _eggGroups: Set<EggGroup>? = null,
    @SerializedName("height")
    private var _height: Float? = null,
    @SerializedName("weight")
    private var _weight: Float? = null,
    @SerializedName("riding")
    private var _riding: RidingProperties? = null,
    @SerializedName("baseAI")
    private var _baseAI: MutableList<BehaviourConfig>? = null,
    @SerializedName("ai")
    private var _ai: MutableList<BehaviourConfig>? = null,
    val requiredMove: String? = null,
    val requiredItem: String? = null,
    /** For forms that can accept different items (e.g. Arceus-Grass: Meadow Plate or Grassium-Z). */
    val requiredItems: List<String>? = null,
    /**
     * The [MoveTemplate] of the signature attack of the G-Max form.
     * This is always null on any form aside G-Max.
     */
    val gigantamaxMove: MoveTemplate? = null,
    @SerializedName("battleTheme")
    private var _battleTheme: ResourceLocation? = null,
    @SerializedName("lightingData")
    private var _lightingData: LightingData? = null
) : Decodable, Encodable, ShowdownIdentifiable {
    @SerializedName("name")
    var name: String = name
        private set

    val baseStats: Map<Stat, Int>
        get() = _baseStats ?: species.baseStats

    val maleRatio: Float
        get() = _maleRatio ?: species.maleRatio
    val baseScale: Float
        get() = _baseScale ?: species.baseScale
    val hitbox: EntityDimensions
        get() = _hitbox ?: species.hitbox
    val catchRate: Int
        get() = _catchRate ?: species.catchRate
    val experienceGroup: ExperienceGroup
        get() = _experienceGroup ?: species.experienceGroup
    val baseExperienceYield: Int
        get() = _baseExperienceYield ?: species.baseExperienceYield
    val baseFriendship: Int
        get() = _baseFriendship ?: species.baseFriendship
    val evYield: Map<Stat, Int>
        get() = _evYield ?: species.evYield
    val primaryType: ElementalType
        get() = _primaryType ?: species.primaryType

    // Don't fall back to the species unless both types in the form are null
    val secondaryType: ElementalType?
        get() = if (_secondaryType == null && _primaryType == null) species.secondaryType else _secondaryType

    val shoulderMountable: Boolean
        get() = _shoulderMountable ?: species.shoulderMountable

    val shoulderEffects: MutableList<ShoulderEffect>
        get() = _shoulderEffects ?: species.shoulderEffects

    val pokedex
        get() = _pokedex ?: species.pokedex
    val moves: Learnset
        get() = _moves ?: species.moves

    val types: Iterable<ElementalType>
        get() = secondaryType?.let { listOf(primaryType, it) } ?: listOf(primaryType)

    val abilities: AbilityPool
        get() = _abilities ?: species.abilities

    val drops: DropTable
        get() = _drops ?: species.drops

    var aspects = mutableListOf<String>()

    val preEvolution: PreEvolution?
        get() = _preEvolution ?: species.preEvolution

    val behaviour = FormPokemonBehaviour()

    val dynamaxBlocked: Boolean
        get() = _dynamaxBlocked ?: species.dynamaxBlocked

    val eggGroups: Set<EggGroup>
        get() = _eggGroups ?: species.eggGroups
    val riding: RidingProperties
        get() = _riding ?: species.riding

    /**
     * The height in decimeters
     */
    val height: Float
        get() = _height ?: species.height

    /**
     * The weight in hectograms
     */
    val weight: Float
        get() = _weight ?: species.weight

    val labels: Set<String>
        get() = _labels ?: species.labels

    /**
     * Contains the evolutions of this form.
     * Do not access this property immediately after a species is loaded, it requires all species in the game to be loaded.
     * To be aware of this gamestage subscribe to [PokemonSpecies.observable].
     */
    val evolutions: MutableSet<Evolution>
        get() = _evolutions ?: mutableSetOf()

    val battleTheme: ResourceLocation
        get() = _battleTheme ?: species.battleTheme

    val lightingData: LightingData?
        get() {
            // Don't always return base species, this is that shitty scenario where forms need to specifically declare in order for null to be respected and intended
            if (this.species.standardForm == this) {
                return this.species.lightingData
            }
            return this._lightingData
        }

    val possibleGenders: Set<Gender>
        get() {
            return if (maleRatio == -1F) {
                setOf(Gender.GENDERLESS)
            } else if (maleRatio == 0F) {
                setOf(Gender.FEMALE)
            } else if (maleRatio == 1F) {
                setOf(Gender.MALE)
            } else {
                setOf(Gender.FEMALE, Gender.MALE)
            }
        }

    val baseAI: List<BehaviourConfig>?
        get() = _baseAI ?: species.baseAI
    val ai: List<BehaviourConfig>
        get() = _ai ?: species.ai

    fun eyeHeight(entity: PokemonEntity): Float {
        return this.resolveEyeHeight(entity) ?: return this.species.eyeHeight(entity)
    }

    private fun resolveEyeHeight(entity: PokemonEntity): Float? = when {
        entity.getCurrentPoseType() in PoseType.SWIMMING_POSES -> this.swimmingEyeHeight ?: this.standingEyeHeight
        entity.getCurrentPoseType() in PoseType.FLYING_POSES -> this.flyingEyeHeight ?: this.standingEyeHeight
        else -> this.standingEyeHeight
    }

    @Transient
    lateinit var species: Species

    fun initialize(species: Species): FormData {
        this.species = species
        this.behaviour.parent = species.behaviour
        Cobblemon.statProvider.provide(this)
        // These properties are lazy, these need all species to be reloaded but SpeciesAdditions is what will eventually trigger this after all species have been loaded
        this.preEvolution?.species
        this.preEvolution?.form
        this.evolutions.size
        this._lightingData?.let { this._lightingData = it.copy(lightLevel = it.lightLevel.coerceIn(0, 15)) }
        behaviour.herd.initialize()
        return this
    }

    internal fun resolveEvolutionMoves() {
        this.evolutions.forEach { evolution ->
            if (evolution.learnableMoves.isNotEmpty() && evolution.result.species != null) {
                val pokemon = evolution.result.create()
                pokemon.form.moves.evolutionMoves += evolution.learnableMoves
            }
        }
    }

    override fun equals(other: Any?): Boolean = other is FormData && other.showdownId() == this.showdownId()

    override fun hashCode(): Int = this.showdownId().hashCode()

    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeString(this.name)
        buffer.writeCollection(this.aspects) { pb, aspect -> pb.writeString(aspect) }
        buffer.writeNullable(this._baseStats) { statsBuffer, map ->
            statsBuffer.writeMap(map,
                { _, stat -> Cobblemon.statProvider.encode(buffer, stat)},
                { _, value -> buffer.writeSizedInt(IntSize.U_SHORT, value) }
            )
        }
        buffer.writeNullable(this._primaryType) { pb, type -> pb.writeString(type.showdownId) }
        buffer.writeNullable(this._secondaryType) { pb, type -> pb.writeString(type.showdownId) }
        buffer.writeNullable(this._experienceGroup) { pb, value -> pb.writeString(value.name) }
        buffer.writeNullable(this._height) { pb, height -> pb.writeFloat(height) }
        buffer.writeNullable(this._weight) { pb, weight -> pb.writeFloat(weight) }
        buffer.writeNullable(this._maleRatio) { pb, ratio -> pb.writeFloat(ratio) }
        buffer.writeNullable(this._baseScale) { buf, fl -> buf.writeFloat(fl) }
        buffer.writeNullable(this._hitbox) { pb, hitbox ->
            pb.writeFloat(hitbox.width)
            pb.writeFloat(hitbox.height)
            pb.writeBoolean(hitbox.fixed)
        }
        buffer.writeNullable(this._moves) { _, moves -> moves.encode(buffer)}
        buffer.writeNullable(this._pokedex) { pb1, pokedex -> pb1.writeCollection(pokedex)  { pb2, line -> pb2.writeString(line) } }
        buffer.writeNullable(this.lightingData) { pb, data ->
            pb.writeInt(data.lightLevel)
            pb.writeEnumConstant(data.liquidGlowMode)
        }
        buffer.writeNullable(_drops) { _, value -> value.encode(buffer) }
        buffer.writeNullable(_abilities) { _, abilities ->
            buffer.writeCollection<PotentialAbility>(abilities.toList()) { pb, ability ->
                pb.writeBoolean(ability is CommonAbility)
                pb.writeString(ability.template.name)
            }
        }
        buffer.writeNullable(_riding) { pb, riding -> riding.encode(buffer) }
    }

    override fun decode(buffer: RegistryFriendlyByteBuf) {
        this.name = buffer.readString()
        this.aspects = buffer.readList { buffer.readString() }.toMutableList()
        buffer.readNullable { mapBuffer ->
            this._baseStats = mapBuffer.readMap(
                { _ -> Cobblemon.statProvider.decode(buffer) },
                { _ -> buffer.readSizedInt(IntSize.U_SHORT) }
            ).toMutableMap()
        }
        this._primaryType = buffer.readNullable { pb -> ElementalTypes.get(pb.readString()) }
        this._secondaryType = buffer.readNullable { pb -> ElementalTypes.get(pb.readString()) }
        this._experienceGroup = buffer.readNullable { pb -> ExperienceGroups.findByName(pb.readString()) }
        this._height = buffer.readNullable { pb -> pb.readFloat() }
        this._weight = buffer.readNullable { pb -> pb.readFloat() }
        this._maleRatio = buffer.readNullable { pb -> pb.readFloat() }
        this._baseScale = buffer.readNullable { pb -> pb.readFloat() }
        this._hitbox = buffer.readNullable { pb -> pb.readEntityDimensions() }
        this._moves = buffer.readNullable { _ -> Learnset().apply { decode(buffer) }}
        this._pokedex = buffer.readNullable { pb -> pb.readList { it.readString() } }?.toMutableList()
        this._lightingData = buffer.readNullable { pb -> LightingData(pb.readInt(), pb.readEnumConstant(LightingData.LiquidGlowMode::class.java)) }
        this._drops = buffer.readNullable { _ -> DropTable().apply { decode(buffer) }}
        this._abilities = buffer.readNullable { pb ->
            AbilityPool().apply {
                pb.readList { _ ->
                    val isCommon = pb.readBoolean()
                    val template = pb.readString()
                    if (isCommon) {
                        CommonAbility(Abilities.getOrException(template))
                    } else {
                        HiddenAbility(Abilities.getOrException(template))
                    }
                }.forEach { add(Priority.NORMAL, it) }
            }
        }
        this._riding = buffer.readNullable { pb -> RidingProperties.decode(buffer) }
    }

    /**
     * The literal Showdown ID of this [formOnlyShowdownId] appended to [Species.showdownId].
     * For example Alolan Vulpix becomes 'vulpixalola'
     *
     * @return The literal Showdown ID of this species and form.
     */
    override fun showdownId(): String = this.species.showdownId() + this.formOnlyShowdownId()

    /**
     * The literal Showdown ID of this form [name].
     * This will be a lowercase version of the [name] with all the non-alphanumeric characters removed.
     * For example Alolan Vulpix becomes 'alola'
     *
     * @return The literal Showdown ID of this form only.
     */
    fun formOnlyShowdownId(): String = ShowdownIdentifiable.REGEX.replace(this.name.lowercase(), "")

}
