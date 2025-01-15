/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.cooking

import com.cobblemon.mod.common.api.data.JsonDataRegistry
import com.cobblemon.mod.common.api.reactive.SimpleObservable
import com.cobblemon.mod.common.net.messages.client.cooking.SeasoningRegistrySyncPacket
import com.cobblemon.mod.common.util.adapters.IdentifierAdapter
import com.cobblemon.mod.common.util.cobblemonResource
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.packs.PackType
import net.minecraft.world.item.ItemStack

object Seasonings : JsonDataRegistry<Seasoning> {
    override val id = cobblemonResource("seasonings")
    override val type = PackType.SERVER_DATA
    override val observable = SimpleObservable<Seasonings>()
    override val typeToken: TypeToken<Seasoning> = TypeToken.get(Seasoning::class.java)
    override val resourcePath = "seasonings"
    override val gson: Gson = GsonBuilder()
        .registerTypeAdapter(ResourceLocation::class.java, IdentifierAdapter)
        .setPrettyPrinting()
        .create()

    private val itemMap = mutableMapOf<ResourceLocation, Seasoning>()

    override fun sync(player: ServerPlayer) {
        SeasoningRegistrySyncPacket(this.itemMap.values.toList()).sendToPlayer(player)
    }

    override fun reload(data: Map<ResourceLocation, Seasoning>) {
        itemMap.clear()
        data.forEach { id, seasoning ->
            //println("Loaded Seasoning: $id -> $seasoning") // Debugging output
            itemMap[id] = seasoning
        }
    }

    fun getFromItemStack(stack: ItemStack): Seasoning? {
        val itemId = BuiltInRegistries.ITEM.getKey(stack.item)
        //println("Checking ItemStack: ${stack.item} -> Identifier: $itemId")
        return if (itemId != null) getFromIdentifier(itemId) else null
    }

    fun getFromIdentifier(identifier: ResourceLocation): Seasoning? {
        //println("Looking for Identifier: $identifier in ${itemMap.keys}")
        return itemMap[identifier]
    }


    fun isSeasoning(stack: ItemStack) = BuiltInRegistries.ITEM.getKey(stack.item)?.let { itemMap.containsKey(it) } == true
}
