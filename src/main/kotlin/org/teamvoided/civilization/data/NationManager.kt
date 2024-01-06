package org.teamvoided.civilization.data

import kotlinx.serialization.builtins.ListSerializer
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.world.World
import org.teamvoided.civilization.Civilization.LOGGER
import org.teamvoided.civilization.compat.WebMaps
import org.teamvoided.civilization.util.ResultType
import org.teamvoided.civilization.util.Util
import org.teamvoided.civilization.util.Util.tText
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

    fun addNation(name: String, player: ServerPlayerEntity): Pair<ResultType, Text> {
        val leader = player.uuid
        val data = PlayerDataManager.getData(player)
        if (data == null) return Pair(
            ResultType.FAIL,
            tText("You are not in a settlement you cant crete a nation one!")
        )
        if (!data.settlements.containsValue(PlayerDataManager.Role.LEADER)) return Pair(
            ResultType.FAIL,
            tText("You are not in a settlement leader you cant crete a nation!")
        )
        if (!data.nations.isNullOrEmpty()) return Pair(
            ResultType.FAIL, tText("You are in a settlement you cant crete a new one!")
        )
        val settlement =
            SettlementManager.getById(data.settlements.filterValues { it == PlayerDataManager.Role.LEADER }.keys.first())
        if (settlement == null) throw Error("[NationManager:addNation] Settlement not found but player has settlement data!")

        val newNation = Nation(name, settlement, leader)
        nations.add(newNation)

        PlayerDataManager.setData(player,
            PlayerDataManager.PlayerData(data.settlements, mapOf(Pair(newNation.id, PlayerDataManager.Role.LEADER)))
        )
        WebMaps.addNation(newNation)
        return Pair(ResultType.SUCCESS, tText("Successfully created a nation!"))
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