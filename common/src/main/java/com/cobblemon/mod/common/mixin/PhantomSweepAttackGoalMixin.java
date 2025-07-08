/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.mixin;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Phantom;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Accesses the synthetic `this$0` field, which refers to the outer Phantom instance.
 */


@Mixin(targets = "net.minecraft.world.entity.monster.Phantom$PhantomSweepAttackGoal")
public abstract class PhantomSweepAttackGoalMixin extends Goal {

    @Unique
    private Phantom phantomSelf;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void onInit(Phantom phantom, CallbackInfo ci) {
        this.phantomSelf = phantom;
    }

    @Inject(method = "canContinueToUse", at = @At("RETURN"), cancellable = true)
    private void modifyCanContinueToUse(CallbackInfoReturnable<Boolean> cir) {
        // Access the outer Phantom instance using our accessor
        Phantom phantom = this.phantomSelf;
        System.out.println("before null check");
        if(phantom == null) return;
        System.out.println("AFTER null check");


        if (cir.getReturnValue()) {
            System.out.println("AFTER return check");
            var nearbyScaryPokemon = phantom.level().getEntitiesOfClass(
                    PokemonEntity.class,
                    phantom.getBoundingBox().inflate(16.0),
                    entity -> entity.getBehaviour().getEntityInteract().getAvoidedByPhantom()
            );
            nearbyScaryPokemon.forEach(PokemonEntity::cry);
            System.out.println(nearbyScaryPokemon);
            if (!nearbyScaryPokemon.isEmpty()) {
                cir.setReturnValue(false);
            }
        }
    }
}

