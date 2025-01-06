/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.item

import com.cobblemon.mod.common.client.pot.PotTypes
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.UseAnim

class PotItem(val type: PotTypes): CobblemonItem(Properties().stacksTo(1)) {

    override fun getUseAnimation(itemStack: ItemStack): UseAnim? = UseAnim.TOOT_HORN

    override fun getUseDuration(stack: ItemStack, user: LivingEntity): Int = 72000
}
