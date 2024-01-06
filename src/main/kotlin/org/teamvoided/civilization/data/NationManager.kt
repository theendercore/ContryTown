package org.teamvoided.civilization.data

import kotlinx.serialization.builtins.ListSerializer
import net.minecraft.server.MinecraftServer
import net.minecraft.world.World
import org.teamvoided.civilization.Civilization.LOGGER
import org.teamvoided.civilization.util.Util
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.util.*

object NationManager {
    private val nations: MutableList<Nation> = mutableListOf()

    fun postServerInit(server: MinecraftServer) {
        for (world in server.worlds) load(server, world)
    }

    fun getById(id: String): Nation? = nations.find { it.id == UUID.fromString(id) }
    fun getByName(name: String): Nation? = nations.find { it.nameId == name }

    fun getAllNations(): List<Nation> {
        return nations.toList()
    }

    fun save(server: MinecraftServer, world: World) {
        Thread {
            try {
                FileWriter(getNationSaveFile(server, world)).use {
                    it.write(Util.json.encodeToString(ListSerializer(Nation.serializer()), nations))
                }
            } catch (e: Exception) {
                LOGGER.error("Failed to save Nations to file! \n {}", e.stackTrace)
            }
        }.start()
    }

    fun load(server: MinecraftServer, world: World) {
        try {
            val stringData = FileReader(getNationSaveFile(server, world)).use { it.readText() }
            nations.clear()
            nations.addAll(Util.json.decodeFromString(ListSerializer(Nation.serializer()), stringData))
        } catch (e: Exception) {
            LOGGER.error("Failed to read Nations from file! \n {}", e.stackTrace)
        }
    }

    private fun getNationSaveFile(server: MinecraftServer, world: World): File =
        Util.getModSavePath(server, world).resolve("nations.json").toFile()
}