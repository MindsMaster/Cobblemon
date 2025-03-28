/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.fabric.utils;

import com.cobblemon.mod.common.item.crafting.CookingPotRecipe;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.function.Supplier;

import static com.cobblemon.mod.common.util.MiscUtilsKt.cobblemonResource;

public class CobblemonRecipeType {

    public static final LazyRegistrar<RecipeType<?>> RECIPE_TYPES = LazyRegistrar.create(BuiltInRegistries.RECIPE_TYPE, "cobblemon");

    public static final Supplier<RecipeType<CookingPotRecipe>> COOKING_POT_RECIPE = RECIPE_TYPES.register("cooking_pot", () -> registerRecipeType("cooking_pot"));

    public static <T extends Recipe<?>> RecipeType<T> registerRecipeType(final String identifier) {
        return new RecipeType<>()
        {
            public String toString() {
                return cobblemonResource(identifier).toString();
            }
        };
    }

}
