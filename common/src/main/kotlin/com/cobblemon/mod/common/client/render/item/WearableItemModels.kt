package com.cobblemon.mod.common.client.render.item

import com.cobblemon.mod.common.util.cobblemonResource

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
}