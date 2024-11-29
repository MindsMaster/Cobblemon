package com.cobblemon.mod.common.mixin;

import net.minecraft.client.ClientRecipeBook;
import net.minecraft.client.gui.screens.recipebook.RecipeBookTabButton;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(RecipeBookTabButton.class)
public class RecipeBookTabButtonMixin {

    @Inject(method = "updateVisibility", at = @At("HEAD"), cancellable = true)
    private void customUpdateVisibility(ClientRecipeBook recipeBook, CallbackInfoReturnable<Boolean> cir) {
        RecipeBookTabButton instance = (RecipeBookTabButton) (Object) this;
        List<RecipeCollection> list = recipeBook.getCollection(instance.getCategory());
        boolean visible = false;
        if (list != null) {
            for (RecipeCollection recipeCollection : list) {
                if (recipeCollection.hasKnownRecipes() && recipeCollection.hasFitting()) {
                    visible = true;
                    break;
                }
            }
        }
        instance.visible = visible;
        cir.setReturnValue(visible);
    }
}