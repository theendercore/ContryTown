package org.teamvoided.civilization.commands.permisions

import me.lucko.fabric.api.permissions.v0.Permissions
import net.minecraft.server.command.ServerCommandSource
import org.teamvoided.civilization.Civilization.MODID
import java.util.function.Predicate

object Perms {

    val CIV = command("civilization")
    val CIV_LOAD = civilization("load")
    val CIV_SAVE = civilization("save")
    val CIV_TP = civilization("tp")


    val SETTLEMENT = command("settlement")
    val NATION = command("nation")

    private fun command(permission: String): String = "$MODID.command.$permission"

    private fun civilization(permission: String): String = "$MODID.command.civilization.$permission"

    fun String.require(enable: Boolean = false): Predicate<ServerCommandSource> = Permissions.require(this, enable)
    fun String.require(level: Int): Predicate<ServerCommandSource> = Permissions.require(this, level)


    object Groups {
        val ADMIN = group("admin")
        val SETTLEMENT_LEADER = group("settlement_leader")
        val NATIONAL_LEADER = group("national_leader")

        private fun group(permission: String): String = "group.$MODID.$permission"
    }
}
