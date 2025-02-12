package com.cobblemon.mod.common.client.render.player

import com.cobblemon.mod.common.Rollable
import com.cobblemon.mod.common.api.riding.Rideable
import com.cobblemon.mod.common.client.entity.PokemonClientDelegate
import com.cobblemon.mod.common.util.math.geometry.toRadians
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.player.AbstractClientPlayer
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.util.Mth
import net.minecraft.world.phys.Vec3
import org.joml.Matrix3f
import org.joml.Matrix4f
import org.joml.Vector3f

object PlayerRendererHook {
    const val disableRollableRenderDebug: Boolean = false

    fun beforeRender(player: AbstractClientPlayer, entityYaw: Float, partialTicks: Float, poseStack: PoseStack, multiBufferSource: MultiBufferSource, i: Int){
        poseStack.pushPose()
        if(player.vehicle !is Rideable) return

        val vehicle = player.vehicle as Rideable?
        val entity = vehicle!!.riding.entity
        val seatIndex = entity.passengers.indexOf(player)
        val seat = entity.seats[seatIndex]
        val delegate = entity.delegate as PokemonClientDelegate
        val locator = delegate.locatorStates[seat.locator]

        //Positions player
        if (locator != null) {
            val pose = poseStack.last().pose()
            val locatorOffset = locator.matrix.getTranslation(Vector3f())

            val center = Vector3f(0f, entity.bbHeight/2, 0f)
            val locatorOffsetToCenter = locatorOffset.sub(center, Vector3f())

            val rotationMatrix = Matrix3f()
            if (player is Rollable && player.shouldRoll()){
                //Yaw is already built into the locator's so we ignore it her
                //Rotate matrix by Player's pitch
                rotationMatrix.rotate(
                    player.pitch.toRadians(),
                    player.getLeftVector()
                )
                //Rotate matrix by Player's roll
                rotationMatrix.rotate(
                    player.roll.toRadians(),
                    player.getForwardVector(),
                )
            }

            val rotatedOffset = rotationMatrix.transform(locatorOffsetToCenter, Vector3f()).add(center).sub(Vector3f(0f, player.bbHeight/2, 0f))
            pose.translate(rotatedOffset)

            //Undo seat position
            val playerPos = Vec3(
                Mth.lerp(partialTicks.toDouble(), player.xOld, player.x),
                Mth.lerp(partialTicks.toDouble(), player.yOld, player.y),
                Mth.lerp(partialTicks.toDouble(), player.zOld, player.z),
            )

            val entityPos = Vec3(
                Mth.lerp(partialTicks.toDouble(), entity.xOld, entity.x),
                Mth.lerp(partialTicks.toDouble(), entity.yOld, entity.y),
                Mth.lerp(partialTicks.toDouble(), entity.zOld, entity.z),
            )

            pose.translate(playerPos.subtract(entityPos).toVector3f().negate())
        }

        //Rotates player
        if (player is Rollable && player.shouldRoll() && !disableRollableRenderDebug) {
            val center = Vector3f(0f, player.bbHeight / 2, 0f)
            val transformationMatrix = Matrix4f()
            transformationMatrix.translate(center)

            //Player already rotates yaw so we ignore it here
            transformationMatrix.rotate(
                Math.toRadians(player.pitch.toDouble()).toFloat(),
                player.getLeftVector(),
                transformationMatrix
            )
            transformationMatrix.rotate(
                Math.toRadians(player.roll.toDouble()).toFloat(),
                player.getForwardVector(),
                transformationMatrix
            )

            transformationMatrix.translate(center.negate(Vector3f()))
            poseStack.mulPose(transformationMatrix)
        }
        poseStack.translate(0f, 0.5f, 0f)
    }

    fun afterRender(player: AbstractClientPlayer, entityYaw: Float, partialTicks: Float, poseStack: PoseStack, multiBufferSource: MultiBufferSource, i: Int){
        poseStack.popPose()
    }
}