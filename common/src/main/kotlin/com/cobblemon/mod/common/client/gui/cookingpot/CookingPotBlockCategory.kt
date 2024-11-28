package com.cobblemon.mod.common.client.gui.cookingpot

import com.mojang.serialization.Codec
import io.netty.buffer.ByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.util.ByIdMap
import net.minecraft.util.StringRepresentable
import java.util.function.IntFunction

enum class CookingPotBookCategory(private val categoryName: String, private val categoryId: Int) : StringRepresentable {
    MISC("misc", 0);

    override fun getSerializedName(): String {
        return this.categoryName
    }

    private fun getCategoryId(): Int {
        return this.categoryId
    }

    companion object {
        val CODEC: Codec<CookingPotBookCategory> = StringRepresentable.fromEnum(::values)
        val BY_ID: IntFunction<CookingPotBookCategory> = ByIdMap.continuous(CookingPotBookCategory::getCategoryId,
            CookingPotBookCategory.entries.toTypedArray(), ByIdMap.OutOfBoundsStrategy.ZERO)
        val STREAM_CODEC: StreamCodec<ByteBuf, CookingPotBookCategory> = ByteBufCodecs.idMapper(BY_ID, CookingPotBookCategory::getCategoryId)
    }
}