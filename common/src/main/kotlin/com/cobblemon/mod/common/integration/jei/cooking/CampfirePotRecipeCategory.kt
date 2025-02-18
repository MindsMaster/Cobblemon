/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.integration.jei.cooking

import com.cobblemon.mod.common.CobblemonItems
import com.cobblemon.mod.common.block.entity.CampfireBlockEntity.Companion.CRAFTING_GRID_WIDTH
import com.cobblemon.mod.common.client.gui.cookingpot.CookingPotRecipeBase
import com.cobblemon.mod.common.client.gui.cookingpot.CookingPotScreen
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.lang
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder
import mezz.jei.api.gui.drawable.IDrawable
import mezz.jei.api.helpers.IGuiHelper
import mezz.jei.api.recipe.IFocusGroup
import mezz.jei.api.recipe.RecipeIngredientRole
import mezz.jei.api.recipe.RecipeType
import mezz.jei.api.recipe.category.IRecipeCategory
import mezz.jei.api.registration.IRecipeCategoryRegistration
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.crafting.Ingredient

class CampfirePotRecipeCategory(registration: IRecipeCategoryRegistration) : IRecipeCategory<CookingPotRecipeBase> {

    companion object {
        val RECIPE_TYPE = RecipeType.create("cobblemon", "campfire_pot_recipe", CookingPotRecipeBase::class.java)!!
        val CAMPFIRE_POT_TEXTURE: ResourceLocation = cobblemonResource("textures/gui/jei/campfire_pot.png")
        const val TEXTURE_WIDTH = 146
        const val TEXTURE_HEIGHT = 59
        const val WIDTH = 146
        const val HEIGHT = 59
    }

    val guiHelper: IGuiHelper = registration.jeiHelpers.guiHelper
    val campfirePotBackground: IDrawable = guiHelper.drawableBuilder(CAMPFIRE_POT_TEXTURE, 0, 0, WIDTH, HEIGHT)
        .setTextureSize(TEXTURE_WIDTH, TEXTURE_HEIGHT)
        .build()
    val campfirePotIcon: IDrawable = guiHelper.createDrawableItemStack(CobblemonItems.CAMPFIRE_POT_BLACK.defaultInstance)

    override fun getRecipeType(): RecipeType<CookingPotRecipeBase?>? = RECIPE_TYPE
    override fun getTitle(): Component? = lang("container.campfire_pot")
    override fun getBackground(): IDrawable? = campfirePotBackground
    override fun getIcon(): IDrawable? = campfirePotIcon

    override fun setRecipe(
        builder: IRecipeLayoutBuilder,
        recipe: CookingPotRecipeBase,
        focuses: IFocusGroup
    ) {
        recipe.ingredients.forEachIndexed { index, ingredient ->
            val column = index / CRAFTING_GRID_WIDTH
            val row = index % CRAFTING_GRID_WIDTH

            val x = 16 + row * 18
            val y = 1 + column * 18

            builder.addSlot(RecipeIngredientRole.INPUT, x, y).addIngredients(ingredient)
        }

        val registryAccess = Minecraft.getInstance().level?.registryAccess() ?: throw IllegalStateException("Registry access not found")
        builder.addSlot(RecipeIngredientRole.OUTPUT, 111, 38).addIngredients(Ingredient.of(recipe.getResultItem(registryAccess)))
    }
}