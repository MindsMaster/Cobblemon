/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.pokemon.riding.controllers

import com.bedrockk.molang.Expression
import com.cobblemon.mod.common.api.riding.controller.RideController
import com.cobblemon.mod.common.api.riding.controller.posing.PoseOption
import com.cobblemon.mod.common.api.riding.controller.posing.PoseProvider
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.util.asExpression
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.getString
import com.cobblemon.mod.common.util.readString
import com.cobblemon.mod.common.util.resolveBoolean
import com.cobblemon.mod.common.util.resolveDouble
import com.cobblemon.mod.common.util.resolveFloat
import com.cobblemon.mod.common.util.writeString
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.phys.Vec2
import net.minecraft.world.phys.Vec3

class GliderAirController : RideController {
    override val key = KEY
    override val poseProvider = PoseProvider(PoseType.HOVER)
        .with(PoseOption(PoseType.FLY) { it.deltaMovement.horizontalDistance() > 0.1 })
    override val condition: (PokemonEntity) -> Boolean = { true }

    var glideSpeed: Expression = "0.1".asExpression()
        private set
    var speed: Expression = "1.0".asExpression()
        private set
    var canJump: Expression = "true".asExpression()
        private set
    var jumpVector = listOf("0".asExpression(), "0.3".asExpression(), "0".asExpression())
        private set

    override fun speed(entity: PokemonEntity, driver: Player): Float {
        return getRuntime(entity).resolveFloat(speed)
    }

    override fun rotation(entity: PokemonEntity, driver: LivingEntity): Vec2 {
        return Vec2(driver.xRot, driver.yRot)
    }

    override fun velocity(entity: PokemonEntity, driver: Player, input: Vec3): Vec3 {
        val xVector = driver.xxa.toDouble()
        val yVector = -getRuntime(entity).resolveDouble(glideSpeed)
        val zVector = driver.zza.toDouble()

        return Vec3(xVector, yVector, zVector)
    }

    override fun gravity(entity: PokemonEntity, regularGravity: Double): Double? {
        return 0.0
    }

    override fun canJump(entity: PokemonEntity, driver: Player): Boolean {
        return getRuntime(entity).resolveBoolean(canJump)
    }

    override fun jumpForce(entity: PokemonEntity, driver: Player, jumpStrength: Int): Vec3 {
        val runtime = getRuntime(entity)
        val jumpVector = jumpVector.map { runtime.resolveDouble(it) }
        return Vec3(jumpVector[0], jumpVector[1], jumpVector[2])
    }

    override fun encode(buffer: RegistryFriendlyByteBuf) {
        super.encode(buffer)
        buffer.writeString(glideSpeed.toString())
        buffer.writeString(speed.toString())
        buffer.writeString(canJump.toString())
        buffer.writeString(jumpVector[0].getString())
        buffer.writeString(jumpVector[1].getString())
        buffer.writeString(jumpVector[2].getString())
    }

    override fun decode(buffer: RegistryFriendlyByteBuf) {
        glideSpeed = buffer.readString().asExpression()
        speed = buffer.readString().asExpression()
        canJump = buffer.readString().asExpression()
        jumpVector = listOf(
            buffer.readString().asExpression(),
            buffer.readString().asExpression(),
            buffer.readString().asExpression()
        )
    }

    companion object {
        val KEY: ResourceLocation = cobblemonResource("air/glider")
    }
}