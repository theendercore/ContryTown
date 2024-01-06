package org.teamvoided.civilization.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import net.minecraft.server.command.CommandManager.literal
import net.minecraft.server.command.ServerCommandSource
import org.teamvoided.civilization.data.NationManager
import org.teamvoided.civilization.data.SettlementManager
import org.teamvoided.civilization.util.Util.tText

object CivilizationCommand {

    fun init(dispatcher: CommandDispatcher<ServerCommandSource>) {
        val civilizationNode = literal("civilization").build()
        dispatcher.root.addChild(civilizationNode)

        val loadNode = literal("load").executes(::load).build()
        civilizationNode.addChild(loadNode)

        val saveNode = literal("save").executes(::save).build()
        civilizationNode.addChild(saveNode)

        val infoNode = literal("info").executes(::info).build()
        civilizationNode.addChild(infoNode)

        val helpNode = literal("help").executes(::help).build()
        civilizationNode.addChild(helpNode)

        val menuNode = literal("menu").executes(::menu).build()
        civilizationNode.addChild(menuNode)


        dispatcher.register(literal("civ").redirect(civilizationNode))
    }

    private fun load(c: CommandContext<ServerCommandSource>): Int {
        val src = c.source
        val server = src.server
        val world = src.world
        val t1 = SettlementManager.load(server, world)
        val t2 = NationManager.load(server, world)
        if (t1 != 1 || t2 != 1)
            src.sendSystemMessage(tText("Failed to load or to start loading!"))

        return 1
    }

    private fun save(c: CommandContext<ServerCommandSource>): Int {
        val src = c.source
        val server = src.server
        val world = src.world
        val t1 = SettlementManager.save(server, world)
        val t2 = NationManager.save(server, world)
        if (t1 != 1 || t2 != 1)
            src.sendSystemMessage(tText("Failed to save or to start saving!"))

        return 1
    }

    private fun info(c: CommandContext<ServerCommandSource>): Int {
        c.source.sendSystemMessage(tText("Info"))

        return 1
    }

    private fun help(c: CommandContext<ServerCommandSource>): Int {
        c.source.sendSystemMessage(tText("*put help here*"))

        return 1
    }

    private fun menu(c: CommandContext<ServerCommandSource>): Int {
        val src = c.source
        val world = src.world
        val player = src.player ?: return 0
        c.source.sendSystemMessage(tText("menu"))

        return 1
    }
}