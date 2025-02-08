/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.mixin;

import com.cobblemon.mod.common.client.render.ClientPlayerIcon;
import com.cobblemon.mod.common.client.render.player.PlayerRendererHook;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(PlayerRenderer.class)
public class PlayerRendererMixin {

    @WrapMethod(method = "render")
    public void applyRotation(AbstractClientPlayer player, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, Operation<Integer> original) {
        PlayerRendererHook.INSTANCE.beforeRender(player, entityYaw, partialTicks, poseStack, multiBufferSource, i);
        original.call(player, entityYaw, partialTicks, poseStack, multiBufferSource, i);
        PlayerRendererHook.INSTANCE.afterRender(player, entityYaw, partialTicks, poseStack, multiBufferSource, i);

        ClientPlayerIcon.Companion.onRenderPlayer(player);
    }
}
