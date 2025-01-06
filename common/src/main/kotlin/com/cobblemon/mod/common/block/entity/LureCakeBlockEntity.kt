package com.cobblemon.mod.common.block.entity

import com.cobblemon.mod.common.CobblemonBlockEntities
import com.cobblemon.mod.common.CobblemonItemComponents
import com.cobblemon.mod.common.api.fishing.FishingBait
import com.cobblemon.mod.common.item.components.CookingComponent
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.core.BlockPos
import net.minecraft.core.Holder
import net.minecraft.core.HolderLookup
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtOps
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientGamePacketListener
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState
import java.util.stream.Stream

class LureCakeBlockEntity(
        pos: BlockPos,
        state: BlockState
) : BlockEntity(CobblemonBlockEntities.LURE_CAKE, pos, state) {

    var cookingComponent: CookingComponent? = null
        private set

    /**
     * Initializes the `CookingComponent` data from the given `ItemStack` when placed.
     */
    fun initializeFromItemStack(itemStack: ItemStack) {
        cookingComponent = itemStack.get(CobblemonItemComponents.COOKING_COMPONENT)
        markUpdated()
    }

    /**
     * Converts the block entity back into an `ItemStack` with the `CookingComponent` when broken.
     */
    fun toItemStack(): ItemStack {
        val stack = ItemStack(this.blockState.block)
        cookingComponent?.let { component ->
            // Use the appropriate method to set the component
            stack.getComponents().apply {
                stack.set(CobblemonItemComponents.COOKING_COMPONENT, component)
            }
        }
        return stack
    }

    /**
     * Generate a `FishingBait` by combining effects from all `RodBaitComponent` data in the `CookingComponent`.
     */
    fun getBaitFromLureCake(): FishingBait? {
        val component = cookingComponent ?: return null
        val combinedEffects = listOf(
                component.bait1.effects,
                component.bait2.effects,
                component.bait3.effects
        ).flatten()

        return FishingBait(
                item = cobblemonResource("lure_cake"), // Directly specify the lure_cake ResourceLocation
                effects = combinedEffects
        )
    }

    override fun saveAdditional(tag: CompoundTag, registries: HolderLookup.Provider) {
        super.saveAdditional(tag, registries)
        cookingComponent?.let { component ->
            CobblemonItemComponents.COOKING_COMPONENT.codec()
                    ?.encodeStart(NbtOps.INSTANCE, component)
                    ?.result()
                    ?.ifPresent { encodedTag ->
                        tag.put("CookingComponent", encodedTag)
                    }
        }
    }

    override fun loadAdditional(tag: CompoundTag, registries: HolderLookup.Provider) {
        super.loadAdditional(tag, registries)
        if (tag.contains("CookingComponent")) {
            CobblemonItemComponents.COOKING_COMPONENT.codec()
                    ?.parse(NbtOps.INSTANCE, tag.getCompound("CookingComponent"))
                    ?.result()
                    ?.ifPresent { component ->
                        cookingComponent = component
                    }
        }
    }

    /**
     * Synchronize block entity data with the client.
     */
    override fun getUpdatePacket(): Packet<ClientGamePacketListener>? {
        return ClientboundBlockEntityDataPacket.create(this)
    }

    override fun getUpdateTag(registryLookup: HolderLookup.Provider): CompoundTag {
        val tag = CompoundTag()
        saveAdditional(tag, registryLookup)
        return tag
    }

   /* override fun getUpdateTag(): CompoundTag {
        val tag = CompoundTag()
        saveAdditional(tag, HolderLookup.Provider.create(Stream.empty())) // Create a temporary empty provider
        return tag
    }*/


    /**
     * Mark the block entity as updated, forcing a save and client update.
     */
    private fun markUpdated() {
        setChanged()
        level?.sendBlockUpdated(worldPosition, blockState, blockState, 3)
    }
}