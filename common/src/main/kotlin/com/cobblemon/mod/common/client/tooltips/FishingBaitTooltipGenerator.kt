/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.tooltips

import com.cobblemon.mod.common.CobblemonItemComponents
import com.cobblemon.mod.common.api.fishing.FishingBaits
import com.cobblemon.mod.common.api.pokemon.egg.EggGroup
import com.cobblemon.mod.common.api.text.*
import com.cobblemon.mod.common.api.types.ElementalTypes
import com.cobblemon.mod.common.item.interactive.PokerodItem
import com.cobblemon.mod.common.pokemon.Gender
import com.cobblemon.mod.common.util.lang
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack
import java.text.DecimalFormat

object FishingBaitTooltipGenerator : TooltipGenerator() {
    private val fishingBaitHeader by lazy { lang("fishing_bait_effect_header").blue() }
    private val fishingBaitItemClass by lazy { lang("item_class.fishing_bait").blue() }

    private val Genders = mapOf<Gender, Component>(
        Gender.MALE to lang("gender.male"),
        Gender.FEMALE to lang("gender.female"),
        Gender.GENDERLESS to lang("gender.genderless"),
    )

    override fun generateCategoryTooltip(stack: ItemStack, lines: MutableList<Component>): MutableList<Component>? {
        if (stack.get(DataComponents.HIDE_ADDITIONAL_TOOLTIP) != null) {
            return null
        }
        if (!FishingBaits.isFishingBait(stack)) return null
        return mutableListOf(fishingBaitItemClass)
    }

    override fun generateAdditionalTooltip(stack: ItemStack, lines: MutableList<Component>): MutableList<Component>? {
        if (stack.get(DataComponents.HIDE_ADDITIONAL_TOOLTIP) != null) {
            return null
        }
        val resultLines = mutableListOf<Component>()

        // Determine the FishingBait or combined effects from poke_bait
        val baitEffects = when {
            (stack.item.asItem()?.toString() == "cobblemon:poke_bait" || stack.item.asItem()?.toString() == "cobblemon:lure_cake") -> {
                val cookingComponent = stack.get(CobblemonItemComponents.COOKING_COMPONENT) ?: return null
                listOf(cookingComponent.bait1, cookingComponent.bait2, cookingComponent.bait3)
                    .flatMap { it.effects }
            }
            stack.item is PokerodItem -> PokerodItem.getBaitOnRod(stack)?.effects
            else -> FishingBaits.getFromBaitItemStack(stack)?.effects
        } ?: return null

        if (baitEffects.isEmpty()) return null

        resultLines.addLast(this.fishingBaitHeader)

        val formatter = DecimalFormat("0.##")

        baitEffects.forEach { effect ->
            val effectType = effect.type.path.toString()
            val effectSubcategory = effect.subcategory?.path
            val effectChance = effect.chance * 100
            var effectValue = when (effectType) {
                "bite_time" -> (effect.value * 100).toInt()
                else -> effect.value.toInt()
            }

            val subcategoryString: Component = if (effectSubcategory != null) {
                when (effectType) {
                    "nature", "ev", "iv" -> com.cobblemon.mod.common.api.pokemon.stats.Stats.getStat(
                        effectSubcategory
                    ).displayName

                    "gender_chance" -> Genders[Gender.valueOf(effectSubcategory.toUpperCase())]

                    "typing" -> ElementalTypes.get(effectSubcategory)?.displayName

                    "egg_group" -> {
                        val effectSubcategory = effect.subcategory?.path
                        val eggGroup = effectSubcategory?.let { EggGroup.fromIdentifier(it) }
                        eggGroup?.let {
                            val langKey = "egg_group.${it.name.lowercase()}"
                            lang(langKey)
                        } ?: Component.literal(effectSubcategory ?: "Unknown").gold()
                    }

                    else -> Component.empty()
                } ?: Component.literal("cursed").obfuscate()
            } else Component.literal("cursed").obfuscate()

            // Adjust shiny chance effectValue
            if (effectType == "shiny_reroll") {
                effectValue++
            }

            resultLines.addLast(
                lang(
                    "fishing_bait_effects.$effectType.tooltip",
                    Component.literal(formatter.format(effectChance)).yellow(),
                    subcategoryString.copy().gold(),
                    Component.literal(formatter.format(effectValue)).green()
                )
            )
        }

        return resultLines
    }

}