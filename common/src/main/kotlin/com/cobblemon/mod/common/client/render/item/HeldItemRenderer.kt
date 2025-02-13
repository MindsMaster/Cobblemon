/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.item

import com.cobblemon.mod.common.api.tags.CobblemonItemTags.WEARABLE_FACE_ITEMS
import com.cobblemon.mod.common.api.tags.CobblemonItemTags.WEARABLE_HAT_ITEMS
import com.cobblemon.mod.common.client.CobblemonClient.storage
import com.cobblemon.mod.common.client.entity.PokemonClientDelegate
import com.cobblemon.mod.common.client.render.MatrixWrapper
import com.cobblemon.mod.common.client.render.models.blockbench.NullObjectParser
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.pokemon.Pokemon
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Axis
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.LightTexture
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.world.item.ItemDisplayContext
import net.minecraft.world.item.ItemStack

class HeldItemRenderer(

) {
    companion object {
        /**
         * @param [pokemonEntity] The pokemon to get the held item from.
         * @return Returns the held item for this pokemon.
         */
        fun getRenderableItem(pokemonEntity: PokemonEntity): ItemStack {
            //Server Search
            if(!pokemonEntity.shownItem.isEmpty) return pokemonEntity.shownItem
            //Client Search
            /*
            TODO depending on we decide how hidden items will work, this might not be needed
            This would allow the owner of a pokemon to always see the held item while other players can not.
            */
            if (pokemonEntity.ownerUUID?.equals(Minecraft.getInstance().player?.uuid) == true) {
                //See if the pokemon that is being rendered is part of client users party
                for (p in storage.myParty) {
                    return comparePokemonEntity(p, pokemonEntity) ?: continue
                }
                //If not then check PCs **this is for pokemon roaming around from the pasture block**
                for (pc in storage.pcStores.values) for (box in pc.boxes) for (p in box.slots) {
                    return comparePokemonEntity(p, pokemonEntity) ?: continue
                }
            }
            //Default
            return ItemStack.EMPTY
        }

        //This is used to compare the uuid of the pokemon being rendered to the pokemon in your party/pc
        private fun comparePokemonEntity(p: Pokemon?, pokemonEntity: PokemonEntity ) : ItemStack? {
            if (p?.entity?.uuid?.equals(pokemonEntity.uuid) == true ) {
                if (p.isItemHidden) return ItemStack.EMPTY
                return p.heldItem()
            }
            return null
        }
    }

    private val itemRenderer = Minecraft.getInstance().itemRenderer
    private var displayContext = ItemDisplayContext.FIXED
    private var scale = 1.0f

    fun render(
        entity: PokemonEntity?,
        item: ItemStack,
        locators: Map<String, MatrixWrapper>,
        poseStack: PoseStack,
        buffer: MultiBufferSource,
        light: Int,
        seed: Int
    ) {
        if (item.isEmpty) return

        poseStack.pushPose()
        when {
            (locators.containsKey("item_face") && item.`is`(WEARABLE_FACE_ITEMS)) -> {
                poseStack.mulPose(locators["item_face"]!!.matrix)
                displayContext = ItemDisplayContext.HEAD
                applyModifiers("item_face", locators)
                poseStack.translate(0f, 0f, .28f * scale)
                poseStack.scale(0.7f * scale, 0.7f * scale, 0.7f * scale)
            }
            (locators.containsKey("item_hat") && item.`is`(WEARABLE_HAT_ITEMS)) -> {
                poseStack.mulPose(locators["item_hat"]!!.matrix)
                displayContext = ItemDisplayContext.HEAD
                applyModifiers("item_hat", locators)
                poseStack.translate(0f, -0.26f * scale, 0f)
                poseStack.scale(.68f * scale, .68f * scale, .68f * scale)
            }
            (locators.containsKey("item")) -> {
                poseStack.mulPose(locators["item"]!!.matrix)
                applyModifiers("item", locators)
                when(displayContext) {
                    ItemDisplayContext.FIXED -> {
                        poseStack.translate(0f, 0.01666f * scale, 0f)
                        poseStack.scale(.5f * scale, .5f * scale, .5f * scale)
                        poseStack.mulPose(Axis.XP.rotationDegrees(90f))
                    }
                    ItemDisplayContext.THIRD_PERSON_RIGHT_HAND -> {
                        poseStack.translate(0.075f* scale, 0f * scale, -0.05f* scale)
                        poseStack.scale(scale, scale, scale)
                        poseStack.mulPose(Axis.XP.rotationDegrees(-90f))
                        poseStack.mulPose(Axis.YP.rotationDegrees(-90f))
                    }
                    else -> {
                        poseStack.scale(scale, scale, scale)
                    }
                }
            }
            else -> { // Don't render any item
                poseStack.popPose()
                return
            }
        }
        itemRenderer.renderStatic(entity, item, displayContext, false, poseStack, buffer, null, light, OverlayTexture.NO_OVERLAY, seed)
        poseStack.popPose()
    }

    fun renderOnModel(
        item: ItemStack,
        locators: Map<String, MatrixWrapper>,
        poseStack: PoseStack,
        buffer: MultiBufferSource,
        light: Int = LightTexture.pack(11, 7)
    ){
        render(null, item, locators, poseStack, buffer, light, 0)
    }

    fun renderOnEntity(
        entity: PokemonEntity,
        delegate: PokemonClientDelegate,
        poseStack: PoseStack,
        buffer: MultiBufferSource,
        light: Int
    ) {
        val shownItem = getRenderableItem(entity)
        val locators: Map<String, MatrixWrapper> = delegate.locatorStates

        render(entity, shownItem, locators, poseStack, buffer, light, 0)
    }

    private fun applyModifiers(name: String, locators: Map<String, MatrixWrapper>) {
        locators.forEach { (locator: String, m: MatrixWrapper) ->
            val modifiers: Map<String, Float>
            if (locator.startsWith("_null_$name[")) {
                modifiers = NullObjectParser.parseNullObject(locator).modifiers
                if (modifiers.containsKey("scale")) scale = modifiers["scale"]!!
                if (modifiers.containsKey("mode")) displayContext = ItemDisplayContext.entries[((modifiers["mode"]!!).toInt()) % 9]
                return
            }
        }
    }
}