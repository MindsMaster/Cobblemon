package com.cobblemon.mod.common.mixin;

import com.cobblemon.mod.common.compat.Test;
import net.minecraft.stats.RecipeBookSettings;
import net.minecraft.world.inventory.RecipeBookType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(RecipeBookSettings.class)
public class RecipeBookSettingsMixin {


    @Shadow @Final public Map<RecipeBookType, RecipeBookSettings.TypeSettings> states;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void cobblemon$init(CallbackInfo ci) {
        states.put(Test.RECIPE_TYPE_COOKING, new RecipeBookSettings.TypeSettings(true, true));
    }

}
