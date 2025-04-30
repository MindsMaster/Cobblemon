package com.cobblemon.mod.common.item.components

import com.mojang.serialization.Codec
import io.netty.buffer.ByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.resources.ResourceLocation

/**
 * Component for storing a list of main ingredient IDs.
 */
class IngredientComponent(
    val ingredientIds: List<ResourceLocation>
) {
    companion object {
        val CODEC: Codec<IngredientComponent> = ResourceLocation.CODEC.listOf()
            .xmap(::IngredientComponent, IngredientComponent::ingredientIds)

        val PACKET_CODEC: StreamCodec<ByteBuf, IngredientComponent> =
            ByteBufCodecs.fromCodec(CODEC)
    }

    override fun equals(other: Any?): Boolean {
        return other is IngredientComponent && other.ingredientIds == this.ingredientIds
    }

    override fun hashCode() = ingredientIds.hashCode()
}