package com.cobblemon.mod.common.pokemon.riding.states

import com.cobblemon.mod.common.api.riding.RidingState
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.phys.Vec3

class DolphinState : RidingState {

    companion object {
        val KEY = cobblemonResource("dolphin_state")
    }

    override val key: ResourceLocation = KEY

    override var isDirty = false

    var lastVelocity = Vec3(0.0,0.0,0.0)
        set(value) {
            field = value
            isDirty = true
        }

    override fun reset() {
        lastVelocity = Vec3(0.0,0.0,0.0)
        isDirty = false
    }

    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeVec3(lastVelocity)
    }

    override fun decode(buffer: RegistryFriendlyByteBuf) {
        lastVelocity = buffer.readVec3()
        isDirty = false
    }

}
