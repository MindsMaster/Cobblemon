/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.pokemon.riding.states

import com.cobblemon.mod.common.api.riding.RidingState
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.phys.Vec3

class BirdAirState : RidingState {

    companion object {
        val KEY = cobblemonResource("bird_air_state")
    }

    override val key: ResourceLocation = KEY

    override var isDirty = false

    var currSpeed: Double = 0.0
        set(value) {
            field = value
            isDirty = true
        }

    var stamina: Float = 1.0f
        set(value) {
            field = value
            isDirty = true
        }

    var rideVel: Vec3 = Vec3.ZERO
        set(value) {
            field = value
            isDirty = true
        }

    var gliding: Boolean = false
        set(value) {
            field = value
            isDirty = true
        }

    override fun reset() {
        currSpeed = 0.0
        stamina = 1.0f
        rideVel = Vec3.ZERO
        gliding = false
        isDirty = false
    }

    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeDouble(currSpeed)
        buffer.writeFloat(stamina)
        buffer.writeVec3(rideVel)
        buffer.writeBoolean(gliding)
    }

    override fun decode(buffer: RegistryFriendlyByteBuf) {
        currSpeed = buffer.readDouble()
        stamina = buffer.readFloat()
        rideVel = buffer.readVec3()
        gliding = buffer.readBoolean()
        isDirty = false
    }

}
