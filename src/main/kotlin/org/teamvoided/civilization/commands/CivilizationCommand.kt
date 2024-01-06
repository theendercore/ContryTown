package org.teamvoided.civilization.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import net.minecraft.command.argument.MessageArgumentType
import net.minecraft.server.command.CommandManager.argument
import net.minecraft.server.command.CommandManager.literal
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text
import org.teamvoided.civilization.commands.argument.SettlementArgumentType
import org.teamvoided.civilization.data.NationManager
import org.teamvoided.civilization.data.Settlement
import org.teamvoided.civilization.data.SettlementManager
import org.teamvoided.civilization.util.ResultType

object CivilizationCommand {

    fun init(dispatcher: CommandDispatcher<ServerCommandSource>) {
        val civilizationNode = literal("civilization").build()
        dispatcher.root.addChild(civilizationNode)
        val addCiv = literal("add").build()
        civilizationNode.addChild(addCiv)
        val addCivName = argument("name", MessageArgumentType.message())
            .executes { civAdd(it, MessageArgumentType.getMessage(it, "name")) }.build()
        addCiv.addChild(addCivName)
        val readCiv = literal("read").executes(::civRead).build()
        civilizationNode.addChild(readCiv)
        val listCiv = literal("list").executes(::civList).build()
        civilizationNode.addChild(listCiv)
        val saveCiv = literal("save").executes(::civSave).build()
        civilizationNode.addChild(saveCiv)
        val claimCiv = literal("claim").build()
        civilizationNode.addChild(claimCiv)
        val claimCivName = argument("name", SettlementArgumentType.settlement())
            .executes { civClaim(it, SettlementArgumentType.getSettlement(it, "name")) }.build()
        claimCiv.addChild(claimCivName)
        val unclaimCiv = literal("unclaim").build()
        civilizationNode.addChild(unclaimCiv)
        val unclaimCivName = argument("name", SettlementArgumentType.settlement())
            .executes { civUnclaim(it, SettlementArgumentType.getSettlement(it, "name")) }.build()
        unclaimCiv.addChild(unclaimCivName)
        val infoCiv = literal("info").build()
        civilizationNode.addChild(infoCiv)
        val infoCivName = argument("name", SettlementArgumentType.settlement())
            .executes { civInfo(it, SettlementArgumentType.getSettlement(it, "name")) }.build()
        infoCiv.addChild(infoCivName)

        dispatcher.register(literal("civ").redirect(civilizationNode))
    }

    private fun civAdd(c: CommandContext<ServerCommandSource>, name: Text): Int {
        val src = c.source
        val world = src.world
        val player = src.player ?: return 0
        val results = SettlementManager.addSettlement(
            name.string, player, world.getChunk(player.blockPos).pos, player.blockPos, world.registryKey.value
        )

        if (results.first == ResultType.FAIL) {
            src.sendError(results.second)
            return 0
        }
        src.sendSystemMessage(results.second)
        return 1
    }

    private fun civRead(c: CommandContext<ServerCommandSource>): Int {
        val server = c.source.server
        val world = c.source.world
        SettlementManager.load(server, world)
        NationManager.load(server, world)

        return 1
    }

    private fun civInfo(c: CommandContext<ServerCommandSource>, settlement: Settlement): Int {
        c.source.sendSystemMessage(Text.of(settlement.toString()))
        return 1
    }

    private fun civList(c: CommandContext<ServerCommandSource>): Int {
        val src = c.source
        val settles = SettlementManager.getAllSettlement()
        if (settles.isEmpty()) {
            src.sendSystemMessage(Text.of("No settlements exists"))

            return 0
        }
        for (camp in settles) c.source.sendSystemMessage(Text.literal(camp.name))

        return 1
    }

    private fun civSave(c: CommandContext<ServerCommandSource>): Int {
        val src = c.source
        val server = src.server
        val world = src.world
        SettlementManager.save(server, world)
        NationManager.save(server, world)

        return 1
    }

    private fun civClaim(c: CommandContext<ServerCommandSource>, settlement: Settlement): Int {
        val src = c.source
        val world = src.world
        val player = src.player ?: return 0

        val results = SettlementManager.addChunk(settlement, world.getChunk(player.blockPos).pos)

        if (results.first == ResultType.FAIL) {
            src.sendError(results.second)
            return 0
        }
        src.sendSystemMessage(results.second)
        return 1
    }

    private fun civUnclaim(c: CommandContext<ServerCommandSource>, settlement: Settlement): Int {
        val src = c.source
        val world = src.world
        val player = src.player ?: return 0

        val results = SettlementManager.removeChunk(settlement, world.getChunk(player.blockPos).pos)

        if (results.first == ResultType.FAIL) {
            src.sendError(results.second)
            return 0
        }
        src.sendSystemMessage(results.second)
        return 1
    }

    private fun civGUI(c: CommandContext<ServerCommandSource>): Int {
        val src = c.source
        val world = src.world
        val player = src.player ?: return 0

        return 1
    }
}