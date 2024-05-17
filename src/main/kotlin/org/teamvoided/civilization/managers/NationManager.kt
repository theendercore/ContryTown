package org.teamvoided.civilization.managers

import kotlinx.serialization.builtins.ListSerializer
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import org.teamvoided.civilization.Civilization.log
import org.teamvoided.civilization.compat.WebMaps
import org.teamvoided.civilization.data.Nation
import org.teamvoided.civilization.data.ResultType
import org.teamvoided.civilization.util.Util
import org.teamvoided.civilization.util.Util.tTxt
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.util.*

object NationManager {
    private val nations: MutableList<Nation> = mutableListOf()
    private var canReadFiles = true

    fun postServerInit(server: MinecraftServer) {
        load()
    }

    fun getById(id: UUID): Nation? = nations.find { it.id == id }
    fun getByName(name: String): Nation? = nations.find { it.nameId == name }

    fun getAllNations(): List<Nation> {
        return nations.toList()
    }

    fun addNation(name: String, player: ServerPlayerEntity): Pair<ResultType, Text> {
        val leader = player.uuid
        val data = PlayerDataManager.getDataD(player)
        if (data == null) return Pair(
            ResultType.FAIL,
            tTxt("You are not in a settlement you cant crete a nation one!")
        )
        if (!data.settlements.containsValue(PlayerDataManager.Role.LEADER)) return Pair(
            ResultType.FAIL,
            tTxt("You are not in a settlement leader you cant crete a nation!")
        )
        if (!data.nations.isNullOrEmpty()) return Pair(
            ResultType.FAIL, tTxt("You are in a settlement you cant crete a new one!")
        )
        val settlement =
            SettlementManager.getById(data.settlements.filterValues { it == PlayerDataManager.Role.LEADER }.keys.first())
        if (settlement == null) throw Error("[NationManager:addNation] Settlement not found but player has settlement data!")

        val newNation = Nation(name, settlement, leader)
        nations.add(newNation)

        settlement.isCapital = true
        settlement.nation = newNation.id
        SettlementManager.updateSettlement(settlement)

        PlayerDataManager.setDataD(
            player,
            PlayerDataManager.PlayerData(data.settlements, mapOf(Pair(newNation.id, PlayerDataManager.Role.LEADER)))
        )
        WebMaps.addNation(newNation)
        return Pair(ResultType.SUCCESS, tTxt("Successfully created a nation!"))
    }

    fun save(): Int {
        if (canReadFiles) {
            canReadFiles = false
            Thread {
                try {
                    FileWriter(getNationSaveFile()).use {
                        it.write(Util.json.encodeToString(ListSerializer(Nation.serializer()), nations))
                    }
                    log.info("Successfully saved Nations!")
                } catch (e: Exception) {
                    log.error("Failed to save Nations to file! \n {}", e.stackTrace)
                }
                canReadFiles = true
            }.start()
        } else {
            log.warn("Tired to write Nation files when couldn't!")
            return 0
        }
        return 1
    }

    fun load(): Int {
        if (canReadFiles) {
            canReadFiles = false
            try {
                val stringData = FileReader(getNationSaveFile()).use { it.readText() }
                nations.clear()
                nations.addAll(Util.json.decodeFromString(ListSerializer(Nation.serializer()), stringData))
            } catch (e: Exception) {
                log.error("Failed to read Nations from file! \n {}", e.stackTrace)
            }
            canReadFiles = true
        } else {
            log.warn("Tired to read Nation files when couldn't!")
            return 0
        }
        return 1
    }

    private fun getNationSaveFile(): File =
        Util.getGlobalPath().resolve("nations.json").toFile()
}
