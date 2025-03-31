/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.mixin;

import com.cobblemon.mod.common.CobblemonNetwork;
import com.cobblemon.mod.common.OrientationControllable;
import com.cobblemon.mod.common.api.orientation.OrientationController;
import com.cobblemon.mod.common.mixin.accessor.ChunkMapAccessor;
import com.cobblemon.mod.common.mixin.accessor.TrackedEntityAccessor;
import com.cobblemon.mod.common.net.messages.client.orientation.S2CUpdateOrientationPacket;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerPlayerConnection;
import net.minecraft.world.entity.Entity;
import org.joml.Matrix3f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;
import java.util.function.Consumer;

@Mixin(ServerEntity.class)
public abstract class ServerEntityMixin {

    @Shadow @Final private Consumer<Packet<?>> broadcast;
    @Shadow @Final private Entity entity;
    @Unique
    private Matrix3f cobblemon$lastSentOrientation;

    @Inject(method = "sendChanges", at = @At("TAIL"))
    private void cobblemon$sendOrientationChanges(CallbackInfo ci) {
        if (!(this instanceof OrientationControllable controllable)) return;


        OrientationController controller = controllable.getOrientationController();
        Matrix3f currOrientation = controller.getOrientation();

        if (currOrientation == null || currOrientation.equals(cobblemon$lastSentOrientation)) return;
        cobblemon$lastSentOrientation = new Matrix3f(currOrientation);

        if (!(entity.level() instanceof ServerLevel level)) return;

        ChunkMap chunkMap = level.getChunkSource().chunkMap;
        Int2ObjectMap<?> entityMap = ((ChunkMapAccessor) chunkMap).getEntityMap();
        Object tracked = entityMap.get(entity.getId());
        if (!(tracked instanceof TrackedEntityAccessor tracker)) return;

        Set<ServerPlayerConnection> seenBy = tracker.getSeenBy();
        for (ServerPlayerConnection conn : seenBy) {
            ServerPlayer player = conn.getPlayer();
            if (player == entity) continue;
            CobblemonNetwork.INSTANCE.sendPacketToPlayer(player, new S2CUpdateOrientationPacket(currOrientation, entity.getId()));
        }


        /*
        OrientationController controller = controllable.getOrientationController();
        Matrix3f currOrientation = controller.getOrientation();

        if(currOrientation != cobblemon$lastSentOrientation) {
            cobblemon$lastSentOrientation = new Matrix3f(currOrientation);

            //this.broadcast.accept(new S2CUpdateOrientationPacket(currOrientation,this.entity.getId()))

            if (!(entity.level() instanceof ServerLevel level)) return;

            ChunkMap chunkMap = level.getChunkSource().chunkMap;
            Long2ObjectMap<ChunkMap.TrackedEntity> entityMap = ((ChunkMapAccessor) chunkMap).getEntityMap();
            TrackedEntity trackedEntity = entityMap.get(entity.getId());
            if (trackedEntity == null) return;
            Set<ServerPlayerConnection> seenBy = ((TrackedEntityAccessor) trackedEntity).getSeenBy();
            for (ServerPlayerConnection conn : seenBy) {
                ServerPlayer player = conn.getPlayer();
                if (player == entity) continue; // optional safety filter
                CobblemonNetwork.sendPacketToPlayer(player, new S2CUpdateOrientationPacket(orientation, entity.getId()));


        }

         */
    }


}
