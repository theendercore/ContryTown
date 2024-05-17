package org.teamvoided.civilization.commands.argument

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.CommandSyntaxException
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import net.minecraft.command.CommandSource
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text
import org.teamvoided.civilization.data.Nation
import org.teamvoided.civilization.managers.NationManager
import java.util.concurrent.CompletableFuture

object NationArgumentType {
    fun nationArg(name: String): RequiredArgumentBuilder<ServerCommandSource, String> {
        return CommandManager.argument(name, StringArgumentType.string()).suggests(::listSuggestions)
    }

    @Throws(CommandSyntaxException::class)
    fun getNation(context: CommandContext<ServerCommandSource>, name: String): Nation {
        val string = context.getArgument(name, String::class.java)
        val nation = NationManager.getByName(string)
        if (nation == null) {
            throw UNKNOWN_NATION_EXCEPTION.create(string)
        } else {
            return nation
        }
    }

    private fun <S> listSuggestions(
        commandContext: CommandContext<S>, suggestionsBuilder: SuggestionsBuilder
    ): CompletableFuture<Suggestions> {
        return if (commandContext.source is CommandSource) CommandSource.suggestMatching(
            NationManager.getAllNations().map { it.nameId }, suggestionsBuilder
        ) else Suggestions.empty()
    }

    private val UNKNOWN_NATION_EXCEPTION =
        DynamicCommandExceptionType { Text.translatable("Nation %s not found!", it) }
}
