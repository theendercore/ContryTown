package org.teamvoided.civilization.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import net.minecraft.command.argument.MessageArgumentType
import net.minecraft.server.command.CommandManager.argument
import net.minecraft.server.command.CommandManager.literal
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text
import org.teamvoided.civilization.commands.argument.NationArgumentType
import org.teamvoided.civilization.commands.argument.NationArgumentType.nationArg
import org.teamvoided.civilization.commands.argument.SettlementArgumentType
import org.teamvoided.civilization.data.Nation
import org.teamvoided.civilization.data.NationManager
import org.teamvoided.civilization.data.Settlement
import org.teamvoided.civilization.util.Util.emptyResult
import org.teamvoided.civilization.util.Util.lText
import org.teamvoided.civilization.util.Util.tText
object NationCommand {
    fun init(dispatcher: CommandDispatcher<ServerCommandSource>) {
        val nationNode = literal("nation").build()
        dispatcher.root.addChild(nationNode)


        val createNode = literal("create").build()
        nationNode.addChild(createNode)
        val createNodeNameArg = argument("name", MessageArgumentType.message())
            .executes { createNation(it, MessageArgumentType.getMessage(it, "name")) }.build()
        createNode.addChild(createNodeNameArg)

        val deleteNode = literal("delete").build()
        nationNode.addChild(deleteNode)
        val deleteNodeNameArg = nationArg("name")
            .executes { deleteNation(it, NationArgumentType.getNation(it, "name")) }.build()
        deleteNode.addChild(deleteNodeNameArg)

        val listNode = literal("list").executes(::list).build()
        nationNode.addChild(listNode)

        val infoNode = literal("info").build()
        nationNode.addChild(infoNode)
        val infoNodeNameArg = nationArg("name")
            .executes { info(it, NationArgumentType.getNation(it, "name")) }.build()
        infoNode.addChild(infoNodeNameArg)

        val addSettlementNode = literal("add_settlement").build()
        nationNode.addChild(addSettlementNode)
        val addSettlementNodeNameArg = SettlementArgumentType.settlementArg("name")
            .executes { addSettlement(it, SettlementArgumentType.getSettlement(it, "name")) }.build()
        addSettlementNode.addChild(addSettlementNodeNameArg)

        val removeNode = literal("remove_settlement").build()
        nationNode.addChild(removeNode)
        val removeNodeNameArg = SettlementArgumentType.settlementArg("name")
            .executes { removeSettlement(it, SettlementArgumentType.getSettlement(it, "name")) }.build()
        removeNode.addChild(removeNodeNameArg)

        val menuNode = literal("menu").executes(::menu).build()
        nationNode.addChild(menuNode)

        val requirementsNode = literal("requirements").executes(::requirements).build()
        nationNode.addChild(requirementsNode)
        val reqNode = literal("req").redirect(requirementsNode).build()
        dispatcher.root.addChild(reqNode)

        if (true) { //config.haveNatAlias
            val natNode = literal("nat").redirect(nationNode).build()
            dispatcher.root.addChild(natNode)
        }
    }

    private fun createNation(c: CommandContext<ServerCommandSource>, name: Text): Int {
        val src = c.source
        val player = src.player ?: return 0
        val results = NationManager.addNation(name.string, player)

        if (results.first.didFail()) {
            src.sendError(results.second)
            return 0
        }
        src.sendSystemMessage(results.second)
        return 1
    }
    private fun deleteNation(c: CommandContext<ServerCommandSource>, nation: Nation): Int {
        val src = c.source
        val player = src.player ?: return 0
        val results = emptyResult()
//            NationManager.removeNation(name.string, player)

        if (results.first.didFail()) {
            src.sendError(results.second)
            return 0
        }
        src.sendSystemMessage(results.second)
        return 1
    }
    private fun list(c: CommandContext<ServerCommandSource>): Int {
        val nations = NationManager.getAllNations()
        if (nations.isEmpty()) {
            c.source.sendSystemMessage(tText("No nations exists!"))
            return 0
        }
        c.source.sendSystemMessage(tText("Nations:"))
        for (nat in nations) c.source.sendSystemMessage(lText(" - ${nat.name}"))

        return 1
    }


    private fun info(c: CommandContext<ServerCommandSource>, nation: Nation): Int {
        c.source.sendSystemMessage(tText("TEST:"))
        c.source.sendSystemMessage(tText(nation.toString()))
        return 1
    }


    private fun addSettlement(c: CommandContext<ServerCommandSource>, settlement: Settlement): Int {
        val src = c.source
        val world = src.world
        val player = src.player ?: return 0

        val results = emptyResult()
//            NationManager.addSettlement(settlement, ...args)

        if (results.first.didFail()) {
            src.sendError(results.second)
            return 0
        }
        src.sendSystemMessage(results.second)
        return 1
    }

    private fun removeSettlement(c: CommandContext<ServerCommandSource>, settlement: Settlement): Int {
        val src = c.source
        val world = src.world
        val player = src.player ?: return 0

        val results =  emptyResult()
//            SettlementManager.removeSettlement(settlement, ...args)

        if (results.first.didFail()) {
            src.sendError(results.second)
            return 0
        }
        src.sendSystemMessage(results.second)
        return 1
    }

    private fun menu(c: CommandContext<ServerCommandSource>): Int {
        val src = c.source
        val world = src.world
        val player = src.player ?: return 0
        src.sendSystemMessage(tText("gui"))
        return 1
    }
    private fun requirements(c: CommandContext<ServerCommandSource>): Int {
        val src = c.source
        src.sendSystemMessage(tText("Requirements:"))
        src.sendSystemMessage(tText("None :)"))
        return 1
    }
}