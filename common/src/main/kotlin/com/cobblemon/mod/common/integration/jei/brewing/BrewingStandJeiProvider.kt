/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.integration.jei.brewing

import com.cobblemon.mod.common.CobblemonRecipeTypes
import mezz.jei.api.constants.RecipeTypes
import mezz.jei.api.registration.IRecipeRegistration
import net.minecraft.client.Minecraft

class BrewingStandJeiProvider {

    companion object {
        fun registerRecipes(registration: IRecipeRegistration) {
            val recipeManger =
                Minecraft.getInstance().level?.recipeManager ?: throw IllegalStateException("Recipe manager not found")

            val allBrewingRecipes = Minecraft.getInstance().level?.recipeManager
                ?.getAllRecipesFor(CobblemonRecipeTypes.BREWING_STAND) ?: return

            val jeiRecipes = allBrewingRecipes.map { entry ->
                JeiBrewingStandRecipe(entry.value, entry.id)
            }

            registration.addRecipes(RecipeTypes.BREWING, jeiRecipes)
        }
    }
}