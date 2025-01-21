package com.cobblemon.mod.common.item.interactive

import com.cobblemon.mod.common.CobblemonSounds
import com.cobblemon.mod.common.api.pokemon.stats.Stats
import net.minecraft.sounds.SoundEvent

class MochiItem(stat: Stats): EVIncreaseItem(stat, 10) {
    override val sound: SoundEvent = CobblemonSounds.MOCHI_USE
}