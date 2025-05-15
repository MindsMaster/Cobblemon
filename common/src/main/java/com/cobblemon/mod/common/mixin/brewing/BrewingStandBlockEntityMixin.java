/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.mixin.brewing;

import com.cobblemon.mod.common.CobblemonRecipeTypes;
import com.cobblemon.mod.common.item.crafting.brewingstand.BrewingStandInput;
import com.cobblemon.mod.common.item.crafting.brewingstand.BrewingStandRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Optional;
@Mixin(BrewingStandBlockEntity.class)
public class BrewingStandBlockEntityMixin {

	@Shadow
	private NonNullList<ItemStack> items;

	@Shadow
	int brewTime;

	@Shadow
	int fuel;

	@Inject(
			method = "serverTick",
			at = @At("HEAD"),
			cancellable = true
	)
	private static void cobblemon$serverTick(Level level, BlockPos pos, BlockState state, BrewingStandBlockEntity blockEntity, CallbackInfo ci) {
		BrewingStandBlockEntityMixin self = (BrewingStandBlockEntityMixin) (Object) blockEntity;
		assert self != null;
		NonNullList<ItemStack> items = self.items;

		BrewingStandRecipe customRecipe = self.fetchBrewingRecipe(level);
		if (customRecipe == null) {
			return;
		}

		ItemStack ingredientStack = items.get(3);
		boolean isBrewing = self.brewTime > 0;

		if (isBrewing) {
			self.brewTime--;
			if (self.brewTime == 0) {
				for (int i = 0; i < 3; i++) {
					if (!items.get(i).isEmpty()) {
						items.set(i, customRecipe.getResult().copy());
					}
				}
				ingredientStack.shrink(1);
			}
		} else if (self.fuel > 0) {
			self.fuel--;
			self.brewTime = 400;
		}

		blockEntity.setChanged();
		ci.cancel();
	}

	private BrewingStandRecipe fetchBrewingRecipe(Level level) {
		ItemStack ingredient = items.get(3);
		List<ItemStack> bottles = items.subList(0, 3);

		boolean allBottlesEmpty = true;
		for (ItemStack bottle : bottles) {
			if (!bottle.isEmpty()) {
				allBottlesEmpty = false;
				break;
			}
		}

		if (ingredient.isEmpty() || allBottlesEmpty) {
			return null;
		}

		BrewingStandInput input = new BrewingStandInput(ingredient, bottles);
		RecipeManager recipeManager = level.getRecipeManager();

		Optional<RecipeHolder<BrewingStandRecipe>> recipeHolder =
				recipeManager.getRecipeFor(CobblemonRecipeTypes.INSTANCE.getBREWING_STAND(), input, level);

		return recipeHolder.map(RecipeHolder::value).orElse(null);
	}
}