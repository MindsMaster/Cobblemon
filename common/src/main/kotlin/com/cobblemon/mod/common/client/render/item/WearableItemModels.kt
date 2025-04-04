/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.item

import com.cobblemon.mod.common.api.tags.CobblemonItemTags.WEARABLE_FACE_ITEMS
import com.cobblemon.mod.common.api.tags.CobblemonItemTags.WEARABLE_HAT_ITEMS
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.core.component.DataComponents
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack

enum class WearableItemModels {
    DEBUG_HAT,
    DEBUG_FACE,

    BLACK_GLASSES,
    CHOICE_BAND,
    CHOICE_SPECS,
    EXP_SHARE,
    FOCUS_BAND,
    KINGS_ROCK,
    MUSCLE_BAND,
    ROCKY_HELMET,
    SAFETY_GOGGLES,
    WISE_GLASSES;

    fun getItemModelPath() = cobblemonResource("$MODEL_PATH/${this.name.lowercase()}")
    fun getItemSpritePath() = cobblemonResource(this.name.lowercase())

    companion object {
        const val MODEL_PATH = "item/wearable"

        fun getWearableModel3d(item: ItemStack): ResourceLocation? = run {
            val isDebugItem = item.components.get(DataComponents.CUSTOM_DATA)?.contains("debug") == true

            if (item.`is`(WEARABLE_HAT_ITEMS) && isDebugItem) return cobblemonResource("$MODEL_PATH/${DEBUG_HAT.name.lowercase()}")
            else if (item.`is`(WEARABLE_FACE_ITEMS) && isDebugItem) return cobblemonResource("$MODEL_PATH/${DEBUG_FACE.name.lowercase()}")
            else {
                val itemName = item.item.toString().substringAfterLast(":")
                entries.toList().forEach { if (it.name.lowercase() == itemName ) return it.getItemModelPath() }
                return null
            }
        }
        fun getWearableModel2d(item: ItemStack): ResourceLocation? = run {
            val itemName = item.item.toString().substringAfterLast(":")
            entries.toList().forEach { if (it.name.lowercase() == itemName ) return it.getItemSpritePath() }
            return null
        }
    }
}