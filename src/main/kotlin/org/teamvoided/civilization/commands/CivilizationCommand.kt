package org.teamvoided.civilization.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import net.minecraft.server.command.CommandManager.literal
import net.minecraft.server.command.ServerCommandSource
import org.teamvoided.civilization.commands.argument.SettlementArgumentType
import org.teamvoided.civilization.commands.argument.SettlementArgumentType.settlementArg
import org.teamvoided.civilization.commands.permisions.Perms
import org.teamvoided.civilization.commands.permisions.Perms.require
import org.teamvoided.civilization.data.*
import org.teamvoided.civilization.managers.NationManager
import org.teamvoided.civilization.managers.SettlementManager
import org.teamvoided.civilization.util.tText
import org.teamvoided.civilization.util.teleport

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

        /*
                val infoNode = literal("info").executes(::info).build()
                civilizationNode.addChild(infoNode)

                val helpNode = literal("help").executes(::help).build()
                civilizationNode.addChild(helpNode)

                val menuNode = literal("menu").executes(::menu).build()
                civilizationNode.addChild(menuNode)
        */

        dispatcher.register(literal("civ").redirect(civilizationNode))
    }

    private fun load(c: CommandContext<ServerCommandSource>): Int {
        val src = c.source
        val server = src.server
        val world = src.world
        val t1 = SettlementManager.load(server, world)
        val t2 = NationManager.load()
        if (t1 != 1 || t2 != 1) {
            src.sendSystemMessage(tText("Failed to load or to start loading!"))

            return 0
        }
        src.sendSystemMessage(tText("Files loaded successfully!"))

        return 1
    }

    private fun save(c: CommandContext<ServerCommandSource>): Int {
        val src = c.source
        val server = src.server
        val world = src.world
        val t1 = SettlementManager.save(server, world)
        val t2 = NationManager.save()
        if (t1 != 1 || t2 != 1) {
            src.sendSystemMessage(tText("Failed to save or to start saving!"))

            return 0
        }
        src.sendSystemMessage(tText("Files saved successfully!"))

        return 1
    }

    private fun tp(c: CommandContext<ServerCommandSource>, settlement: Settlement): Int {
        val src = c.source
        val player = src.player ?: return 0

        settlement.center

        player.teleport(settlement.center)
        src.sendSystemMessage(tText("Teleported to %s!", settlement.name))
        return 1
    }

}




interface CivCommandError : CommandError

