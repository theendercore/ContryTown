package org.teamvoided.civilization.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import net.minecraft.command.argument.MessageArgumentType
import net.minecraft.server.command.CommandManager.argument
import net.minecraft.server.command.CommandManager.literal
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text
import org.teamvoided.civilization.commands.argument.SettlementArgumentType
import org.teamvoided.civilization.data.Settlement
import org.teamvoided.civilization.data.SettlementManager
import org.teamvoided.civilization.util.Util.emptyResult
import org.teamvoided.civilization.util.Util.lText
import org.teamvoided.civilization.util.Util.tText

object SettlementCommand {
    fun init(dispatcher: CommandDispatcher<ServerCommandSource>) {
        val settlementNode = literal("settlement").build()
        dispatcher.root.addChild(settlementNode)


        val createNode = literal("create").build()
        settlementNode.addChild(createNode)
        val createNodeNameArg = argument("name", MessageArgumentType.message())
            .executes { createSettlement(it, MessageArgumentType.getMessage(it, "name")) }.build()
        createNode.addChild(createNodeNameArg)

        val deleteNode = literal("delete").build()
        settlementNode.addChild(deleteNode)
        val deleteNodeNameArg = argument("name", SettlementArgumentType.settlement())
            .executes { deleteSettlement(it, SettlementArgumentType.getSettlement(it, "name")) }.build()
        deleteNode.addChild(deleteNodeNameArg)

        val listNode = literal("list").executes(::list).build()
        settlementNode.addChild(listNode)

        val infoNode = literal("info").build()
        settlementNode.addChild(infoNode)
        val infoNodeNameArg = argument("name", SettlementArgumentType.settlement())
            .executes { info(it, SettlementArgumentType.getSettlement(it, "name")) }.build()
        infoNode.addChild(infoNodeNameArg)

        val claimNode = literal("claim").build()
        settlementNode.addChild(claimNode)
        val claimNodeNameArg = argument("name", SettlementArgumentType.settlement())
            .executes { claim(it, SettlementArgumentType.getSettlement(it, "name")) }.build()
        claimNode.addChild(claimNodeNameArg)

        val desertNode = literal("desert").build()
        settlementNode.addChild(desertNode)
        val desertNodeNameArg = argument("name", SettlementArgumentType.settlement())
            .executes { desert(it, SettlementArgumentType.getSettlement(it, "name")) }.build()
        desertNode.addChild(desertNodeNameArg)

        val menuNode = literal("menu").executes(::menu).build()
        settlementNode.addChild(menuNode)


        if (true) { //config.haveSetlAlias
            val setlNode = literal("setl").redirect(settlementNode).build()
            dispatcher.root.addChild(setlNode)
        }
    }

    private fun createSettlement(c: CommandContext<ServerCommandSource>, name: Text): Int {
        val src = c.source
        val world = src.world
        val player = src.player ?: return 0
        val results = SettlementManager.addSettlement(
            name.string, player, world.getChunk(player.blockPos).pos, player.blockPos, world.registryKey.value
        )
        if (results.first.didFail()) {
            src.sendError(results.second)
            return 0
        }
        src.sendSystemMessage(results.second)
        return 1
    }

    private fun deleteSettlement(c: CommandContext<ServerCommandSource>, settlement: Settlement): Int {
        val src = c.source
        val player = src.player ?: return 0
        val results = emptyResult()
//            SettlementManager.removeSettlement(settlement)

        if (results.first.didFail()) {
            src.sendError(results.second)
            return 0
        }
        src.sendSystemMessage(results.second)
        return 1
    }

    private fun list(c: CommandContext<ServerCommandSource>): Int {
        val settlements = SettlementManager.getAllSettlement()
        if (settlements.isEmpty()) {
            c.source.sendSystemMessage(tText("No settlements exists!"))
            return 0
        }
        c.source.sendSystemMessage(tText("Settlements:"))
        for (setl in settlements) c.source.sendSystemMessage(lText(" - ${setl.name}"))

        return 1
    }


    private fun info(c: CommandContext<ServerCommandSource>, settlement: Settlement): Int {
        c.source.sendSystemMessage(tText("TEST:"))
        c.source.sendSystemMessage(tText(settlement.toString()))
        return 1
    }


    private fun claim(c: CommandContext<ServerCommandSource>, settlement: Settlement): Int {
        val src = c.source
        val world = src.world
        val player = src.player ?: return 0

        val results = SettlementManager.addChunk(settlement, world.getChunk(player.blockPos).pos)

        if (results.first.didFail()) {
            src.sendError(results.second)
            return 0
        }
        src.sendSystemMessage(results.second)
        return 1
    }

    private fun desert(c: CommandContext<ServerCommandSource>, settlement: Settlement): Int {
        val src = c.source
        val world = src.world
        val player = src.player ?: return 0

        val results = SettlementManager.removeChunk(settlement, world.getChunk(player.blockPos).pos)

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

}