/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.mixin;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.ai.EntityBehaviour;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Phantom;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
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
        if(phantom == null) return;

        if (cir.getReturnValue()) {
            var nearbyScaryPokemon = phantom.level().getEntitiesOfClass(
                    PokemonEntity.class,
                    phantom.getBoundingBox().inflate(16.0),
                    entity -> entity.getBeamMode() == 0 && entity.getBehaviour().getEntityInteract().getAvoidedByPhantom()
            );
            nearbyScaryPokemon.forEach(PokemonEntity::cry);
            if (!nearbyScaryPokemon.isEmpty()) {
                cir.setReturnValue(false);
            } else {
                // Check for shoulder mounted pokemon
                var nearbyPlayersWithScaryShoulders = phantom.level().getEntitiesOfClass(
                        ServerPlayer.class,
                        phantom.getBoundingBox().inflate(16.0),
                        entity -> EntityBehaviour.Companion.hasPhantomFearedShoulderMount((ServerPlayer)entity)
                );
                if (!nearbyPlayersWithScaryShoulders.isEmpty()) {
                    // TODO: Figure out how to make a shoulder mounted pokemon emit a cry
                    cir.setReturnValue(false);
                }
            }
        }
    }
}

