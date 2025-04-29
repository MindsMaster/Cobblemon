package com.cobblemon.mod.common.item.components

import com.mojang.serialization.Codec
import io.netty.buffer.ByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.resources.ResourceLocation

/**
 * A simple component to store the main ingredient ID used in an item.
 *
 * @author Plastered_Crab
 * @since April 29th, 2025
 */
class IngredientComponent(
        val ingredientId: ResourceLocation
) {
    companion object {
        val CODEC: Codec<IngredientComponent> =
                ResourceLocation.CODEC.xmap(::IngredientComponent, IngredientComponent::ingredientId)

        val PACKET_CODEC: StreamCodec<ByteBuf, IngredientComponent> =
                ByteBufCodecs.fromCodec(CODEC)
    }

    override fun equals(other: Any?): Boolean {
        return other is IngredientComponent && other.ingredientId == this.ingredientId
    }

    override fun hashCode() = ingredientId.hashCode()
}
