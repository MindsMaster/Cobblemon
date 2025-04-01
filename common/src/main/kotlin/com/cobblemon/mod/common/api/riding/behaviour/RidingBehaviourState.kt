package com.cobblemon.mod.common.api.riding.behaviour

import com.cobblemon.mod.common.api.net.Decodable
import com.cobblemon.mod.common.api.net.Encodable
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.RegistryFriendlyByteBuf

/**
 * Represents the state of a Pokemon when being ridden.
 * This is intended to contain mutable state that is passed between the client and server
 * or temporary state that is used during the riding process.
 *
 * This will be destroyed when the a user dismounts the Pokemon.
 *
 * @author landonjw
 */
interface RidingBehaviourState : Encodable, Decodable {
    var isDirty: Boolean
    fun reset()
}

object NoState : RidingBehaviourState {
    override var isDirty = false
        set(value) = Unit // Just so nobody accidentally sets this to true
    override fun encode(buffer: RegistryFriendlyByteBuf) = Unit
    override fun decode(buffer: RegistryFriendlyByteBuf) = Unit
    override fun reset() = Unit
}
