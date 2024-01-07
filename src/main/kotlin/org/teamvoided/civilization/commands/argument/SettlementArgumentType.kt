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
import org.teamvoided.civilization.data.Settlement
import org.teamvoided.civilization.data.SettlementManager
import org.teamvoided.civilization.util.Util.tText
import java.util.concurrent.CompletableFuture

object SettlementArgumentType {
    fun settlementArg(name: String): RequiredArgumentBuilder<ServerCommandSource, String> {
        return CommandManager.argument(name, StringArgumentType.string()).suggests(::listSuggestions)
    }

    @Throws(CommandSyntaxException::class)
    fun getSettlement(context: CommandContext<ServerCommandSource>, name: String): Settlement {
        val string = context.getArgument(name, String::class.java)
        val settlement = SettlementManager.getByName(string)
        if (settlement == null) {
            throw UNKNOWN_SETTLEMENT_EXCEPTION.create(string)
        } else {
            return settlement
        }
    }

    private fun <S> listSuggestions(
        commandContext: CommandContext<S>, suggestionsBuilder: SuggestionsBuilder
    ): CompletableFuture<Suggestions> {
        return if (commandContext.source is CommandSource) CommandSource.suggestMatching(
            SettlementManager.getAllSettlement().map { it.nameId }, suggestionsBuilder
        ) else Suggestions.empty()
    }

    private val UNKNOWN_SETTLEMENT_EXCEPTION =
        DynamicCommandExceptionType { tText("Settlement %s not found!", it) }
}