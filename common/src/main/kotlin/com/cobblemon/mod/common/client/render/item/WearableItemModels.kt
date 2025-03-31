package com.cobblemon.mod.common.client.render.item

import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.resources.ResourceLocation

enum class WearableItemModels {
    BLACK_GLASSES,
    CHOICE_BAND,
    CHOICE_SPECS,
    EXP_SHARE,
    FOCUS_BAND,
    KINGS_ROCK,
    SAFETY_GOGGLES,
    WISE_GLASSES;

    fun getItemModelPath() = cobblemonResource("item/wearable/${this.name.lowercase()}")
    fun getItemSpritePath() = cobblemonResource(this.name.lowercase())

    companion object {
        fun getWearableModel3d(id: String): ResourceLocation? = run {
            val itemName = id.substringAfterLast(":")
            entries.toList().forEach { if (it.name.lowercase() == itemName ) return it.getItemModelPath() }
            return null
        }
        fun getWearableModel2d(id: String): ResourceLocation? = run {
            val itemName = id.substringAfterLast(":")
            entries.toList().forEach { if (it.name.lowercase() == itemName ) return it.getItemSpritePath() }
            return null
        }
    }
}