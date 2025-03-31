/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.riding

import com.cobblemon.mod.common.api.net.Decodable
import com.cobblemon.mod.common.api.net.Encodable
import net.minecraft.resources.ResourceLocation

/**
 * Honestly having a base interface is probably unnecessary I'm just considering whether there's going to be some
 * kind of shared state later.
 *
 * @author Hiroku
 * @since October 6th, 2024
 */
interface RidingState: Encodable, Decodable {
    val key: ResourceLocation
    var isDirty: Boolean
    fun reset()
}
