package org.teamvoided.civilization.util

import arrow.core.Either
import arrow.core.getOrElse
import arrow.core.raise.either
import arrow.core.raise.ensureNotNull
import com.mojang.brigadier.context.CommandContext
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.MinecraftServer
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text
import net.minecraft.world.World
import org.teamvoided.civilization.Civilization.log
import org.teamvoided.civilization.data.CommandError
import org.teamvoided.civilization.data.GenericCommandError
import org.teamvoided.civilization.data.SenderIsNotPlayerError
import org.teamvoided.civilization.data.WitheText


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


typealias ServerWorldFn = (src: ServerCommandSource, server: MinecraftServer, world: World) -> Either<CommandError, Int>

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
        fn(src, server, world).bind()
    }.getOrElse {
        if (it is WitheText) src.error(it.text())
        else src.tError(it.key())
        0
    }

}

typealias ServerWorldNullPlayerFn = (src: ServerCommandSource, server: MinecraftServer, world: World, player: PlayerEntity?) -> Either<CommandError, Int>

fun CommandContext<ServerCommandSource>.serverWorldNullPlayer(fn: ServerWorldNullPlayerFn): Int =
    serverWorld { src, server, world ->
        val player = src.player
        fn(src, server, world, player)
    }

typealias ServerWorldPlayerFn = (src: ServerCommandSource, server: MinecraftServer, world: World, player: PlayerEntity) -> Either<CommandError, Int>

fun CommandContext<ServerCommandSource>.serverWorldPlayer(fn: ServerWorldPlayerFn): Int =
    serverWorld { src, server, world ->
        either {
            val player = src.player
            ensureNotNull(player) { SenderIsNotPlayerError() }
            fn(src, server, world, player).bind()
        }
    }

