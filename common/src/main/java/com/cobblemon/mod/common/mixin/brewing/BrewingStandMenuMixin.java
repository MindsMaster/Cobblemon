package com.cobblemon.mod.common.mixin.brewing;

import com.cobblemon.mod.common.item.crafting.brewingstand.BrewingStandRecipe;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.BrewingStandMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BrewingStandMenu.class)
public abstract class BrewingStandMenuMixin {

    /*
        Slot layout in BrewingStandMenu:
        0-2: Bottle slots
        3: Ingredient slot
        4: Fuel slot
        5-31: Player inventory
        32-41: Hotbar
    */

	@Shadow
	@Final
	private Slot ingredientSlot;
	
	private static final int BOTTLE_SLOT_START = 0;
	private static final int BOTTLE_SLOT_END = 3;

	private static final int INGREDIENT_SLOT_INDEX = 3;
	private static final int FUEL_SLOT_INDEX = 4;

	private static final int PLAYER_INV_START = 5;
	private static final int HOTBAR_END = 41;

	@Inject(method = "quickMoveStack", at = @At("HEAD"), cancellable = true)
	private void cobblemon$quickMoveStack(Player player, int index, CallbackInfoReturnable<ItemStack> cir) {
		BrewingStandMenu menu = (BrewingStandMenu) (Object) this;
		Slot slot = menu.slots.get(index);
		ItemStack itemStack = slot.getItem();

		if (itemStack.isEmpty()) {
			return;
		}

		ItemStack originalStack = itemStack.copy();

		boolean moved = false;

		if (index < PLAYER_INV_START) {
			// Brewing stand → player inventory
			moved = menu.moveItemStackTo(itemStack, PLAYER_INV_START, HOTBAR_END, true);
		} else {
			// Inventory → brewing stand

			if (!moved && BrewingStandMenu.FuelSlot.mayPlaceItem(itemStack)) {
				moved = menu.moveItemStackTo(itemStack, FUEL_SLOT_INDEX, FUEL_SLOT_INDEX + 1, false);
			}

			if (!moved && (BrewingStandRecipe.Companion.isBottle(itemStack, player.level()) ||
					BrewingStandMenu.PotionSlot.mayPlaceItem(itemStack))) {
				moved = menu.moveItemStackTo(itemStack, BOTTLE_SLOT_START, BOTTLE_SLOT_END, false);
			}
			
			if (BrewingStandRecipe.Companion.isInput(itemStack, player.level()) ||
					this.ingredientSlot.mayPlace(itemStack)) {
				moved = menu.moveItemStackTo(itemStack, INGREDIENT_SLOT_INDEX, INGREDIENT_SLOT_INDEX + 1, false);
			}
		}

		if (!moved) {
			cir.setReturnValue(ItemStack.EMPTY);
			return;
		}

		if (itemStack.isEmpty()) {
			slot.set(ItemStack.EMPTY);
		} else {
			slot.setChanged();
		}

		cir.setReturnValue(originalStack);
	}
}
