/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.item

import com.cobblemon.mod.common.client.CobblemonClient
import com.cobblemon.mod.common.client.pokedex.PokedexTypes
import com.cobblemon.mod.common.client.pot.PotTypes
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.util.isLookingAt
import net.minecraft.client.player.LocalPlayer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.UseAnim
import net.minecraft.world.level.Level
import net.minecraft.world.phys.AABB

class PotItem(val type: PotTypes): CobblemonItem(Properties().stacksTo(1)) {

    override fun getUseAnimation(itemStack: ItemStack): UseAnim? = UseAnim.TOOT_HORN

    override fun getUseDuration(stack: ItemStack, user: LivingEntity): Int = 72000
}
