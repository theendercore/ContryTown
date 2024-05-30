package org.teamvoided.civilization.commands

import arrow.core.raise.ensure
import arrow.core.raise.ensureNotNull
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import net.minecraft.command.argument.EntityArgumentType
import net.minecraft.server.command.CommandManager.argument
import net.minecraft.server.command.CommandManager.literal
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.world.World
import org.teamvoided.civilization.commands.argument.SettlementArgumentType
import org.teamvoided.civilization.commands.argument.SettlementArgumentType.settlementArg
import org.teamvoided.civilization.commands.permisions.Perms
import org.teamvoided.civilization.commands.permisions.Perms.require
import org.teamvoided.civilization.data.FailedToLoad
import org.teamvoided.civilization.data.FailedToSave
import org.teamvoided.civilization.data.SenderIsNotPlayerError
import org.teamvoided.civilization.data.Settlement
import org.teamvoided.civilization.managers.NationManager
import org.teamvoided.civilization.managers.SettlementManager
import org.teamvoided.civilization.util.*


object CivilizationCommand {


    @Suppress("MagicNumber", "UNUSED_VARIABLE")
    fun init(dispatcher: CommandDispatcher<ServerCommandSource>) {
        val civilizationNode = literal("civilization")
            .requires(Perms.CIV.require(0))
            .build()
            .childOf(dispatcher.root)

        val load = literal("load")
            .requires(Perms.CIV_LOAD.require(4))
            .executes(::load)
            .build()
            .childOf(civilizationNode)

        val save = literal("save")
            .requires(Perms.CIV_SAVE.require(4))
            .executes(::save)
            .build()
            .childOf(civilizationNode)

        val saveAll = literal("save_all")
            .requires(Perms.CIV_SAVE_ALL.require(4))
            .executes(::saveAll)
            .build()
            .childOf(civilizationNode)


        val tp = literal("tp")
            .requires(Perms.CIV_TP.require(2))
            .build()
            .childOf(civilizationNode)
        val tpSettlement = literal("settlement")
            .build()
            .childOf(tp)
        val tpSettlementName = settlementArg()
            .executes(::tp)
            .build()
            .childOf(tpSettlement)

        val delete = literal("delete")
            .requires(Perms.CIV_DELETE.require(4))
            .build()
            .childOf(civilizationNode)
        val deleteSettlement = literal("settlement")
            .build()
            .childOf(delete)
        val deleteSettlementName = settlementArg()
            .executes(::delete)
            .build()
            .childOf(deleteSettlement)

        val set = literal("set")
            .requires(Perms.CIV_SET.require(2))
            .build()
            .childOf(civilizationNode)
        val setSettlement = literal("settlement")
            .build()
            .childOf(set)
        val setSettlementName = settlementArg()
            .build()
            .childOf(setSettlement)
        val setSettlementLeader = literal("leader")
            .executes { setSettlementLeader(it, SettlementArgumentType.getSettlement(it), null) }
            .build()
            .childOf(setSettlementName)
        val setSettlementLeaderName = argument("player", EntityArgumentType.player())
            .executes {
                setSettlementLeader(
                    it, SettlementArgumentType.getSettlement(it), EntityArgumentType.getPlayer(it, "player")
                )
            }
            .build()
            .childOf(setSettlementLeader)

        dispatcher.register(literal("civ").redirect(civilizationNode))
    }

    private fun delete(c: CommandContext<ServerCommandSource>, settlement: Settlement): Int =
        c.serverWorld { src, server, _ ->
            SettlementManager.deleteSettlement(settlement, server)

            src.tFeedback(cmd("settlement", "deleted", "success"))
            1
        }

    private fun load(c: CommandContext<ServerCommandSource>): Int = c.serverWorld { src, server, world ->
        val t1 = SettlementManager.load(server, world)
        ensure(t1 >= 1) { FailedToLoad("settlement") }
        val t2 = NationManager.load()
        ensure(t2 >= 1) { FailedToLoad("nation") }

        src.tFeedback(cmd("load", "success"))
        1
    }

    private fun save(c: CommandContext<ServerCommandSource>): Int = c.serverWorld { src, server, world ->
        val t1 = SettlementManager.save(server, world)
        ensure(t1 >= 1) { FailedToSave("settlement") }
        val t2 = NationManager.save()
        ensure(t2 >= 1) { FailedToSave("nation") }

        src.tFeedback(cmd("save", "success"))
        1
    }

    private fun loadAll(c: CommandContext<ServerCommandSource>): Int = c.serverWorld { src, server, world ->
        SettlementManager.loadAll(server)
        src.tFeedback(cmd("load", "all", "success"))
        1
    }

    private fun saveAll(c: CommandContext<ServerCommandSource>): Int = c.serverWorld { src, server, _ ->
        SettlementManager.saveAll(server)
        src.tFeedback(cmd("save", "all", "success"))
        1
    }

    private fun tp(c: CommandContext<ServerCommandSource>, settlement: Settlement): Int =
        c.serverWorldPlayer { src, _, _, player ->
            player.teleport(settlement.center)
            src.tFeedback(cmd("teleported", "to", "%s"), settlement.name)
            return@serverWorldPlayer 1
        }

    private fun setSettlementLeader(
        c: CommandContext<ServerCommandSource>, settlement: Settlement, leader: ServerPlayerEntity?
    ): Int = c.serverWorldNullPlayer { src, _, _, player ->
        val newLeader = leader ?: player
        ensureNotNull(newLeader) { SenderIsNotPlayerError }
        SettlementManager.setSettlementLeader(settlement, newLeader)
        src.tFeedback(cmd("set", "leader", "to", "%s"), newLeader.name.string)
        1
    }

    fun civ(vararg args: String): String = cmd("civilization", *args)
}
