/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.pokemon.riding.states

import com.cobblemon.mod.common.api.riding.RidingState
import com.cobblemon.mod.common.api.riding.controller.RideController
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.readNullable
import com.cobblemon.mod.common.util.writeNullable
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.resources.ResourceLocation

open class CompositeState : RidingState {

    companion object {
        val KEY = cobblemonResource("composite_state")
    }

    override val key: ResourceLocation = KEY

    override var isDirty = false

    var activeController: ResourceLocation? = null
        set(value) {
            field = value
            isDirty = true
        }

    var timeTransitioned = -100L
        set(value) {
            field = value
            isDirty = true
        }

    override fun reset() {
        activeController = null
        timeTransitioned = -100L
        isDirty = false
    }

    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeNullable(activeController) { _, value -> buffer.writeResourceLocation(value) }
        buffer.writeLong(timeTransitioned)
    }

    override fun decode(buffer: RegistryFriendlyByteBuf) {
        activeController = buffer.readNullable { buffer.readResourceLocation() }
        timeTransitioned = buffer.readLong()
        isDirty = false
    }

}
