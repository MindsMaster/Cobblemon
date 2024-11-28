package com.cobblemon.mod.common.mixin;

import com.cobblemon.mod.common.compat.Test;
import com.google.common.collect.ImmutableList;
import net.minecraft.client.RecipeBookCategories;
import net.minecraft.world.inventory.RecipeBookType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(RecipeBookCategories.class)
public class RecipeBookCategoriesMixin {

    @Final
    @Mutable
    @Shadow
    private static RecipeBookCategories[] $VALUES;

    @Invoker("<init>")
    private static RecipeBookCategories cobblemon$createCategory(String name, int ordinal, ItemStack... icons) {
        throw new AssertionError();
    }

    @Inject(
            method = "<clinit>",
            at = @At("TAIL")
    )
    private static void cobblemon$addRecipeBookType(CallbackInfo ci) {
        ArrayList<RecipeBookCategories> types = new ArrayList<>(List.of($VALUES));
        types.add(cobblemon$createCategory("COOKING_POT_MISC", $VALUES.length, new ItemStack(Items.BARRIER)));
        $VALUES = types.toArray(RecipeBookCategories[]::new);
    }

    @Inject(method = "getCategories", at = @At("HEAD"), cancellable = true)
    private static void modifyCategories(RecipeBookType recipeBookType, CallbackInfoReturnable<List<RecipeBookCategories>> cir) {
        if (recipeBookType == Test.RECIPE_TYPE_COOKING) {
            List var10000 = ImmutableList.of(Test.COOKING_POT_MISC_CATEGORY);
            cir.setReturnValue(var10000);
            return;
        }
    }
}
