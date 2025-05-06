package com.cobblemon.mod.common.client.render.block

import com.cobblemon.mod.common.block.entity.BrewingStandBlockEntity
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider
import net.minecraft.world.item.ItemDisplayContext
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items

class BrewingStandBlockEntityRenderer(ctx: BlockEntityRendererProvider.Context) :
    BlockEntityRenderer<BrewingStandBlockEntity> {

    override fun render(
        blockEntity: BrewingStandBlockEntity,
        partialTick: Float,
        poseStack: PoseStack,
        bufferSource: MultiBufferSource,
        packedLight: Int,
        packedOverlay: Int
    ) {
        poseStack.pushPose()

        poseStack.translate(0.5, 0.0, 0.5)

        val itemRenderer = Minecraft.getInstance().itemRenderer
        val brewingStandItem = ItemStack(Items.BREWING_STAND)

        itemRenderer.renderStatic(
            brewingStandItem,
            ItemDisplayContext.GROUND,
            packedLight,
            packedOverlay,
            poseStack,
            bufferSource,
            blockEntity.level,
            0
        )

        poseStack.popPose()
    }
}
