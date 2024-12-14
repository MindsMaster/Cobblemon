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

class BrainPreset {
    val name: Component = "dummy".asTranslated()
    val description: Component = "".asTranslated()
    val configurations = mutableListOf<BrainConfig>()
}