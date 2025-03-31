/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.pokemon.riding.states

import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.resources.ResourceLocation

class RunUpToFlightCompositeState : CompositeState() {

    companion object {
        val KEY = cobblemonResource("run_up_to_flight_composite_state")
    }

    override val key: ResourceLocation = KEY

    var currSpeed: Double = 0.0
        set(value) {
            field = value
            isDirty = true
        }

    override fun reset() {
        super.reset()
        currSpeed = 0.0
        isDirty = false
    }

    override fun encode(buffer: RegistryFriendlyByteBuf) {
        super.encode(buffer)
        buffer.writeDouble(currSpeed)
    }

    override fun decode(buffer: RegistryFriendlyByteBuf) {
        super.decode(buffer)
        currSpeed = buffer.readDouble()
        isDirty = false
    }

}
