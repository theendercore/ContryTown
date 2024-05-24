package org.teamvoided.civilization.commands

import arrow.core.raise.either
import arrow.core.raise.ensure
import arrow.core.right
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import net.minecraft.server.command.CommandManager.literal
import net.minecraft.server.command.ServerCommandSource
import org.teamvoided.civilization.commands.argument.SettlementArgumentType
import org.teamvoided.civilization.commands.argument.SettlementArgumentType.settlementArg
import org.teamvoided.civilization.commands.permisions.Perms
import org.teamvoided.civilization.commands.permisions.Perms.require
import org.teamvoided.civilization.data.CommandError
import org.teamvoided.civilization.data.Settlement
import org.teamvoided.civilization.managers.NationManager
import org.teamvoided.civilization.managers.SettlementManager
import org.teamvoided.civilization.util.*


object CivilizationCommand {

    @Suppress("MagicNumber")
    fun init(dispatcher: CommandDispatcher<ServerCommandSource>) {
        val civilizationNode = literal("civilization").build()
        dispatcher.root.addChild(civilizationNode)

        val loadNode = literal("load")
            .requires(Perms.CIV_LOAD.require(4))
            .executes(::load)
            .build()
        civilizationNode.addChild(loadNode)

        val saveNode = literal("save")
            .requires(Perms.CIV_SAVE.require(4))
            .executes(::save)
            .build()
        civilizationNode.addChild(saveNode)


        val tpNode = literal("tp")
            .requires(Perms.CIV_TP.require(2))
            .build()
        civilizationNode.addChild(tpNode)
        val tpSetlNode = literal("settlement").build()
        tpNode.addChild(tpSetlNode)
        val tpSetlNameArg = settlementArg().executes { tp(it, SettlementArgumentType.getSettlement(it)) }.build()
        tpSetlNode.addChild(tpSetlNameArg)



        dispatcher.register(literal("civ").redirect(civilizationNode))
    }

    private fun load(c: CommandContext<ServerCommandSource>): Int = c.serverWorld { src, server, world ->
        either {
            val t1 = SettlementManager.load(server, world)
            ensure(t1 >= 1) { FailedToLoad("settlement") }
            val t2 = NationManager.load()

            ensure(t2 >= 1) { FailedToLoad("nation") }

            src.tFeedback(cmd("files", "loaded", "success"))
            1
        }
    }

    private fun save(c: CommandContext<ServerCommandSource>): Int = c.serverWorld { src, server, world ->
        either {
            val t1 = SettlementManager.save(server, world)
            ensure(t1 >= 1) { FailedToSave("settlement") }
            val t2 = NationManager.save()
            ensure(t2 >= 1) { FailedToSave("nation") }

            src.tFeedback(cmd("files", "saved", "success"))
            1
        }
    }

    private fun tp(c: CommandContext<ServerCommandSource>, settlement: Settlement): Int =
        c.serverWorldPlayer { src, _, _, player ->
            player.teleport(settlement.center)
            src.tFeedback(cmd("teleported", "to", "%s", settlement.name))
            return@serverWorldPlayer 1.right()
        }
}

interface CivCommandError : CommandError

data class FailedToLoad(val failedSection: String) : CivCommandError {
    override fun key(): String = civ("failed", "to", "load", failedSection)
}

data class FailedToSave(val failedSection: String) : CivCommandError {
    override fun key(): String = civ("failed", "to", "save", failedSection)
}

fun civ(vararg args: String): String = cmd("civilization", *args)
