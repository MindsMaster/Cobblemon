/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.mixin.client;

import com.cobblemon.mod.common.Rollable;
import com.cobblemon.mod.common.api.riding.Rideable;
import com.cobblemon.mod.common.api.riding.Seat;
import com.cobblemon.mod.common.client.entity.PokemonClientDelegate;
import com.cobblemon.mod.common.client.render.MatrixWrapper;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraMixin {
    @Shadow @Final private static float DEFAULT_CAMERA_DISTANCE;

    @Shadow private Entity entity;
    @Shadow @Final private Quaternionf rotation;
    @Shadow @Final private Vector3f forwards;
    @Shadow @Final private Vector3f up;
    @Shadow @Final private Vector3f left;
    @Shadow private float xRot;
    @Shadow private float yRot;

    @Shadow public abstract float getXRot();
    @Shadow public abstract float getYRot();

    @Shadow private Vec3 position;
    @Shadow private float eyeHeight;
    @Shadow private float eyeHeightOld;

    @Unique private float returnTimer = 0;
    @Unique private float rollAngleStart = 0;
    @Unique Minecraft minecraft = Minecraft.getInstance();

    @Unique boolean disableRollableCameraDebug = false;

    @Inject(method = "setRotation", at = @At("HEAD"), cancellable = true)
    public void open_camera$setRotation(float f, float g, CallbackInfo ci) {
        if (!(this.entity instanceof Rollable rollable) || disableRollableCameraDebug) return;
        if (!rollable.shouldRoll() && rollable.getOrientation() != null) {
            if(this.returnTimer < 1) {
                //Rotation is taken from entity since we no longer handle mouse ourselves
                //Stops a period of time when you can't input anything.
                rollable.getOrientation().set(
                        new Matrix3f()
                                .rotateY((float) Math.toRadians(180 - this.entity.getYRot()))
                                .rotateX((float) Math.toRadians(-this.entity.getXRot()))
                );
                if(rollAngleStart == 0){
                    this.returnTimer = 1;
                    rollable.clearRotation();
                    return;
                }
                rollable.getOrientation().rotateZ((float) Math.toRadians(-rollAngleStart*(1-returnTimer)));
                applyRotation();
                this.returnTimer += .05F;
                ci.cancel();
            } else {
                this.returnTimer = 1;
                rollable.clearRotation();
            }
            return;
        }
        if (rollable.getOrientation() == null) return;
        applyRotation();

        this.returnTimer = 0;
        this.rollAngleStart = rollable.getRoll();
        ci.cancel();
    }

    //If you want to move this to a delagate you need an AW for position
    @WrapOperation(method = "setup", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;setPosition(DDD)V"))
    public void positionCamera(Camera instance, double x, double y, double z, Operation<Void> original){
        Entity entity = instance.getEntity();
        Entity vehicle = entity.getVehicle();

        if(vehicle instanceof Rideable){
            //RidingCameraDelagate.INSTANCE.positionCamera(instance, x, y, z, original);
            if(!(vehicle instanceof PokemonEntity)){
                original.call(instance, x, y, z);
                return;
            }
            PokemonEntity pokemon = (PokemonEntity) vehicle;

            int seatIndex = pokemon.getPassengers().indexOf(entity);
            Seat seat = pokemon.getSeats().get(seatIndex);
            PokemonClientDelegate delegate = (PokemonClientDelegate) pokemon.getDelegate();
            MatrixWrapper locator = delegate.getLocatorStates().get(seat.getLocator());
            if(locator == null){
                original.call(instance, x, y, z);
                return;
            }

            Vec3 locatorOffset = new Vec3(locator.getMatrix().getTranslation(new Vector3f()));

            Vec3 entityPos = new Vec3(
                    Mth.lerp(instance.getPartialTickTime(), pokemon.xOld, pokemon.getX()),
                    Mth.lerp(instance.getPartialTickTime(), pokemon.yOld, pokemon.getY()),
                    Mth.lerp(instance.getPartialTickTime(), pokemon.zOld, pokemon.getZ())
            );

            Rollable rollable = (Rollable) entity;
            float currEyeHeight = Mth.lerp(instance.getPartialTickTime(), eyeHeightOld, eyeHeight);
            Matrix3f orientation = rollable.shouldRoll() && rollable.getOrientation() != null ? rollable.getOrientation() : new Matrix3f();
            Vec3 rotatedEyeHeight = new Vec3(orientation.transform(new Vector3f(0f, currEyeHeight, 0f)));

            position = locatorOffset.add(entityPos).add(rotatedEyeHeight);
        } else {
            original.call(instance, x, y, z);
        }
    }

    @Unique
    private void applyRotation(){
        if (!(this.entity instanceof Rollable rollable) || rollable.getOrientation() == null) return;
        var newRotation = rollable.getOrientation().normal(new Matrix3f()).getNormalizedRotation(new Quaternionf());
        if (this.minecraft.options.getCameraType().isMirrored()) {
            newRotation.rotateY((float)Math.toRadians(180));
        }
        this.rotation.set(newRotation);
        this.xRot = rollable.getPitch();
        this.yRot = rollable.getYaw();
        this.forwards.set(rollable.getForwardVector());
        this.up.set(rollable.getUpVector());
        this.left.set(rollable.getLeftVector());
    }
}
