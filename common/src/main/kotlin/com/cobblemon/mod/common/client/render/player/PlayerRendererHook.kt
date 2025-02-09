package com.cobblemon.mod.common.client.render.player

import com.cobblemon.mod.common.Rollable
import com.cobblemon.mod.common.api.riding.Rideable
import com.cobblemon.mod.common.client.entity.PokemonClientDelegate
import com.cobblemon.mod.common.util.math.geometry.toRadians
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.Minecraft
import net.minecraft.client.player.AbstractClientPlayer
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.util.Mth
import net.minecraft.world.phys.Vec3
import org.joml.Matrix3f
import org.joml.Matrix4f
import org.joml.Vector3d
import org.joml.Vector3f

object PlayerRendererHook {
    const val disableRollableRenderDebug: Boolean = false

    fun beforeRender(player: AbstractClientPlayer, entityYaw: Float, partialTicks: Float, poseStack: PoseStack, multiBufferSource: MultiBufferSource, i: Int){
        poseStack.pushPose()
        if (player.getVehicle() is Rideable) {
            val vehicle = player.getVehicle() as Rideable?
            val entity = vehicle!!.riding.entity
            val seatIndex = entity.getPassengers().indexOf(player)
            val seat = entity.seats.get(seatIndex)
            val delegate = entity.delegate as PokemonClientDelegate
            val locator = delegate.locatorStates.get(seat.locator)
            if (locator != null) {
                val pose = poseStack.last().pose()
                val locatorTranslation = locator.getOrigin().toVector3f()
                val cameraPos = Minecraft.getInstance().gameRenderer.mainCamera.position


                /*val oldEntityPos = Vector3d(entity.xOld, entity.yOld, entity.zOld).get(Vector3f())
                val lerpEntityPos = Vector3f(
                    Mth.lerp(partialTicks.toDouble(), oldEntityPos.x.toDouble(), entity.position().x).toFloat(),
                    Mth.lerp(partialTicks.toDouble(), oldEntityPos.y.toDouble(), entity.position().y).toFloat(),
                    Mth.lerp(partialTicks.toDouble(), oldEntityPos.z.toDouble(), entity.position().z).toFloat()
                )

                val oldPlayerPos = Vector3d(player.xOld, player.yOld, player.zOld).get(Vector3f())
                val lerpPlayerPos = Vector3f(
                    Mth.lerp(partialTicks.toDouble(), oldPlayerPos.x.toDouble(), player.position().x).toFloat(),
                    Mth.lerp(partialTicks.toDouble(), oldPlayerPos.y.toDouble(), player.position().y).toFloat(),
                    Mth.lerp(partialTicks.toDouble(), oldPlayerPos.z.toDouble(), player.position().z).toFloat()
                )*/


                val locatorOffset = locator.getOrigin().subtract(entity.position())
                val oldLocator = locatorOffset.add(Vec3(entity.x, entity.y, entity.z))
                val curLocator = oldLocator.lerp(locator.getOrigin(), partialTicks.toDouble())
                val curLocatorOffset = curLocator.subtract(entity.position())

                /*val lerpLocator = Vector3f(
                    (Mth.lerp(partialTicks.toDouble(), oldLocatorPos.x.toDouble(), locatorTranslation.x.toDouble()) - cameraPosition.x).toFloat(),
                    (Mth.lerp(partialTicks.toDouble(), oldLocatorPos.y.toDouble(), locatorTranslation.y.toDouble()) - cameraPosition.y).toFloat(),
                    (Mth.lerp(partialTicks.toDouble(), oldLocatorPos.z.toDouble(), locatorTranslation.z.toDouble()) - cameraPosition.z).toFloat()
                )*/

                val center = Vector3f(0f, entity.bbHeight/2, 0f)
                val entityPos = Vec3(entity.xOld, entity.yOld, entity.zOld).lerp(entity.position(), partialTicks.toDouble())
                val locatorOffsetToCenter = curLocatorOffset.toVector3f().sub(center, Vector3f())
                val rotationMatrix = Matrix3f()
                if (player is Rollable && player.shouldRoll()){
                    //Yaw is already built into the locator's so we ignore it here
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

                //Undo the position from the seat
                val playerOffset = entity.position().subtract(player.position()).toVector3f()
                //pose.translate(playerOffset)

                val seatOffset = seat.getOffset(entity.getCurrentPoseType()).toVector3f()

                val seatToCenter = center.sub(seatOffset, Vector3f())
                val orientation = (player as Rollable).orientation ?: Matrix3f().rotate(entityYaw.toRadians(), Vector3f(0f, 1f, 0f))
                pose.translate(orientation.transform(seatToCenter, Vector3f()).add(center).sub(Vector3f(0f, player.bbHeight/2, 0f)).negate())
            }
        }

        if (player is Rollable && player.shouldRoll() && !disableRollableRenderDebug) {
            val center = Vector3f(0f, player.bbHeight / 2, 0f)
            val transformationMatrix = Matrix4f()
            transformationMatrix.translate(center)

            //transformationMatrix.rotate(180f.toRadians() - (rollable.getYaw()).toRadians(), rollable.getUpVector(), transformationMatrix);
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
            transformationMatrix.translate(Vector3f(0f, 0.5f, 0f))
            poseStack.mulPose(transformationMatrix)
        } else {
            poseStack.last().pose().translate(Vector3f(0f, 0.5f, 0f))
        }
    }

    fun afterRender(player: AbstractClientPlayer, entityYaw: Float, partialTicks: Float, poseStack: PoseStack, multiBufferSource: MultiBufferSource, i: Int){
        poseStack.popPose()
    }
}