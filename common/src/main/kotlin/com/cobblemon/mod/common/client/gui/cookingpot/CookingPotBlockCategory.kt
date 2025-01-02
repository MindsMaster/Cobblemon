package com.cobblemon.mod.common.client.gui.cookingpot

import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import io.netty.buffer.ByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.util.ByIdMap
import net.minecraft.util.StringRepresentable
import java.util.function.IntFunction

enum class CookingPotBookCategory(private val categoryName: String, private val categoryId: Int) : StringRepresentable {
    FOODS("foods", 0),
    MEDICINES("medicines", 1),
    BAITS("baits", 2),
    MISC("misc", 3);

    override fun getSerializedName(): String {
        return this.categoryName
    }

    private fun getCategoryId(): Int {
        return this.categoryId
    }

    companion object {
        val CODEC: Codec<CookingPotBookCategory> = Codec.STRING.flatXmap(
            { name ->
                val category = CookingPotBookCategory.values().firstOrNull { it.categoryName.equals(name, ignoreCase = true) }
                if (category != null) {
                    println("Successfully parsed category: $name -> ${category.name}")
                    DataResult.success(category)
                } else {
                    println("Failed to parse category: $name")
                    DataResult.error { "Unknown category: $name" }
                }
            },
            { category ->
                println("Serializing category: ${category.categoryName}")
                DataResult.success(category.categoryName)
            }
        )
        val BY_ID: IntFunction<CookingPotBookCategory> = ByIdMap.continuous(CookingPotBookCategory::getCategoryId,
            CookingPotBookCategory.entries.toTypedArray(), ByIdMap.OutOfBoundsStrategy.ZERO)
        val STREAM_CODEC: StreamCodec<ByteBuf, CookingPotBookCategory> = ByteBufCodecs.idMapper(BY_ID, CookingPotBookCategory::getCategoryId)
    }
}