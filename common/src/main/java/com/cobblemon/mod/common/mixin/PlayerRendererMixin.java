/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.mixin;

import com.cobblemon.mod.common.Rollable;
import com.cobblemon.mod.common.api.riding.Rideable;
import com.cobblemon.mod.common.api.riding.Seat;
import com.cobblemon.mod.common.client.entity.PokemonClientDelegate;
import com.cobblemon.mod.common.client.render.ClientPlayerIcon;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.cobblemon.mod.common.client.render.MatrixWrapper;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.mojang.authlib.minecraft.client.MinecraftClient;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.loader.impl.lib.sat4j.core.Vec;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerRenderer.class)
public class PlayerRendererMixin {
    private static boolean disableRollableRenderDebug = false;

    @WrapMethod(method = "render")
    public void applyRotation(AbstractClientPlayer player, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, Operation<Integer> original) {
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
        if (player.getVehicle() instanceof Rideable) {
            Rideable vehicle = (Rideable) player.getVehicle();
            PokemonEntity entity = vehicle.getRiding().getEntity();
            int seatIndex = entity.getPassengers().indexOf(player);
            Seat seat = entity.getSeats().get(seatIndex);
            PokemonClientDelegate delegate = (PokemonClientDelegate) entity.getDelegate();
            MatrixWrapper locator = delegate.getLocatorStates().get(seat.getLocator());
            if (locator != null) {
                Matrix4f pose = poseStack.last().pose();
                Vector3f locatorTranslation = locator.getOrigin().toVector3f();
                Vector3f cameraPosition = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition().toVector3f();

                Vector3f locatorOffset = new Vector3f(locatorTranslation).sub(entity.position().toVector3f());
                Vector3f oldEntityPos = new Vector3d(entity.xOld, entity.yOld, entity.zOld).get(new Vector3f());
                Vector3f oldLocatorPos = oldEntityPos.add(locatorOffset);

                pose.setTranslation(
                    Mth.lerp(partialTicks, oldLocatorPos.x, locatorTranslation.x) - cameraPosition.x,
                    Mth.lerp(partialTicks, oldLocatorPos.y, locatorTranslation.y) - cameraPosition.y,
                    Mth.lerp(partialTicks, oldLocatorPos.z, locatorTranslation.z) - cameraPosition.z
                );
                //Move down half a block to compensate for the sitting player pose
                pose.translate(0F, -0.5F, 0F);
            }
        }

        original.call(player, entityYaw, partialTicks, poseStack, multiBufferSource, i);

        ClientPlayerIcon.Companion.onRenderPlayer(player);
        if(player instanceof Rollable && !disableRollableRenderDebug && ((Rollable) player).shouldRoll()){
            poseStack.popPose();
        }
    }
}
