/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.mixin;

import com.cobblemon.mod.common.Rollable;
import com.cobblemon.mod.common.client.render.ClientPlayerIcon;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerRenderer.class)
public class PlayerRendererMixin {
    private static boolean disableRollableRenderDebug = false;

    @WrapMethod(method = "render")
    public void applyRotation(AbstractClientPlayer player, float f, float g, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, Operation<Integer> original) {
        if(player instanceof Rollable && !disableRollableRenderDebug) {
            Rollable rollable = (Rollable) player;
            if(rollable.shouldRoll()){
                poseStack.pushPose();
                Matrix4f transformationMatrix = new Matrix4f();
                Vector3f center = new Vector3f(0, player.getBbHeight()/2, 0);
                transformationMatrix.translate(center);

                //transformationMatrix.rotate(180f.toRadians() - (rollable.getYaw()).toRadians(), rollable.getUpVector(), transformationMatrix);
                transformationMatrix.rotate((float) Math.toRadians(-rollable.getPitch()), rollable.getLeftVector(), transformationMatrix);
                transformationMatrix.rotate((float) Math.toRadians(-rollable.getRoll()), rollable.getForwardVector(), transformationMatrix);

                transformationMatrix.translate(center.negate(new Vector3f()));
                poseStack.mulPose(transformationMatrix);
            }
        }

        original.call(player, f, g, poseStack, multiBufferSource, i);

        ClientPlayerIcon.Companion.onRenderPlayer(player);
        if(player instanceof Rollable && !disableRollableRenderDebug && ((Rollable) player).shouldRoll()){
            poseStack.popPose();
        }
    }
}
