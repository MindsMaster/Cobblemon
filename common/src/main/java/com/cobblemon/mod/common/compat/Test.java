/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.compat;

import net.minecraft.client.RecipeBookCategories;
import net.minecraft.world.inventory.RecipeBookType;

public class Test {

    public static final RecipeBookType RECIPE_TYPE_COOKING = ClassTinkerers.getEnum(RecipeBookType.class, "COOKING_POT");
    public static final RecipeBookCategories COOKING_POT_MISC_CATEGORY = ClassTinkerers.getEnum(RecipeBookCategories.class, "COOKING_POT_MISC");


}
