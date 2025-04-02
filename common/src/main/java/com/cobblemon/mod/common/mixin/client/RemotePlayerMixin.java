package com.cobblemon.mod.common.mixin.client;

import com.cobblemon.mod.common.RemotePlayerOrientation;
import net.minecraft.client.player.RemotePlayer;
import org.joml.Matrix3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(RemotePlayer.class)
public class RemotePlayerMixin implements RemotePlayerOrientation {

    @Unique
    Matrix3f cobblemon$lastOrientation;

    @Unique
    Matrix3f cobblemon$renderOrientation;


    @Override
    public Matrix3f getLastOrientation() {
        return cobblemon$lastOrientation;
    }

    @Override
    public void setLastOrientation(Matrix3f orientation) {
        cobblemon$lastOrientation = orientation;
    }


    @Override
    public Matrix3f getRenderOrientation() {
        return cobblemon$renderOrientation;
    }

    @Override
    public void setRenderOrientation(Matrix3f orientation) {
        cobblemon$renderOrientation = orientation;
    }

}
