package com.cobblemon.mod.common.command.argument

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.CommandSyntaxException
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.SharedSuggestionProvider

class TransformTypeArgumentType: ArgumentType<TransformTypeArgumentType.Companion.TransformType>
{
    override fun parse(reader: StringReader): TransformType {
        try {
            reader.readString()?.let { return TransformType.valueOf(it.uppercase()) }
        }
        catch (_: Exception) { }
        throw  CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherParseException().create(reader)
    }

    override fun getExamples() = EXAMPLES

    override fun <S : Any> listSuggestions(context: CommandContext<S>, builder: SuggestionsBuilder) = SharedSuggestionProvider.suggest(EXAMPLES, builder)

    companion object {
        enum class TransformType {
            POSITION,
            ROTATION,
            SCALE
        }

        fun transformType() = TransformTypeArgumentType()

        fun getTransform(context: CommandContext<CommandSourceStack>, name: String): TransformType {
            return context.getArgument(name, TransformType::class.java )
        }

        private val EXAMPLES = TransformType.entries.map { it.name.lowercase() }
    }

}