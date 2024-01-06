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
import org.teamvoided.civilization.data.Nation
import org.teamvoided.civilization.data.NationManager
import java.util.concurrent.CompletableFuture

class NationArgumentType : ArgumentType<String> {
    @Throws(CommandSyntaxException::class)
    override fun parse(stringReader: StringReader): String = stringReader.readUnquotedString()
    override fun <S> listSuggestions(
        commandContext: CommandContext<S>, suggestionsBuilder: SuggestionsBuilder
    ): CompletableFuture<Suggestions> {
        return if (commandContext.source is CommandSource) CommandSource.suggestMatching(
            NationManager.getAllNations().map { it.nameId }, suggestionsBuilder
        ) else Suggestions.empty()
    }

    override fun getExamples(): Collection<String> = EXAMPLES

    companion object {
        private val EXAMPLES = mutableListOf("this_cool_nation", "fire_nation123")
        private val UNKNOWN_NATION_EXCEPTION = DynamicCommandExceptionType {
            Text.method_54159("Nation %s not found!", it)
        }

        fun nation(): NationArgumentType {
            return NationArgumentType()
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

        fun init() {
            ArgumentTypeRegistry.registerArgumentType(
                Civilization.id("nation"),
                NationArgumentType::class.java,
                SingletonArgumentInfo.contextFree(NationArgumentType::nation)
            )
        }
    }
}