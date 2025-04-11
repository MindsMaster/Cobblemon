/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.ai

import com.cobblemon.mod.common.api.ai.config.BrainConfig
import com.cobblemon.mod.common.util.asTranslated
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.LivingEntity

class BrainPreset(
    val name: Component = "".asTranslated(),
    val description: Component = "".asTranslated(),
    /** Some presets aren't really worth showing on the client, namely ones that are simply conditional bundles of other presets. */
    val visible: Boolean = true,
    val entityType: ResourceLocation? = null,
    val configurations: List<BrainConfig> = mutableListOf()
) {
    fun canBeApplied(entity: LivingEntity) = entityType?.let { entityType == entity.type.builtInRegistryHolder().unwrapKey().get().location() } != false
}