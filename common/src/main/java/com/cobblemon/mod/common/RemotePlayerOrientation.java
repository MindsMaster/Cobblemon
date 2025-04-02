package com.cobblemon.mod.common;

import org.joml.Matrix3f;

public interface RemotePlayerOrientation {
    Matrix3f getLastOrientation();
    void setLastOrientation(Matrix3f orientation);
    Matrix3f getRenderOrientation();
    void setRenderOrientation(Matrix3f orientation);
}
