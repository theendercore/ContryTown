package org.teamvoided.civilization.commands.argument

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.CommandSyntaxException
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry
import net.minecraft.command.CommandSource
import net.minecraft.command.argument.SingletonArgumentInfo
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text
import org.teamvoided.civilization.Civilization
import org.teamvoided.civilization.data.Settlement
import org.teamvoided.civilization.data.SettlementManager
import java.util.concurrent.CompletableFuture

class SettlementArgumentType : ArgumentType<String> {
    @Throws(CommandSyntaxException::class)
    override fun parse(stringReader: StringReader): String = stringReader.readUnquotedString()
    override fun <S> listSuggestions(
        commandContext: CommandContext<S>, suggestionsBuilder: SuggestionsBuilder
    ): CompletableFuture<Suggestions> {
        return if (commandContext.source is CommandSource) CommandSource.suggestMatching(
            SettlementManager.getAllSettlement().map { it.nameId }, suggestionsBuilder
        ) else Suggestions.empty()
    }

    override fun getExamples(): Collection<String> = EXAMPLES

    companion object {
        private val EXAMPLES = mutableListOf("this_cool_place", "town123")
        private val UNKNOWN_SETTLEMENT_EXCEPTION = DynamicCommandExceptionType {
            Text.method_54159("Settlement %s not found!", it)
        }

        fun settlement(): SettlementArgumentType {
            return SettlementArgumentType()
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

        fun init() {
            ArgumentTypeRegistry.registerArgumentType(
                Civilization.id("settlement"),
                SettlementArgumentType::class.java,
                SingletonArgumentInfo.contextFree(SettlementArgumentType::settlement)
            )
        }
    }
}