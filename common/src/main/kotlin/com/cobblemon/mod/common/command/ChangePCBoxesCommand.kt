package com.cobblemon.mod.common.command

import com.cobblemon.mod.common.api.permission.CobblemonPermissions
import com.cobblemon.mod.common.api.text.green
import com.cobblemon.mod.common.api.text.red
import com.cobblemon.mod.common.util.*
import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands.argument
import net.minecraft.commands.Commands.literal
import net.minecraft.commands.arguments.EntityArgument
import net.minecraft.server.level.ServerPlayer

object ChangePCBoxesCommand {
    private const val NAME = "boxcount"
    fun register(dispatcher : CommandDispatcher<CommandSourceStack>) {
        dispatcher.register(literal(NAME)
                .permission(CobblemonPermissions.CHANGE_BOX_COUNT)
                .then(argument("action", StringArgumentType.word())
                    .suggests { _, builder ->
                        builder
                            .suggest("add")
                            .suggest("remove")
                            .suggest("set")
                            .suggest("query")
                        builder.buildFuture()
                    }
                    .then(argument("player", EntityArgument.player())
                        .executes {
                            context ->
                            val player = context.player()
                            val action = StringArgumentType.getString(context, "action")
                            val number = 1
                            execute(context, player, action, number)
                        }
                        .then(argument("number", IntegerArgumentType.integer(1, 1000))
                            .executes {
                                context ->
                                val player = context.player()
                                val action = StringArgumentType.getString(context, "action")
                                val number = IntegerArgumentType.getInteger(context, "number")
                                execute(context, player, action, number)
                            } ))))

    }



    private fun execute(
        context: CommandContext<CommandSourceStack>,
        player:  ServerPlayer,
        action: String,
        number: Int
    ): Int {
        val playerPc = player.pc()

        when(action) {
            "add" -> {
                playerPc.resize(playerPc.boxes.size + number, true)
                playerPc.sendTo(player)
                context.source.sendSystemMessage((lang("command.changeboxcount", player.name, playerPc.boxes.size).green()))
            }
            "remove" -> {
                val slicedBoxes = playerPc.boxes.slice(playerPc.boxes.size - number until playerPc.boxes.size)
                val boxEmpty = slicedBoxes.all { box -> box.getNonEmptySlots().isEmpty() }

                if (boxEmpty) {
                    playerPc.resize(playerPc.boxes.size - number, true)
                    playerPc.sendTo(player)
                    context.source.sendSystemMessage((lang("command.changeboxcount", player.name, playerPc.boxes.size).green()))
                }
                else {
                    context.source.sendSystemMessage(lang("command.changeboxcount.removing_not_empty_box").red())
                }
            }
            "set" -> {
                if (number < playerPc.boxes.size) {
                    val slicedBoxes = playerPc.boxes.slice(number until playerPc.boxes.size)
                    val boxEmpty = slicedBoxes.all { box -> box.getNonEmptySlots().isEmpty() }

                    if (!boxEmpty) {
                        context.source.sendSystemMessage((lang("command.changeboxcount.removing_not_empty_box").red()))
                        return Command.SINGLE_SUCCESS
                    }
                }
                playerPc.resize(number, true)
                playerPc.sendTo(player)
                context.source.sendSystemMessage((lang("command.changeboxcount", player.name, playerPc.boxes.size).green()))
            }
            "query" -> {
                context.source.sendSystemMessage((lang("command.boxcount", player.name, playerPc.boxes.size)))
            }
        }
        return Command.SINGLE_SUCCESS
    }
}