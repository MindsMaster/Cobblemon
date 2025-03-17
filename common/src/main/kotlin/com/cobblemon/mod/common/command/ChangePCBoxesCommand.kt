package com.cobblemon.mod.common.command

import com.cobblemon.mod.common.api.permission.CobblemonPermissions
import com.cobblemon.mod.common.api.text.green
import com.cobblemon.mod.common.api.text.red
import com.cobblemon.mod.common.util.*
import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.context.CommandContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands.argument
import net.minecraft.commands.Commands.literal
import net.minecraft.commands.arguments.EntityArgument
import net.minecraft.commands.arguments.selector.EntitySelector

object ChangePCBoxesCommand {
    private const val NAME = "boxcount"
    fun register(dispatcher : CommandDispatcher<CommandSourceStack>) {
        dispatcher.register(literal(NAME)
                .permission(CobblemonPermissions.CHANGE_BOX_COUNT)
                    .then(literal("query").then(
                        argument("player", EntityArgument.player()).executes(::executeQuery))
                    )
                    .then(literal("add").then(
                        argument("player", EntityArgument.player()).then(
                        argument("amount", IntegerArgumentType.integer(1, 1000)).executes(::executeAdd)))
                    )
                    .then(literal("remove").then(
                        argument("player", EntityArgument.player()).then(
                        argument("amount", IntegerArgumentType.integer(1, 1000)).executes(::executeRemove)))
                    )
                    .then(literal("set").then(
                        argument("player", EntityArgument.player()).then(
                        argument("amount", IntegerArgumentType.integer(1, 1000)).executes(::executeSet)))
                    )
        )
    }

    private fun executeQuery(context: CommandContext<CommandSourceStack>): Int {
        val player = context.getArgument("player", EntitySelector::class.java).findSinglePlayer(context.source)
        val playerPc = player.pc()
        context.source.sendSystemMessage(lang("command.boxcount", player.name, playerPc.boxes.size))

        return Command.SINGLE_SUCCESS
    }

    private fun executeAdd(context: CommandContext<CommandSourceStack>): Int {
        val player = context.getArgument("player", EntitySelector::class.java).findSinglePlayer(context.source)
        val playerPc = player.pc()
        val amount = context.getArgument("amount", Int::class.java)

        playerPc.resize(playerPc.boxes.size + amount, true)
        playerPc.sendTo(player)
        context.source.sendSystemMessage(lang("command.changeboxcount", player.name, playerPc.boxes.size).green())

        return Command.SINGLE_SUCCESS
    }

    private fun executeRemove(context: CommandContext<CommandSourceStack>): Int {
        val player = context.getArgument("player", EntitySelector::class.java).findSinglePlayer(context.source)
        val playerPc = player.pc()
        val amount = context.getArgument("amount", Int::class.java)

        if (amount < playerPc.boxes.size) {
            val slicedBoxes = playerPc.boxes.slice(playerPc.boxes.size - amount until playerPc.boxes.size)
            val boxEmpty = slicedBoxes.all { box -> box.getNonEmptySlots().isEmpty() }

            if (boxEmpty) {
                playerPc.resize(playerPc.boxes.size - amount, true)
                playerPc.sendTo(player)
                context.source.sendSystemMessage(lang("command.changeboxcount", player.name, playerPc.boxes.size).green())
            }
            else {
                context.source.sendSystemMessage(lang("command.changeboxcount.removing_not_empty_box").red())
                return 0
            }

            return Command.SINGLE_SUCCESS
        }
        else {
            context.source.sendSystemMessage(lang("command.changeboxcount.removing_too_much_boxes", player.name, playerPc.boxes.size).red())
            return 0
        }
    }

    private fun executeSet(context: CommandContext<CommandSourceStack>): Int {
        val player = context.getArgument("player", EntitySelector::class.java).findSinglePlayer(context.source)
        val playerPc = player.pc()
        val amount = context.getArgument("amount", Int::class.java)

        if (amount < playerPc.boxes.size) {
            val slicedBoxes = playerPc.boxes.slice(amount until playerPc.boxes.size)
            val boxEmpty = slicedBoxes.all { box -> box.getNonEmptySlots().isEmpty() }

            if (!boxEmpty) {
                context.source.sendSystemMessage(lang("command.changeboxcount.removing_not_empty_box").red())
                return 0
            }
        }
        playerPc.resize(amount, true)
        playerPc.sendTo(player)
        context.source.sendSystemMessage(lang("command.changeboxcount", player.name, playerPc.boxes.size).green())

        return Command.SINGLE_SUCCESS
    }
}