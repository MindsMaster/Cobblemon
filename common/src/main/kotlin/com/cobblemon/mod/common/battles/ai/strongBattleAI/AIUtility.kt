package com.cobblemon.mod.common.battles.ai.strongBattleAI

import com.cobblemon.mod.common.api.types.ElementalTypes


/**
 * Everything in this class will be eventually getting replaced by a dynamic datadriven approach
 * when moves/abilities/types become data-packable or at least queryable from showdown
 */
object AIUtility {
    fun getDamageMultiplier(attackerType: com.cobblemon.mod.common.api.types.ElementalType, defenderType: com.cobblemon.mod.common.api.types.ElementalType): Double {
        return typeEffectiveness[attackerType]?.get(defenderType) ?: 1.0

    }

    val typeEffectiveness: Map<com.cobblemon.mod.common.api.types.ElementalType, Map<com.cobblemon.mod.common.api.types.ElementalType, Double>> = mapOf(
        ElementalTypes.NORMAL to mapOf(
            ElementalTypes.NORMAL to 1.0, ElementalTypes.FIRE to 1.0, ElementalTypes.WATER to 1.0, ElementalTypes.ELECTRIC to 1.0, ElementalTypes.GRASS to 1.0,
            ElementalTypes.ICE to 1.0, ElementalTypes.FIGHTING to 1.0, ElementalTypes.POISON to 1.0, ElementalTypes.GROUND to 1.0, ElementalTypes.FLYING to 1.0,
            ElementalTypes.PSYCHIC to 1.0, ElementalTypes.BUG to 1.0, ElementalTypes.ROCK to 0.5, ElementalTypes.GHOST to 0.0, ElementalTypes.DRAGON to 1.0,
            ElementalTypes.DARK to 1.0, ElementalTypes.STEEL to 0.5, ElementalTypes.FAIRY to 1.0
        ),
        ElementalTypes.FIRE to mapOf(
            ElementalTypes.NORMAL to 1.0, ElementalTypes.FIRE to 0.5, ElementalTypes.WATER to 0.5, ElementalTypes.ELECTRIC to 1.0, ElementalTypes.GRASS to 2.0,
            ElementalTypes.ICE to 2.0, ElementalTypes.FIGHTING to 1.0, ElementalTypes.POISON to 1.0, ElementalTypes.GROUND to 1.0, ElementalTypes.FLYING to 1.0,
            ElementalTypes.PSYCHIC to 1.0, ElementalTypes.BUG to 2.0, ElementalTypes.ROCK to 0.5, ElementalTypes.GHOST to 1.0, ElementalTypes.DRAGON to 0.5,
            ElementalTypes.DARK to 1.0, ElementalTypes.STEEL to 2.0, ElementalTypes.FAIRY to 1.0
        ),
        ElementalTypes.WATER to mapOf(
            ElementalTypes.NORMAL to 1.0, ElementalTypes.FIRE to 2.0, ElementalTypes.WATER to 0.5, ElementalTypes.ELECTRIC to 1.0, ElementalTypes.GRASS to 0.5,
            ElementalTypes.ICE to 1.0, ElementalTypes.FIGHTING to 1.0, ElementalTypes.POISON to 1.0, ElementalTypes.GROUND to 2.0, ElementalTypes.FLYING to 1.0,
            ElementalTypes.PSYCHIC to 1.0, ElementalTypes.BUG to 1.0, ElementalTypes.ROCK to 2.0, ElementalTypes.GHOST to 1.0, ElementalTypes.DRAGON to 0.5,
            ElementalTypes.DARK to 1.0, ElementalTypes.STEEL to 1.0, ElementalTypes.FAIRY to 1.0
        ),
        ElementalTypes.ELECTRIC to mapOf(
            ElementalTypes.NORMAL to 1.0, ElementalTypes.FIRE to 1.0, ElementalTypes.WATER to 2.0, ElementalTypes.ELECTRIC to 0.5, ElementalTypes.GRASS to 0.5,
            ElementalTypes.ICE to 1.0, ElementalTypes.FIGHTING to 1.0, ElementalTypes.POISON to 1.0, ElementalTypes.GROUND to 0.0, ElementalTypes.FLYING to 2.0,
            ElementalTypes.PSYCHIC to 1.0, ElementalTypes.BUG to 1.0, ElementalTypes.ROCK to 1.0, ElementalTypes.GHOST to 1.0, ElementalTypes.DRAGON to 0.5,
            ElementalTypes.DARK to 1.0, ElementalTypes.STEEL to 1.0, ElementalTypes.FAIRY to 1.0
        ),
        ElementalTypes.GRASS to mapOf(
            ElementalTypes.NORMAL to 1.0, ElementalTypes.FIRE to 0.5, ElementalTypes.WATER to 2.0, ElementalTypes.ELECTRIC to 1.0, ElementalTypes.GRASS to 0.5,
            ElementalTypes.ICE to 1.0, ElementalTypes.FIGHTING to 1.0, ElementalTypes.POISON to 0.5, ElementalTypes.GROUND to 2.0, ElementalTypes.FLYING to 0.5,
            ElementalTypes.PSYCHIC to 1.0, ElementalTypes.BUG to 0.5, ElementalTypes.ROCK to 2.0, ElementalTypes.GHOST to 1.0, ElementalTypes.DRAGON to 0.5,
            ElementalTypes.DARK to 1.0, ElementalTypes.STEEL to 0.5, ElementalTypes.FAIRY to 1.0
        ),
        ElementalTypes.ICE to mapOf(
            ElementalTypes.NORMAL to 1.0, ElementalTypes.FIRE to 0.5, ElementalTypes.WATER to 0.5, ElementalTypes.ELECTRIC to 1.0, ElementalTypes.GRASS to 2.0,
            ElementalTypes.ICE to 0.5, ElementalTypes.FIGHTING to 1.0, ElementalTypes.POISON to 1.0, ElementalTypes.GROUND to 2.0, ElementalTypes.FLYING to 2.0,
            ElementalTypes.PSYCHIC to 1.0, ElementalTypes.BUG to 1.0, ElementalTypes.ROCK to 1.0, ElementalTypes.GHOST to 1.0, ElementalTypes.DRAGON to 2.0,
            ElementalTypes.DARK to 1.0, ElementalTypes.STEEL to 0.5, ElementalTypes.FAIRY to 1.0
        ),
        ElementalTypes.FIGHTING to mapOf(
            ElementalTypes.NORMAL to 2.0, ElementalTypes.FIRE to 1.0, ElementalTypes.WATER to 1.0, ElementalTypes.ELECTRIC to 1.0, ElementalTypes.GRASS to 1.0,
            ElementalTypes.ICE to 2.0, ElementalTypes.FIGHTING to 1.0, ElementalTypes.POISON to 0.5, ElementalTypes.GROUND to 1.0, ElementalTypes.FLYING to 0.5,
            ElementalTypes.PSYCHIC to 0.5, ElementalTypes.BUG to 0.5, ElementalTypes.ROCK to 2.0, ElementalTypes.GHOST to 0.0, ElementalTypes.DRAGON to 1.0,
            ElementalTypes.DARK to 2.0, ElementalTypes.STEEL to 2.0, ElementalTypes.FAIRY to 0.5
        ),
        ElementalTypes.POISON to mapOf(
            ElementalTypes.NORMAL to 1.0, ElementalTypes.FIRE to 1.0, ElementalTypes.WATER to 1.0, ElementalTypes.ELECTRIC to 1.0, ElementalTypes.GRASS to 2.0,
            ElementalTypes.ICE to 1.0, ElementalTypes.FIGHTING to 1.0, ElementalTypes.POISON to 0.5, ElementalTypes.GROUND to 0.5, ElementalTypes.FLYING to 1.0,
            ElementalTypes.PSYCHIC to 1.0, ElementalTypes.BUG to 1.0, ElementalTypes.ROCK to 0.5, ElementalTypes.GHOST to 0.5, ElementalTypes.DRAGON to 1.0,
            ElementalTypes.DARK to 1.0, ElementalTypes.STEEL to 0.0, ElementalTypes.FAIRY to 2.0
        ),
        ElementalTypes.GROUND to mapOf(
            ElementalTypes.NORMAL to 1.0, ElementalTypes.FIRE to 2.0, ElementalTypes.WATER to 1.0, ElementalTypes.ELECTRIC to 2.0, ElementalTypes.GRASS to 0.5,
            ElementalTypes.ICE to 1.0, ElementalTypes.FIGHTING to 1.0, ElementalTypes.POISON to 2.0, ElementalTypes.GROUND to 1.0, ElementalTypes.FLYING to 0.0,
            ElementalTypes.PSYCHIC to 1.0, ElementalTypes.BUG to 0.5, ElementalTypes.ROCK to 2.0, ElementalTypes.GHOST to 1.0, ElementalTypes.DRAGON to 1.0,
            ElementalTypes.DARK to 1.0, ElementalTypes.STEEL to 2.0, ElementalTypes.FAIRY to 1.0
        ),
        ElementalTypes.FLYING to mapOf(
            ElementalTypes.NORMAL to 1.0, ElementalTypes.FIRE to 1.0, ElementalTypes.WATER to 1.0, ElementalTypes.ELECTRIC to 0.5, ElementalTypes.GRASS to 2.0,
            ElementalTypes.ICE to 1.0, ElementalTypes.FIGHTING to 2.0, ElementalTypes.POISON to 1.0, ElementalTypes.GROUND to 1.0, ElementalTypes.FLYING to 1.0,
            ElementalTypes.PSYCHIC to 1.0, ElementalTypes.BUG to 2.0, ElementalTypes.ROCK to 0.5, ElementalTypes.GHOST to 1.0, ElementalTypes.DRAGON to 1.0,
            ElementalTypes.DARK to 1.0, ElementalTypes.STEEL to 0.5, ElementalTypes.FAIRY to 1.0
        ),
        ElementalTypes.PSYCHIC to mapOf(
            ElementalTypes.NORMAL to 1.0, ElementalTypes.FIRE to 1.0, ElementalTypes.WATER to 1.0, ElementalTypes.ELECTRIC to 1.0, ElementalTypes.GRASS to 1.0,
            ElementalTypes.ICE to 1.0, ElementalTypes.FIGHTING to 2.0, ElementalTypes.POISON to 2.0, ElementalTypes.GROUND to 1.0, ElementalTypes.FLYING to 1.0,
            ElementalTypes.PSYCHIC to 0.5, ElementalTypes.BUG to 1.0, ElementalTypes.ROCK to 1.0, ElementalTypes.GHOST to 1.0, ElementalTypes.DRAGON to 1.0,
            ElementalTypes.DARK to 0.0, ElementalTypes.STEEL to 0.5, ElementalTypes.FAIRY to 1.0
        ),
        ElementalTypes.BUG to mapOf(
            ElementalTypes.NORMAL to 1.0, ElementalTypes.FIRE to 0.5, ElementalTypes.WATER to 1.0, ElementalTypes.ELECTRIC to 1.0, ElementalTypes.GRASS to 2.0,
            ElementalTypes.ICE to 1.0, ElementalTypes.FIGHTING to 0.5, ElementalTypes.POISON to 0.5, ElementalTypes.GROUND to 1.0, ElementalTypes.FLYING to 0.5,
            ElementalTypes.PSYCHIC to 2.0, ElementalTypes.BUG to 1.0, ElementalTypes.ROCK to 1.0, ElementalTypes.GHOST to 0.5, ElementalTypes.DRAGON to 1.0,
            ElementalTypes.DARK to 2.0, ElementalTypes.STEEL to 0.5, ElementalTypes.FAIRY to 0.5
        ),
        ElementalTypes.ROCK to mapOf(
            ElementalTypes.NORMAL to 1.0, ElementalTypes.FIRE to 2.0, ElementalTypes.WATER to 1.0, ElementalTypes.ELECTRIC to 1.0, ElementalTypes.GRASS to 1.0,
            ElementalTypes.ICE to 2.0, ElementalTypes.FIGHTING to 0.5, ElementalTypes.POISON to 1.0, ElementalTypes.GROUND to 0.5, ElementalTypes.FLYING to 2.0,
            ElementalTypes.PSYCHIC to 1.0, ElementalTypes.BUG to 2.0, ElementalTypes.ROCK to 1.0, ElementalTypes.GHOST to 1.0, ElementalTypes.DRAGON to 1.0,
            ElementalTypes.DARK to 1.0, ElementalTypes.STEEL to 0.5, ElementalTypes.FAIRY to 1.0
        ),
        ElementalTypes.GHOST to mapOf(
            ElementalTypes.NORMAL to 0.0, ElementalTypes.FIRE to 1.0, ElementalTypes.WATER to 1.0, ElementalTypes.ELECTRIC to 1.0, ElementalTypes.GRASS to 1.0,
            ElementalTypes.ICE to 1.0, ElementalTypes.FIGHTING to 1.0, ElementalTypes.POISON to 1.0, ElementalTypes.GROUND to 1.0, ElementalTypes.FLYING to 1.0,
            ElementalTypes.PSYCHIC to 2.0, ElementalTypes.BUG to 1.0, ElementalTypes.ROCK to 1.0, ElementalTypes.GHOST to 2.0, ElementalTypes.DRAGON to 1.0,
            ElementalTypes.DARK to 0.5, ElementalTypes.STEEL to 1.0, ElementalTypes.FAIRY to 1.0
        ),
        ElementalTypes.DRAGON to mapOf(
            ElementalTypes.NORMAL to 1.0, ElementalTypes.FIRE to 1.0, ElementalTypes.WATER to 1.0, ElementalTypes.ELECTRIC to 1.0, ElementalTypes.GRASS to 1.0,
            ElementalTypes.ICE to 1.0, ElementalTypes.FIGHTING to 1.0, ElementalTypes.POISON to 1.0, ElementalTypes.GROUND to 1.0, ElementalTypes.FLYING to 1.0,
            ElementalTypes.PSYCHIC to 1.0, ElementalTypes.BUG to 1.0, ElementalTypes.ROCK to 1.0, ElementalTypes.GHOST to 1.0, ElementalTypes.DRAGON to 2.0,
            ElementalTypes.DARK to 1.0, ElementalTypes.STEEL to 0.5, ElementalTypes.FAIRY to 0.0
        ),
        ElementalTypes.DARK to mapOf(
            ElementalTypes.NORMAL to 1.0, ElementalTypes.FIRE to 1.0, ElementalTypes.WATER to 1.0, ElementalTypes.ELECTRIC to 1.0, ElementalTypes.GRASS to 1.0,
            ElementalTypes.ICE to 1.0, ElementalTypes.FIGHTING to 0.5, ElementalTypes.POISON to 1.0, ElementalTypes.GROUND to 1.0, ElementalTypes.FLYING to 1.0,
            ElementalTypes.PSYCHIC to 2.0, ElementalTypes.BUG to 1.0, ElementalTypes.ROCK to 1.0, ElementalTypes.GHOST to 2.0, ElementalTypes.DRAGON to 1.0,
            ElementalTypes.DARK to 0.5, ElementalTypes.STEEL to 1.0, ElementalTypes.FAIRY to 0.5
        ),
        ElementalTypes.STEEL to mapOf(
            ElementalTypes.NORMAL to 1.0, ElementalTypes.FIRE to 0.5, ElementalTypes.WATER to 0.5, ElementalTypes.ELECTRIC to 0.5, ElementalTypes.GRASS to 1.0,
            ElementalTypes.ICE to 2.0, ElementalTypes.FIGHTING to 1.0, ElementalTypes.POISON to 1.0, ElementalTypes.GROUND to 1.0, ElementalTypes.FLYING to 1.0,
            ElementalTypes.PSYCHIC to 1.0, ElementalTypes.BUG to 1.0, ElementalTypes.ROCK to 2.0, ElementalTypes.GHOST to 1.0, ElementalTypes.DRAGON to 1.0,
            ElementalTypes.DARK to 1.0, ElementalTypes.STEEL to 0.5, ElementalTypes.FAIRY to 2.0
        ),
        ElementalTypes.FAIRY to mapOf(
            ElementalTypes.NORMAL to 1.0, ElementalTypes.FIRE to 0.5, ElementalTypes.WATER to 1.0, ElementalTypes.ELECTRIC to 1.0, ElementalTypes.GRASS to 1.0,
            ElementalTypes.ICE to 1.0, ElementalTypes.FIGHTING to 2.0, ElementalTypes.POISON to 0.5, ElementalTypes.GROUND to 1.0, ElementalTypes.FLYING to 1.0,
            ElementalTypes.PSYCHIC to 1.0, ElementalTypes.BUG to 1.0, ElementalTypes.ROCK to 1.0, ElementalTypes.GHOST to 1.0, ElementalTypes.DRAGON to 2.0,
            ElementalTypes.DARK to 2.0, ElementalTypes.STEEL to 0.5, ElementalTypes.FAIRY to 1.0
        )
    )

    val multiHitMoves: Map<String, Pair<Int, Int>> = mapOf(
        // 2 - 5 hit moves
        "armthrust" to Pair(2 to 5),
        "barrage" to Pair(2 to 5),
        "bonerush" to Pair(2 to 5),
        "bulletseed" to Pair(2 to 5),
        "cometpunch" to Pair(2 to 5),
        "doubleslap" to Pair(2 to 5),
        "furyattack" to Pair(2 to 5),
        "furyswipes" to Pair(2 to 5),
        "iciclespear" to Pair(2 to 5),
        "pinmissile" to Pair(2 to 5),
        "rockblast" to Pair(2 to 5),
        "scaleshot" to Pair(2 to 5),
        "spikecannon" to Pair(2 to 5),
        "tailslap" to Pair(2 to 5),
        "watershuriken" to Pair(2 to 5),

        // fixed hit count
        "bonemerang" to Pair(2 to 2),
        "doublehit" to Pair(2 to 2),
        "doubleironbash" to Pair(2 to 2),
        "doublekick" to Pair(2 to 2),
        "dragondarts" to Pair(2 to 2),
        "dualchop" to Pair(2 to 2),
        "dualwingbeat" to Pair(2 to 2),
        "geargrind" to Pair(2 to 2),
        "twinbeam" to Pair(2 to 2),
        "twineedle" to Pair(2 to 2),
        "suringstrikes" to Pair(3 to 3),
        "tripledive" to Pair(3 to 3),
        "watershuriken" to Pair(3 to 3),

        // accuracy based multi-hit moves
        "tripleaxel" to Pair(1 to 3),
        "triplekick" to Pair(1 to 3),
        "populationbomb" to Pair(1 to 10)
    )

    val statusMoves: Map<MoveTemplate?, String> = mapOf(
        Moves.getByName("willowisp") to Statuses.BURN.showdownName,
        Moves.getByName("scald") to Statuses.BURN.showdownName,
        Moves.getByName("scorchingsands") to Statuses.BURN.showdownName,
        Moves.getByName("glare") to Statuses.PARALYSIS.showdownName,
        Moves.getByName("nuzzle") to Statuses.PARALYSIS.showdownName,
        Moves.getByName("stunspore") to Statuses.PARALYSIS.showdownName,
        Moves.getByName("thunderwave") to Statuses.PARALYSIS.showdownName,
        Moves.getByName("Nuzzle") to Statuses.PARALYSIS.showdownName,
        Moves.getByName("darkvoid") to Statuses.SLEEP.showdownName,
        Moves.getByName("hypnosis") to Statuses.SLEEP.showdownName,
        Moves.getByName("lovelykiss") to Statuses.SLEEP.showdownName,
        Moves.getByName("relicsong") to Statuses.SLEEP.showdownName,
        Moves.getByName("sing") to Statuses.SLEEP.showdownName,
        Moves.getByName("sleeppower") to Statuses.SLEEP.showdownName,
        Moves.getByName("spore") to Statuses.SLEEP.showdownName,
        Moves.getByName("yawn") to Statuses.SLEEP.showdownName,
        Moves.getByName("chatter") to "confusion",
        Moves.getByName("confuseray") to "confusion",
        Moves.getByName("dynamicpunch") to "confusion",
        Moves.getByName("flatter") to "confusion",
        Moves.getByName("supersonic") to "confusion",
        Moves.getByName("swagger") to "confusion",
        Moves.getByName("sweetkiss") to "confusion",
        Moves.getByName("teeterdance") to "confusion",
        Moves.getByName("poisongas") to Statuses.POISON.showdownName,
        Moves.getByName("poisonpowder") to Statuses.POISON.showdownName,
        Moves.getByName("toxic") to Statuses.POISON_BADLY.showdownName,
        Moves.getByName("toxicthread") to Statuses.POISON.showdownName,
        Moves.getByName("curse") to "cursed",
        Moves.getByName("leechseed") to "leech"
    )

    val boostFromMoves: Map<String, Map<Stat, Int>> = mapOf(
        "bellydrum" to mapOf(Stats.ATTACK to 6),
        "bulkup" to mapOf(Stats.ATTACK to 1, Stats.DEFENCE to 1),
        "clangoroussoul" to mapOf(Stats.ATTACK to 1, Stats.DEFENCE to 1, Stats.SPECIAL_ATTACK to 1, Stats.SPECIAL_DEFENCE to 1, Stats.SPEED to 1),
        "coil" to mapOf(Stats.ATTACK to 1, Stats.DEFENCE to 1, Stats.ACCURACY to 1),
        "dragondance" to mapOf(Stats.ATTACK to 1, Stats.SPEED to 1),
        "extremeevoboost" to mapOf(Stats.ATTACK to 2, Stats.DEFENCE to 2, Stats.SPECIAL_ATTACK to 2, Stats.SPECIAL_DEFENCE to 2, Stats.SPEED to 2),
        "clangoroussoulblaze" to mapOf(Stats.ATTACK to 1, Stats.DEFENCE to 1, Stats.SPECIAL_ATTACK to 1, Stats.SPECIAL_DEFENCE to 1, Stats.SPEED to 1),
        "filletaway" to mapOf(Stats.ATTACK to 2, Stats.SPECIAL_ATTACK to 2, Stats.SPEED to 2),
        "honeclaws" to mapOf(Stats.ATTACK to 1, Stats.ACCURACY to 1),
        "noretreat" to mapOf(Stats.ATTACK to 1, Stats.DEFENCE to 1, Stats.SPECIAL_ATTACK to 1, Stats.SPECIAL_DEFENCE to 1, Stats.SPEED to 1),
        "shellsmash" to mapOf(Stats.ATTACK to 2, Stats.DEFENCE to -1, Stats.SPECIAL_ATTACK to 2, Stats.SPECIAL_DEFENCE to -1, Stats.SPEED to 2),
        "shiftgear" to mapOf(Stats.ATTACK to 1, Stats.SPEED to 2),
        "swordsdance" to mapOf(Stats.ATTACK to 2),
        "tidyup" to mapOf(Stats.ATTACK to 1, Stats.SPEED to 1),
        "victorydance" to mapOf(Stats.ATTACK to 1, Stats.DEFENCE to 1, Stats.SPEED to 1),
        "acidarmor" to mapOf(Stats.DEFENCE to 2),
        "barrier" to mapOf(Stats.DEFENCE to 2),
        "cottonguard" to mapOf(Stats.DEFENCE to 3),
        "defensecurl" to mapOf(Stats.DEFENCE to 1),
        "irondefense" to mapOf(Stats.DEFENCE to 2),
        "shelter" to mapOf(Stats.DEFENCE to 2, Stats.EVASION to 1),
        "stockpile" to mapOf(Stats.DEFENCE to 1, Stats.SPECIAL_DEFENCE to 1),
        "stuffcheeks" to mapOf(Stats.DEFENCE to 2),
        "amnesia" to mapOf(Stats.SPECIAL_DEFENCE to 2),
        "calmmind" to mapOf(Stats.SPECIAL_ATTACK to 1, Stats.SPECIAL_DEFENCE to 1),
        "geomancy" to mapOf(Stats.SPECIAL_ATTACK to 2, Stats.SPECIAL_DEFENCE to 2, Stats.SPEED to 2),
        "nastyplot" to mapOf(Stats.SPECIAL_ATTACK to 2),
        "quiverdance" to mapOf(Stats.SPECIAL_ATTACK to 1, Stats.SPECIAL_DEFENCE to 1, Stats.SPEED to 1),
        "tailglow" to mapOf(Stats.SPECIAL_ATTACK to 3),
        "takeheart" to mapOf(Stats.SPECIAL_ATTACK to 1, Stats.SPECIAL_DEFENCE to 1),
        "agility" to mapOf(Stats.SPEED to 2),
        "autotomize" to mapOf(Stats.SPEED to 2),
        "rockpolish" to mapOf(Stats.SPEED to 2),
        "curse" to mapOf(Stats.ATTACK to 1, Stats.DEFENCE to 1, Stats.SPEED to -1),
        "minimize" to mapOf(Stats.EVASION to 2)
    )

    val entryHazards = listOf("spikes", "stealthrock", "stickyweb", "toxicspikes")
    val antiHazardsMoves = listOf("rapidspin", "defog", "tidyup")
    val antiBoostMoves = listOf("slearsmog","haze")
    val pivotMoves = listOf("uturn","flipturn", "partingshot", "batonpass", "chillyreception","shedtail", "voltswitch", "teleport")
    val setupMoves = setOf("tailwind", "trickroom", "auroraveil", "lightscreen", "reflect")
    val selfRecoveryMoves = listOf("healorder", "milkdrink", "recover", "rest", "roost", "slackoff", "softboiled")
    val weatherSetupMoves = mapOf(
        "chillyreception" to "Snow",
        "hail" to "Hail",
        "raindance" to "RainDance",
        "sandstorm" to "Sandstorm",
        "snowscape" to "Snow",
        "sunnyday" to "SunnyDay"
    )
}