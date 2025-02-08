package com.cobblemon.mod.common.client.render.player

import com.cobblemon.mod.common.Rollable
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.player.AbstractClientPlayer
import net.minecraft.client.renderer.MultiBufferSource
import org.joml.Matrix4f
import org.joml.Vector3f

object PlayerRendererHook {
    const val disableRollableRenderDebug: Boolean = false

    fun beforeRender(player: AbstractClientPlayer, entityYaw: Float, partialTicks: Float, poseStack: PoseStack, multiBufferSource: MultiBufferSource, i: Int){
        poseStack.pushPose()

        if (player is Rollable && player.shouldRoll() && !disableRollableRenderDebug) {
            val rollable = player as Rollable
            val center = Vector3f(0f, player.bbHeight / 2, 0f)
            val transformationMatrix = Matrix4f()
            transformationMatrix.translate(center)

            //transformationMatrix.rotate(180f.toRadians() - (rollable.getYaw()).toRadians(), rollable.getUpVector(), transformationMatrix);
            transformationMatrix.rotate(
                Math.toRadians(-rollable.getPitch().toDouble()).toFloat(),
                rollable.getLeftVector(),
                transformationMatrix
            )
            transformationMatrix.rotate(
                Math.toRadians(-rollable.getRoll().toDouble()).toFloat(),
                rollable.getForwardVector(),
                transformationMatrix
            )

            transformationMatrix.translate(center.negate(Vector3f()))
            poseStack.mulPose(transformationMatrix)
        }
    }

    fun afterRender(player: AbstractClientPlayer, entityYaw: Float, partialTicks: Float, poseStack: PoseStack, multiBufferSource: MultiBufferSource, i: Int){
        poseStack.popPose()
    }
}