/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common


import com.cobblemon.mod.common.client.gui.cookingpot.CookingPotRecipe
import com.cobblemon.mod.common.platform.PlatformRegistry
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.crafting.Recipe
import net.minecraft.world.item.crafting.RecipeType

object CobblemonRecipeTypes : PlatformRegistry<Registry<RecipeType<*>>, ResourceKey<Registry<RecipeType<*>>>, RecipeType<*>>() {

    val COOKING_POT_COOKING : RecipeType<CookingPotRecipe> = register("cobblemon:cooking_pot")

    override val registry: Registry<RecipeType<*>>
        get() = BuiltInRegistries.RECIPE_TYPE
    override val resourceKey: ResourceKey<Registry<RecipeType<*>>>
        get() = Registries.RECIPE_TYPE

    fun <T : Recipe<*>> register(identifier: String): RecipeType<T> {
        return Registry.register(
            BuiltInRegistries.RECIPE_TYPE,
            identifier,
            object : RecipeType<T> {
                override fun toString(): String {
                    return identifier
                }
            }
        ) as RecipeType<T>
    }


}