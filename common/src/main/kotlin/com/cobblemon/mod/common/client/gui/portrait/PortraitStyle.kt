/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui.portrait

import com.mojang.serialization.Codec
import net.minecraft.util.StringRepresentable

enum class PortraitStyle : StringRepresentable {

    NEVER_ANIMATE,
    ANIMATE_SELECTED,
    ALWAYS_ANIMATE;

    override fun getSerializedName(): String = this.name

    companion object {

        @JvmStatic
        val CODEC: Codec<PortraitStyle> = StringRepresentable.fromEnum(PortraitStyle::values)

    }

}