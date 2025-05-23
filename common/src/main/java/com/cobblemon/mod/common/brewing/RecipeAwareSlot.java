package com.cobblemon.mod.common.brewing;

import net.minecraft.world.item.crafting.RecipeManager;

public interface RecipeAwareSlot {
	void setRecipeManager(RecipeManager recipeManager);

	RecipeManager getRecipeManager();
}
