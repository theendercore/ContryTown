package org.teamvoided.civilization.util

import arrow.core.getOrElse
import arrow.core.raise.Raise
import arrow.core.raise.either
import arrow.core.raise.ensureNotNull
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.tree.CommandNode
import net.minecraft.server.MinecraftServer
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.world.World
import org.teamvoided.civilization.Civilization.log
import org.teamvoided.civilization.data.*


fun ServerCommandSource.tError(text: String, vararg args: Any) = this.sendError(tText(text, *args))
fun ServerCommandSource.tMessage(text: String, vararg args: Any) = this.sendSystemMessage(tText(text, *args))
fun ServerCommandSource.litMessage(text: String) = this.sendSystemMessage(lText(text))
fun ServerCommandSource.tFeedback(text: String, vararg args: Any, broadcast: Boolean = false) =
    this.sendFeedback(tText(text, *args).fn(), broadcast)

fun ServerCommandSource.litFeedback(text: String, broadcast: Boolean = false) =
    this.sendFeedback(lText(text).fn(), broadcast)

fun ServerCommandSource.error(text: Text) = this.sendError(text)
fun ServerCommandSource.message(text: Text) = this.sendSystemMessage(text)
fun ServerCommandSource.feedback(text: Text, broadcast: Boolean = false) = this.sendFeedback(text.fn(), broadcast)


typealias ServerWorldFn = Raise<CommandError>.(src: ServerCommandSource, server: MinecraftServer, world: World) -> Int

fun CommandContext<ServerCommandSource>.serverWorld(fn: ServerWorldFn): Int {
    val src = this.source
    if (src == null) {
        log.error("Source is null this should not happen!")
        return 0
    }
    return either {
        val server = src.server
        ensureNotNull(server) { GenericCommandError("server") }
        val world = src.world
        ensureNotNull(world) { GenericCommandError("world") }
        fn(src, server, world)
    }.getOrElse {
        if (it is WitheText) src.error(it.text())
        else src.tError(it.key())
        0
    }

}

typealias ServerWorldNullPlayerFn = Raise<CommandError>.(src: ServerCommandSource, server: MinecraftServer, world: World, player: ServerPlayerEntity?) -> Int

fun CommandContext<ServerCommandSource>.serverWorldNullPlayer(fn: ServerWorldNullPlayerFn): Int =
    serverWorld { src, server, world -> fn(src, server, world, src.player) }

typealias ServerWorldPlayerFn = Raise<CommandError>.(src: ServerCommandSource, server: MinecraftServer, world: World, player: ServerPlayerEntity) -> Int

fun CommandContext<ServerCommandSource>.serverWorldPlayer(fn: ServerWorldPlayerFn): Int =
    serverWorld { src, server, world ->
        val player = src.player
        ensureNotNull(player) { SenderIsNotPlayerError }
        fn(src, server, world, player)
    }


typealias ServerArgs<T> = ArgumentBuilder<ServerCommandSource, T>

fun <T : ServerArgs<T>> ServerArgs<T>.executes(action: (c: CommandContext<ServerCommandSource>, settlement: Settlement) -> Int): ServerArgs<T> =
    this.executes { action(it, org.teamvoided.civilization.commands.argument.SettlementArgumentType.getSettlement(it)) }


fun <S> CommandNode<S>.childOf(node: CommandNode<S>): CommandNode<S> {
    node.addChild(this)
    return this
}