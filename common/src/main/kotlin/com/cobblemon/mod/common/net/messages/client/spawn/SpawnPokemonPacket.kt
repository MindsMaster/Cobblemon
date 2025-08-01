/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.client.spawn

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.mark.Marks
import com.cobblemon.mod.common.api.pokeball.PokeBalls
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies
import com.cobblemon.mod.common.entity.PlatformType
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.pokemon.Gender
import com.cobblemon.mod.common.util.*
import net.minecraft.client.multiplayer.ClientLevel
import java.util.UUID
import net.minecraft.world.entity.Entity
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.chat.MutableComponent
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket
import net.minecraft.resources.ResourceLocation

class SpawnPokemonPacket(
        var ownerId: UUID?,
        var pokemonUUID: UUID,
        var scaleModifier: Float,
        var speciesId: ResourceLocation,
        var gender: Gender,
        var shiny: Boolean,
        var formName: String,
        var aspects: Set<String>,
        var battleId: UUID?,
        var phasingTargetId: Int,
        var beamMode: Byte,
        var platform: PlatformType,
        var nickname: MutableComponent?,
        var mark: ResourceLocation?,
        var labelLevel: Int,
        var poseType: PoseType,
        var unbattlable: Boolean,
        var hideLabel: Boolean,
        var caughtBall: ResourceLocation,
        var spawnYaw: Float,
        var friendship: Int,
        var freezeFrame: Float,
        var passengers: IntArray,
        var tickSpawned: Int,
        vanillaSpawnPacket: ClientboundAddEntityPacket,
) : SpawnExtraDataEntityPacket<SpawnPokemonPacket, PokemonEntity>(vanillaSpawnPacket) {

    override val id: ResourceLocation = ID

    constructor(entity: PokemonEntity, vanillaSpawnPacket: ClientboundAddEntityPacket) : this(
            entity.ownerUUID,
            entity.pokemon.uuid,
            entity.pokemon.scaleModifier,
            entity.exposedSpecies.resourceIdentifier,
            entity.pokemon.gender,
            entity.pokemon.shiny,
            entity.exposedForm.formOnlyShowdownId(),
            entity.exposedAspects,
            entity.battleId,
            entity.phasingTargetId,
            entity.beamMode.toByte(),
            entity.platform,
            entity.pokemon.nickname,
            entity.pokemon.activeMark?.identifier,
            if (Cobblemon.config.displayEntityLevelLabel) entity.entityData.get(PokemonEntity.LABEL_LEVEL) else -1,
            entity.entityData.get(PokemonEntity.POSE_TYPE),
            entity.entityData.get(PokemonEntity.UNBATTLEABLE),
            entity.entityData.get(PokemonEntity.HIDE_LABEL),
            entity.exposedBall.name,
            entity.entityData.get(PokemonEntity.SPAWN_DIRECTION),
            entity.entityData.get(PokemonEntity.FRIENDSHIP),
            entity.entityData.get(PokemonEntity.FREEZE_FRAME),
            entity.passengers.map { it.id }.toIntArray(),
            entity.tickCount,
            vanillaSpawnPacket
    )

    override fun encodeEntityData(buffer: RegistryFriendlyByteBuf) {
        buffer.writeNullable(ownerId) { _, v -> buffer.writeUUID(v) }
        buffer.writeUUID(this.pokemonUUID)
        buffer.writeFloat(this.scaleModifier)
        buffer.writeIdentifier(this.speciesId)
        buffer.writeEnumConstant(this.gender)
        buffer.writeBoolean(this.shiny)
        buffer.writeString(this.formName)
        buffer.writeCollection(this.aspects) { pb, value -> pb.writeString(value) }
        buffer.writeNullable(this.battleId) { pb, value -> pb.writeUUID(value) }
        buffer.writeInt(this.phasingTargetId)
        buffer.writeByte(this.beamMode.toInt())
        buffer.writeEnumConstant(this.platform)
        buffer.writeNullable(this.nickname) { _, v -> buffer.writeText(v) }
        buffer.writeNullable(this.mark) { _, v -> buffer.writeResourceLocation(v) }
        buffer.writeInt(this.labelLevel)
        buffer.writeEnumConstant(this.poseType)
        buffer.writeBoolean(this.unbattlable)
        buffer.writeBoolean(this.hideLabel)
        buffer.writeIdentifier(this.caughtBall)
        buffer.writeFloat(this.spawnYaw)
        buffer.writeInt(this.friendship)
        buffer.writeFloat(this.freezeFrame)
        buffer.writeVarIntArray(this.passengers)
        buffer.writeInt(this.tickSpawned)
    }

    override fun applyData(entity: PokemonEntity, level: ClientLevel) {
        entity.ownerUUID = ownerId
        entity.pokemon.apply {
            uuid = this@SpawnPokemonPacket.pokemonUUID
            scaleModifier = this@SpawnPokemonPacket.scaleModifier
            species = this@SpawnPokemonPacket.speciesId.let { PokemonSpecies.getByIdentifier(it) ?: PokemonSpecies.random() }
            gender = this@SpawnPokemonPacket.gender
            shiny = this@SpawnPokemonPacket.shiny
            form = this@SpawnPokemonPacket.formName.let { formName -> species.forms.find { it.formOnlyShowdownId() == formName }} ?: species.standardForm
            forcedAspects = this@SpawnPokemonPacket.aspects
            nickname = this@SpawnPokemonPacket.nickname
            this@SpawnPokemonPacket.mark?.let {activeMark = Marks.getByIdentifier(it) }
            PokeBalls.getPokeBall(this@SpawnPokemonPacket.caughtBall)?.let { caughtBall = it }
        }
        entity.phasingTargetId = this.phasingTargetId
        entity.beamMode = this.beamMode.toInt()
        entity.platform = this.platform
        entity.battleId = this.battleId
        entity.entityData.set(PokemonEntity.LABEL_LEVEL, labelLevel)
        entity.entityData.set(PokemonEntity.SPECIES, entity.pokemon.species.resourceIdentifier.toString())
        entity.entityData.set(PokemonEntity.ASPECTS, aspects)
        entity.entityData.set(PokemonEntity.POSE_TYPE, poseType)
        entity.entityData.set(PokemonEntity.UNBATTLEABLE, unbattlable)
        entity.entityData.set(PokemonEntity.HIDE_LABEL, hideLabel)
        entity.entityData.set(PokemonEntity.SPAWN_DIRECTION, spawnYaw)
        entity.entityData.set(PokemonEntity.FRIENDSHIP, friendship)
        entity.entityData.set(PokemonEntity.FREEZE_FRAME, freezeFrame)

        entity.ejectPassengers()
        passengers.forEach {
            val passenger = level.getEntity(it) ?: return@forEach
            passenger.startRiding(entity)
        }
        entity.tickSpawned = this.tickSpawned
        entity.delegate.updateAge(this.tickSpawned)
    }

    override fun checkType(entity: Entity): Boolean = entity is PokemonEntity

    companion object {
        val ID = cobblemonResource("spawn_pokemon_entity")
        fun decode(buffer: RegistryFriendlyByteBuf): SpawnPokemonPacket {
            val ownerId = buffer.readNullable { buffer.readUUID() }
            val pokemonUUID = buffer.readUUID()
            val scaleModifier = buffer.readFloat()
            val speciesId = buffer.readIdentifier()
            val gender = buffer.readEnumConstant(Gender::class.java)
            val shiny = buffer.readBoolean()
            val formName = buffer.readString()
            val aspects = buffer.readList { it.readString() }.toSet()
            val battleId = buffer.readNullable { buffer.readUUID() }
            val phasingTargetId = buffer.readInt()
            val beamModeEmitter = buffer.readByte()
            val platform = buffer.readEnumConstant(PlatformType::class.java)
            val nickname = buffer.readNullable { buffer.readText().copy() }
            val mark = buffer.readNullable { buffer.readResourceLocation() }
            val labelLevel = buffer.readInt()
            val poseType = buffer.readEnumConstant(PoseType::class.java)
            val unbattlable = buffer.readBoolean()
            val hideLabel = buffer.readBoolean()
            val caughtBall = buffer.readIdentifier()
            val spawnAngle = buffer.readFloat()
            val friendship = buffer.readInt()
            val freezeFrame = buffer.readFloat()
            val passengers = buffer.readVarIntArray()
            val tickSpawned = buffer.readInt()
            val vanillaPacket = decodeVanillaPacket(buffer)

            return SpawnPokemonPacket(ownerId, pokemonUUID, scaleModifier, speciesId, gender, shiny, formName, aspects, battleId, phasingTargetId, beamModeEmitter, platform, nickname, mark, labelLevel, poseType, unbattlable, hideLabel, caughtBall, spawnAngle, friendship, freezeFrame, passengers, tickSpawned, vanillaPacket)
        }
    }

}