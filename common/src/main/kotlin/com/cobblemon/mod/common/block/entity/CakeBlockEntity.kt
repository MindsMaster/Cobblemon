/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.block.entity

import com.cobblemon.mod.common.CobblemonItemComponents
import com.cobblemon.mod.common.item.components.CookingComponent
import net.minecraft.core.BlockPos
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

open class CakeBlockEntity(
    type: BlockEntityType<*>,
    pos: BlockPos,
    state: BlockState
) : BlockEntity(type, pos, state) {

    companion object {
        const val MAX_NUMBER_OF_BITES = 6
    }

    var cookingComponent: CookingComponent? = null
    var bites: Int = 0

    fun initializeFromItemStack(itemStack: ItemStack) {
        cookingComponent = itemStack.get(CobblemonItemComponents.COOKING_COMPONENT)
    }

    override fun saveAdditional(
        tag: CompoundTag,
        registries: HolderLookup.Provider
    ) {
        super.saveAdditional(tag, registries)

        tag.putInt("Bites", bites)
        cookingComponent?.let { component ->
            CobblemonItemComponents.COOKING_COMPONENT.codec()
                ?.encodeStart(NbtOps.INSTANCE, component)
                ?.result()
                ?.ifPresent { encodedTag ->
                    tag.put("CookingComponent", encodedTag)
                }
        }
    }

    override fun loadAdditional(
        tag: CompoundTag,
        registries: HolderLookup.Provider
    ) {
        super.loadAdditional(tag, registries)

        bites = tag.getInt("Bites")
        if (tag.contains("CookingComponent")) {
            CobblemonItemComponents.COOKING_COMPONENT.codec()
                ?.parse(NbtOps.INSTANCE, tag.getCompound("CookingComponent"))
                ?.result()
                ?.ifPresent { component ->
                    cookingComponent = component
                }
        }
    }

    override fun getUpdatePacket(): Packet<ClientGamePacketListener?>? {
        return ClientboundBlockEntityDataPacket.create(this)
    }

    override fun getUpdateTag(registries: HolderLookup.Provider): CompoundTag? {
        val tag = CompoundTag()
        saveAdditional(tag, registries)
        return tag
    }

    fun toItemStack(): ItemStack {
        val stack = ItemStack(this.blockState.block)
        cookingComponent?.let { component ->
            stack.getComponents().apply {
                stack.set(CobblemonItemComponents.COOKING_COMPONENT, component)
            }
        }
        return stack
    }
}