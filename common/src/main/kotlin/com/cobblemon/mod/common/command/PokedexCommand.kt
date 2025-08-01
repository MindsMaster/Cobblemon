/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.command

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.CobblemonNetwork.sendPacket
import com.cobblemon.mod.common.api.permission.CobblemonPermissions
import com.cobblemon.mod.common.api.pokedex.*
import com.cobblemon.mod.common.api.pokedex.def.PokedexDef
import com.cobblemon.mod.common.api.pokedex.def.SimplePokedexDef
import com.cobblemon.mod.common.api.storage.player.PlayerInstancedDataStoreTypes
import com.cobblemon.mod.common.command.argument.DexArgumentType
import com.cobblemon.mod.common.command.argument.FormArgumentType
import com.cobblemon.mod.common.command.argument.SpeciesArgumentType
import com.cobblemon.mod.common.net.messages.client.SetClientPlayerDataPacket
import com.cobblemon.mod.common.pokemon.FormData
import com.cobblemon.mod.common.pokemon.Species
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.permission
import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.arguments.EntityArgument
import net.minecraft.commands.arguments.selector.EntitySelector
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation

object PokedexCommand {

    private const val NAME = "pokedex"
    private const val GRANT_NAME = "grant"
    private const val REVOKE_NAME = "revoke"
    fun register(dispatcher : CommandDispatcher<CommandSourceStack>) {
        val commandArgumentBuilder = Commands.literal(NAME)
        val grantCommandBuilder = Commands.literal(GRANT_NAME).then(
            Commands.argument("player", EntityArgument.player())
                .then(Commands.literal("all").then(
                    Commands.argument("dex", DexArgumentType.dex()).executes(::executeGrantAll)
                ))
                .then(Commands.literal("only").then(
                    Commands.argument("species", SpeciesArgumentType.species()).then(
                        Commands.argument("form", FormArgumentType.form()).executes(::executeGrantOnly)
                    )
                ))
        )
        val revokeCommandBuilder = Commands.literal(REVOKE_NAME).then(
            Commands.argument("player", EntityArgument.player())
                .then(Commands.literal("all").then(
                    Commands.argument("dex", DexArgumentType.dex()).executes(::executeRemoveAll)
                ))
                .then(Commands.literal("only").then(
                    Commands.argument("species", SpeciesArgumentType.species()).then(
                        Commands.argument("form", FormArgumentType.form()).executes(::executeRemoveOnly)
                    )
                ))
        )
        commandArgumentBuilder
            .permission(CobblemonPermissions.POKEDEX)
            .then(grantCommandBuilder)
            .then(revokeCommandBuilder)
            .then(Commands.literal("printcalculations")
                .then(Commands.argument("player", EntityArgument.player())
                    .executes(::printNationalDexValues)
                    .then(Commands.literal("all").executes(::printValuesGlobal))
                    .then(Commands.argument("dex", DexArgumentType.dex())
                        .executes(::printValues))))


        dispatcher.register(commandArgumentBuilder)
    }

    private fun executeGrantOnly(context: CommandContext<CommandSourceStack>): Int {
        val players = context.getArgument("player", EntitySelector::class.java).findPlayers(context.source)
        val species = context.getArgument("species", Species::class.java)
        val form = context.getArgument("form", FormData::class.java)
        val nationalDexEntries = Dexes.dexEntryMap[cobblemonResource("national")]?.getEntries()
            ?: Dexes.dexEntryMap.values.filter { it is SimplePokedexDef }.flatMap { it.getEntries() }
                .groupBy { it.speciesId }
                .map { it.value.reduce { a, b -> a.combinedWith(b) } }
        val completeEntry = nationalDexEntries.first { it.speciesId == species.resourceIdentifier }

        players.forEach {
            val dex = Cobblemon.playerDataManager.getPokedexData(it)
            val entry = dex.getOrCreateSpeciesRecord(species.resourceIdentifier)

            completeEntry.forms
                .filter { it.displayForm == form.name }
                .forEach { form ->
                    form.unlockForms.forEach { unlockForm ->
                        val formRecord = entry.getOrCreateFormRecord(unlockForm)
                        formRecord.setKnowledgeProgress(PokedexEntryProgress.CAUGHT)
                        formRecord.addAllShinyStatesAndGenders()
                    }
                }
            entry.addAspects(completeEntry.variations.flatMap { it.aspects }.toSet())
        }
        val selectorStr = if (players.size == 1) players.first().name.string else "${players.size} players"
        context.source.sendSystemMessage(
            Component.literal("Granted ${species.name}-${form.formOnlyShowdownId()} to $selectorStr")
        )
        return Command.SINGLE_SUCCESS
    }

    private fun executeRemoveOnly(context: CommandContext<CommandSourceStack>): Int {
        val players = context.getArgument("player", EntitySelector::class.java).findPlayers(context.source)
        val species = context.getArgument("species", Species::class.java)
        val form = context.getArgument("form", FormData::class.java)
        players.forEach {
            val dex = Cobblemon.playerDataManager.getPokedexData(it)
            dex.deleteFormRecord(species.resourceIdentifier, form.name.lowercase())
            dex.clearCalculatedValues()
            it.sendPacket(SetClientPlayerDataPacket(PlayerInstancedDataStoreTypes.POKEDEX, dex.toClientData()))
        }
        Cobblemon.playerDataManager.saveAllOfOneType(PlayerInstancedDataStoreTypes.POKEDEX)
        val selectorStr = if (players.size == 1) players.first().name.string else "${players.size} players"
        context.source.sendSystemMessage(
            Component.literal("Removed ${species.name}-${form.formOnlyShowdownId()} from $selectorStr")
        )
        return Command.SINGLE_SUCCESS
    }

    private fun executeGrantAll(context: CommandContext<CommandSourceStack>): Int {
        val players = context.getArgument("player", EntitySelector::class.java).findPlayers(context.source)
        val dexDef = context.getArgument("dex", PokedexDef::class.java)
        players.forEach { player ->
            val dex = Cobblemon.playerDataManager.getPokedexData(player)
            dexDef.getEntries().forEach { dexEntry ->
                val speciesRecord = dex.getOrCreateSpeciesRecord(dexEntry.speciesId)
                dexEntry.forms.forEach { form ->
                    form.unlockForms.forEach {unlockForm ->
                        val formRecord = speciesRecord.getOrCreateFormRecord(unlockForm)
                        formRecord.setKnowledgeProgress(PokedexEntryProgress.CAUGHT)
                        formRecord.addAllShinyStatesAndGenders()
                    }
                }
                speciesRecord.addAspects(dexEntry.variations.flatMap { it.aspects }.toSet())
            }
        }
        val selectorStr = if (players.size == 1) players.first().name.string else "${players.size} players"
        context.source.sendSystemMessage(
            Component.literal("Filled dex of $selectorStr")
        )
        return Command.SINGLE_SUCCESS
    }

    private fun executeRemoveAll(context: CommandContext<CommandSourceStack>): Int {
        val players = context.getArgument("player", EntitySelector::class.java).findPlayers(context.source)
        val dexDef = context.getArgument("dex", PokedexDef::class.java)
        players.forEach {
            val dex = Cobblemon.playerDataManager.getPokedexData(it)
            dexDef.getEntries().forEach {
                dex.deleteSpeciesRecord(it.speciesId)
            }
            dex.clearCalculatedValues()
            it.sendPacket(SetClientPlayerDataPacket(PlayerInstancedDataStoreTypes.POKEDEX, dex.toClientData()))
        }
        Cobblemon.playerDataManager.saveAllOfOneType(PlayerInstancedDataStoreTypes.POKEDEX)
        val selectorStr = if (players.size == 1) players.first().name.string else "${players.size} players"
        context.source.sendSystemMessage(
            Component.literal("Cleared dex of $selectorStr")
        )
        return Command.SINGLE_SUCCESS
    }

    private fun printValuesGlobal(context: CommandContext<CommandSourceStack>): Int {
        val player = context.getArgument("player", EntitySelector::class.java).findSinglePlayer(context.source)
        val dex = Cobblemon.playerDataManager.getPokedexData(player)
        var calculators = listOf(SeenCount, CaughtCount, SeenPercent, CaughtPercent)
        calculators.forEach {
            var value = dex.getGlobalCalculatedValue(it)
            val percentString = if (it.outputIsPercentage) "%" else ""

            context.source.sendSystemMessage(Component.literal("${it.javaClass.simpleName} result: $value$percentString"))
        }

        Dexes.dexEntryMap.forEach { (dexId, dexDef) ->
            val seenCompletion = dex.getDexCalculatedValue(dexId, SeenPercent)
            val caughtCompletion = dex.getDexCalculatedValue(dexId, CaughtPercent)

            // Send system message with completion percentages for each Dex
            context.source.sendSystemMessage(
                Component.literal("Completion for ${dexId}: Seen: ${seenCompletion}% Caught: ${caughtCompletion}%")
            )
        }

        return 1
    }

    private fun printValues(context: CommandContext<CommandSourceStack>): Int {
        val player = context.getArgument("player", EntitySelector::class.java).findSinglePlayer(context.source)
        val dex = Cobblemon.playerDataManager.getPokedexData(player)
        val dexDef = context.getArgument("dex", PokedexDef::class.java)
        var calculators = listOf(SeenCount, CaughtCount, SeenPercent, CaughtPercent)
        calculators.forEach {
            var value = dex.getDexCalculatedValue(dexDef.id, it)
            val percentString = if (it.outputIsPercentage) "%" else ""

            context.source.sendSystemMessage(Component.literal("${it.javaClass.simpleName} result for ${dexDef.id}: $value$percentString"))
        }

        return 1
    }

    private fun printNationalDexValues (context: CommandContext<CommandSourceStack>) : Int {
        val player = context.getArgument("player", EntitySelector::class.java).findSinglePlayer(context.source)
        val dex = Cobblemon.playerDataManager.getPokedexData(player)
        val dexDef = Dexes.dexEntryMap[ResourceLocation("cobblemon", "national")] ?: throw IllegalStateException("No National Dex")
        var calculators = listOf(SeenCount, CaughtCount, SeenPercent, CaughtPercent)
        calculators.forEach {
            var value = dex.getDexCalculatedValue(dexDef.id, it)
            val percentString = if (it.outputIsPercentage) "%" else ""

            context.source.sendSystemMessage(Component.literal("${it.javaClass.simpleName} result for ${dexDef.id}: $value$percentString"))
        }

        return 1
    }
}