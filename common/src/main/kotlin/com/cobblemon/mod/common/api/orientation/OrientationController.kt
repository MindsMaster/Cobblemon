package com.cobblemon.mod.common.api.orientation

import com.cobblemon.mod.common.util.math.geometry.toDegrees
import com.cobblemon.mod.common.util.math.geometry.toRadians
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.Mth
import net.minecraft.world.entity.LivingEntity
import org.joml.Matrix3f
import org.joml.Quaternionf
import org.joml.Vector3f
import kotlin.math.asin

open class OrientationController(val entity: LivingEntity) {

    companion object {
        val FORWARDS: Vector3f = Vector3f(0.0f, 0.0f, -1.0f)
        val UP: Vector3f = Vector3f(0.0f, 1.0f, 0.0f)
        val LEFT: Vector3f = Vector3f(-1.0f, 0.0f, 0.0f)
    }

    open var active: Boolean = false

    var orientation: Matrix3f? = null
        private set

    var renderOrientations = mutableMapOf<ResourceLocation, Matrix3f>()

    /** Adding this simply because it irritates me seeing `getActive` when most of this is Java. */
    fun isActive() = active

    fun updateOrientation(fn: (Matrix3f) -> Matrix3f?) {
        if (!active) return
        if (orientation == null) {
            orientation = Matrix3f()
            rotate(entity.yRot - 180, entity.xRot, 0f)
        }
        orientation = fn(orientation!!)
        entity.yRot = yaw
        entity.xRot = pitch
    }

    fun reset() {
        orientation = null
        active = false
    }

    fun rotate(yaw: Float, pitch: Float, roll: Float) {
        rotateYaw(yaw)
        rotatePitch(pitch)
        rotateRoll(roll)
    }

    fun getRenderOrientation(resourceLocation: ResourceLocation): Quaternionf {
        val current = orientation ?: Matrix3f()
        val renderOrientation = renderOrientations[resourceLocation] ?: current
        renderOrientations[resourceLocation] = renderOrientation
        val renderQuat = Quaternionf().setFromUnnormalized(renderOrientation)
        val targetQuat  = Quaternionf().setFromUnnormalized(orientation)
        val dampingFactor = 0.15f
        renderQuat.slerp(targetQuat, dampingFactor)

        val newRenderOrientation = Matrix3f()
        renderQuat.get(newRenderOrientation)
        renderOrientations[resourceLocation] = newRenderOrientation
        return renderQuat
    }

    val forwardVector: Vector3f
        get() = orientation?.transform(FORWARDS, Vector3f()) ?: Vector3f(FORWARDS)

    val leftVector: Vector3f
        get() = orientation?.transform(LEFT, Vector3f()) ?: Vector3f(LEFT)

    val upVector : Vector3f
        get() = orientation?.transform(UP, Vector3f()) ?: Vector3f(UP)

    fun rotateYaw(yaw: Float) = updateOrientation { it.rotateY(-yaw.toRadians()) }
    fun rotatePitch(pitch: Float) = updateOrientation { it.rotateX(-pitch.toRadians()) }
    fun rotateRoll(roll: Float) = updateOrientation { it.rotateZ(-roll.toRadians()) }

    val yaw: Float
        get() = Mth.wrapDegrees(-FORWARDS.angleSigned(forwardVector, UP).toDegrees() + 180)

    val pitch: Float
        get() = Mth.wrapDegrees(-asin(forwardVector.y).toDegrees())

    val roll: Float
        get() = Mth.wrapDegrees(-upVector.angleSigned(UP, forwardVector).toDegrees())

}
