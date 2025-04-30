/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.item

import com.cobblemon.mod.common.CobblemonItemComponents
import com.cobblemon.mod.common.CobblemonItems
import com.cobblemon.mod.common.api.cooking.Seasonings
import com.cobblemon.mod.common.entity.fishing.PokeRodFishingBobberEntity
import com.cobblemon.mod.common.item.interactive.PokerodItem
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.client.renderer.item.ItemProperties
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.player.Player

object CobblemonModelPredicateRegistry {

    fun registerPredicates() {

        val rods = listOf(
                CobblemonItems.AZURE_ROD,
                CobblemonItems.BEAST_ROD,
                CobblemonItems.CHERISH_ROD,
                CobblemonItems.CITRINE_ROD,
                CobblemonItems.DIVE_ROD,
                CobblemonItems.DREAM_ROD,
                CobblemonItems.DUSK_ROD,
                CobblemonItems.FAST_ROD,
                CobblemonItems.FRIEND_ROD,
                CobblemonItems.GREAT_ROD,
                CobblemonItems.HEAL_ROD,
                CobblemonItems.HEAVY_ROD,
                CobblemonItems.LEVEL_ROD,
                CobblemonItems.LOVE_ROD,
                CobblemonItems.LURE_ROD,
                CobblemonItems.LUXURY_ROD,
                CobblemonItems.MASTER_ROD,
                CobblemonItems.MOON_ROD,
                CobblemonItems.NEST_ROD,
                CobblemonItems.NET_ROD,
                CobblemonItems.PARK_ROD,
                CobblemonItems.POKE_ROD,
                CobblemonItems.PREMIER_ROD,
                CobblemonItems.QUICK_ROD,
                CobblemonItems.REPEAT_ROD,
                CobblemonItems.ROSEATE_ROD,
                CobblemonItems.SAFARI_ROD,
                CobblemonItems.SLATE_ROD,
                CobblemonItems.SPORT_ROD,
                CobblemonItems.TIMER_ROD,
                CobblemonItems.ULTRA_ROD,
                CobblemonItems.VERDANT_ROD,
                CobblemonItems.ANCIENT_AZURE_ROD,
                CobblemonItems.ANCIENT_CITRINE_ROD,
                CobblemonItems.ANCIENT_FEATHER_ROD,
                CobblemonItems.ANCIENT_GIGATON_ROD,
                CobblemonItems.ANCIENT_GREAT_ROD,
                CobblemonItems.ANCIENT_HEAVY_ROD,
                CobblemonItems.ANCIENT_IVORY_ROD,
                CobblemonItems.ANCIENT_JET_ROD,
                CobblemonItems.ANCIENT_LEADEN_ROD,
                CobblemonItems.ANCIENT_ORIGIN_ROD,
                CobblemonItems.ANCIENT_POKE_ROD,
                CobblemonItems.ANCIENT_ROSEATE_ROD,
                CobblemonItems.ANCIENT_SLATE_ROD,
                CobblemonItems.ANCIENT_ULTRA_ROD,
                CobblemonItems.ANCIENT_VERDANT_ROD,
                CobblemonItems.ANCIENT_WING_ROD
        )

        rods.forEach { rod ->
            ItemProperties.register(rod, ResourceLocation.parse("cast")) { stack, world, entity, seed ->
                if (entity !is Player || entity.fishing !is PokeRodFishingBobberEntity) return@register 0.0f

                val rodId = entity.fishing!!.entityData.get(PokeRodFishingBobberEntity.POKEROD_ID)

                val isMainHand = stack == entity.mainHandItem
                var isOffHand = stack == entity.offhandItem

                var mainHandItem = entity.mainHandItem.item
                val isFishingWithMainHand = mainHandItem is PokerodItem && rodId == mainHandItem.pokeRodId.toString()

                if (isFishingWithMainHand) {
                    isOffHand = false
                }

                if (isMainHand && isFishingWithMainHand || isOffHand) 1.0f else 0.0f
            }
        }

        ItemProperties.register(
                CobblemonItems.PONIGIRI,
                cobblemonResource("ponigiri_overlay")
        ) { stack, world, entity, seed ->
            val component = stack.get(CobblemonItemComponents.INGREDIENT)
            val id = component?.ingredientId?.toString() ?: return@register 0.0f

            return@register when (id) {
                "minecraft:carrot" -> 0.1f
                "minecraft:salmon" -> 0.2f
                "minecraft:cod" -> 0.3f
                "minecraft:rotten_flesh" -> 0.4f
                "minecraft:pumpkin" -> 0.5f
                else -> 0.0f
            }
        }

        ItemProperties.register(CobblemonItems.POKE_PUFF, cobblemonResource("poke_puff_flavour")) { stack, _, _, _ ->
            val flavour = stack.get(CobblemonItemComponents.FLAVOUR)?.getDominantFlavours()?.firstOrNull() ?: return@register 0.0f
            return@register when (flavour.name) {
                "SPICY" -> 0.1f
                "DRY" -> 0.2f
                "SWEET" -> 0.3f
                "BITTER" -> 0.4f
                "SOUR" -> 0.5f
                "MILD" -> 0.6f
                "PLAIN" -> 0.0f
                else -> 0.0f
            }
        }

        ItemProperties.register(CobblemonItems.POKE_PUFF, cobblemonResource("poke_puff_overlay")) { stack, _, _, _ ->
            val flavour = stack.get(CobblemonItemComponents.FLAVOUR)?.getDominantFlavours()?.firstOrNull()?.name?.lowercase()
            val ingredientId = stack.get(CobblemonItemComponents.INGREDIENT)?.ingredientId?.toString()
            val hasSugar = ingredientId == "minecraft:sugar"
            val sweet = when (ingredientId) {
                "cobblemon:strawberry_sweet" -> "strawberry"
                "cobblemon:love_sweet" -> "love"
                "cobblemon:berry_sweet" -> "berry"
                "cobblemon:ribbon_sweet" -> "ribbon"
                "cobblemon:clover_sweet" -> "clover"
                "cobblemon:flower_sweet" -> "flower"
                "cobblemon:star_sweet" -> "star"
                else -> null
            }
            val key = when {
                hasSugar && sweet != null -> "overlay_${flavour}_${sweet}"
                sweet != null -> "overlay_${sweet}"
                hasSugar && flavour != null -> "overlay_${flavour}"
                else -> "overlay_plain"
            }

            return@register 0.11f

            return@register when (key) {
                "overlay_spicy" -> 0.11f
                "overlay_strawberry" -> 0.12f
                "overlay_spicy_strawberry" -> 0.13f
                "overlay_love" -> 0.14f
                "overlay_spicy_love" -> 0.15f
                "overlay_berry" -> 0.16f
                "overlay_spicy_berry" -> 0.17f
                "overlay_ribbon" -> 0.18f
                "overlay_spicy_ribbon" -> 0.19f
                "overlay_clover" -> 0.2f
                "overlay_spicy_clover" -> 0.21f
                "overlay_flower" -> 0.22f
                "overlay_spicy_flower" -> 0.23f
                "overlay_star" -> 0.24f
                "overlay_spicy_star" -> 0.25f
                "overlay_dry" -> 0.26f
                "overlay_dry_strawberry" -> 0.27f
                "overlay_dry_love" -> 0.28f
                "overlay_dry_berry" -> 0.29f
                "overlay_dry_ribbon" -> 0.30f
                "overlay_dry_clover" -> 0.31f
                "overlay_dry_flower" -> 0.32f
                "overlay_dry_star" -> 0.33f
                "overlay_sweet" -> 0.34f
                "overlay_sweet_strawberry" -> 0.35f
                "overlay_sweet_love" -> 0.36f
                "overlay_sweet_berry" -> 0.37f
                "overlay_sweet_ribbon" -> 0.38f
                "overlay_sweet_clover" -> 0.39f
                "overlay_sweet_flower" -> 0.40f
                "overlay_sweet_star" -> 0.41f
                "overlay_bitter" -> 0.42f
                "overlay_bitter_strawberry" -> 0.43f
                "overlay_bitter_love" -> 0.44f
                "overlay_bitter_berry" -> 0.45f
                "overlay_bitter_ribbon" -> 0.46f
                "overlay_bitter_clover" -> 0.47f
                "overlay_bitter_flower" -> 0.48f
                "overlay_bitter_star" -> 0.49f
                "overlay_sour" -> 0.50f
                "overlay_sour_strawberry" -> 0.51f
                "overlay_sour_love" -> 0.52f
                "overlay_sour_berry" -> 0.53f
                "overlay_sour_ribbon" -> 0.54f
                "overlay_sour_clover" -> 0.55f
                "overlay_sour_flower" -> 0.56f
                "overlay_sour_star" -> 0.57f
                "overlay_mild" -> 0.58f
                "overlay_mild_strawberry" -> 0.59f
                "overlay_mild_love" -> 0.60f
                "overlay_mild_berry" -> 0.61f
                "overlay_mild_ribbon" -> 0.62f
                "overlay_mild_clover" -> 0.63f
                "overlay_mild_flower" -> 0.64f
                "overlay_mild_star" -> 0.65f
                "overlay_plain" -> 0.66f
                "overlay_plain_strawberry" -> 0.67f
                "overlay_plain_love" -> 0.68f
                "overlay_plain_berry" -> 0.69f
                "overlay_plain_ribbon" -> 0.70f
                "overlay_plain_clover" -> 0.71f
                "overlay_plain_flower" -> 0.72f
                "overlay_plain_star" -> 0.73f
                else -> 0.0f
            }
        }



    }
}