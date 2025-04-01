package com.cobblemon.mod.common;

import org.joml.Matrix3f;

public interface RemotePlayerOrientation {
    Matrix3f getLastOrientation();
    void setLastOrientation(Matrix3f orientation);
}
