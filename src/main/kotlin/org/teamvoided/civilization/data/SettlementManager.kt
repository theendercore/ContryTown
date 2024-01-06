package org.teamvoided.civilization.data

import eu.pb4.playerdata.api.PlayerDataApi
import eu.pb4.playerdata.api.storage.JsonDataStorage
import eu.pb4.playerdata.api.storage.PlayerDataStorage
import kotlinx.serialization.builtins.ListSerializer
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.ChunkPos
import net.minecraft.world.World
import org.teamvoided.civilization.Civilization.LOGGER
import org.teamvoided.civilization.compat.WebMaps
import org.teamvoided.civilization.util.BasicDirection
import org.teamvoided.civilization.util.ResultType
import org.teamvoided.civilization.util.Util
import org.teamvoided.civilization.util.Util.getModSavePath
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.util.*


@Suppress("MemberVisibilityCanBePrivate")
object SettlementManager {
    private val settlements: MutableList<Settlement> = mutableListOf()
    private val PLAYER_DATA: PlayerDataStorage<PlayerData> = JsonDataStorage("civilization", PlayerData::class.java)
    private var canReadFiles = true


    fun init() {
        PlayerDataApi.register(PLAYER_DATA)
    }

    fun postServerInit(server: MinecraftServer) {
        for (world in server.worlds) load(server, world)
    }

    @Suppress("unused")
    fun getById(id: String): Settlement? = settlements.find { it.id == UUID.fromString(id) }
    fun getByName(name: String): Settlement? = settlements.find { it.nameId == name }
    fun getSettledChunks(): Map<ChunkPos, UUID> =
        settlements.flatMap { set -> set.chunks.map { Pair(it, set.id) } }.toMap()

    fun addSettlement(
        name: String, player: ServerPlayerEntity, chunkPos: ChunkPos, capitalPos: BlockPos, dimension: Identifier
    ): Pair<ResultType, Text> {
        val leader = player.uuid
        val data = PlayerDataApi.getCustomDataFor(player, PLAYER_DATA)
        if (data != null && data.settlement.isNotEmpty()) return Pair(
            ResultType.FAIL, Text.translatable("You are in a settlement you cant crete a new one!")
        )
        if (!canCreateSettlementInDim(dimension)) return Pair(
            ResultType.FAIL, Text.translatable("Can't settle in this dimension")
        )
        if (getSettledChunks().contains(chunkPos)) return Pair(
            ResultType.FAIL, Text.translatable("This chunk has been settled already!")
        )
        val id = UUID.randomUUID()
        val newSet = Settlement(id, name, leader, chunkPos, capitalPos, dimension)
        settlements.add(newSet)

        PlayerDataApi.setCustomDataFor(player, PLAYER_DATA, PlayerData(mapOf(Pair(newSet.id, "leader"))))
        WebMaps.addSettlement(newSet)
        return Pair(ResultType.SUCCESS, Text.translatable("Successfully created a base!"))
    }

    fun addChunk(settlement: Settlement, pos: ChunkPos): Pair<ResultType, Text> {
        if (getSettledChunks().contains(pos)) return Pair(
            ResultType.FAIL, Text.translatable("This chunk has been settled already!")
        )
        val neighbors = getChunkNeighbours(pos).map { it.first }
        if (neighbors.isEmpty()) return Pair(
            ResultType.FAIL, Text.translatable("This chunk isn't connected to any settlements! Try /civ outpost")
        )
        settlement.chunks.add(pos)
        updateSettlement(settlement)
        WebMaps.modifySettlement(settlement)
        return Pair(ResultType.SUCCESS, Text.translatable("Chunk successfully added!"))
    }

    fun removeChunk(settlement: Settlement, pos: ChunkPos): Pair<ResultType, Text> {
        if (!getSettledChunks().contains(pos)) return Pair(
            ResultType.FAIL, Text.translatable("This chunk isn't part of your settlement!")
        )

        settlement.chunks.remove(pos)
        updateSettlement(settlement)
        WebMaps.modifySettlement(settlement)
        return Pair(ResultType.SUCCESS, Text.translatable("Chunk successfully removed!"))
    }

    fun updateSettlement(settlement: Settlement) {
        settlements[settlements.indexOf(settlement)] = settlement
    }

    fun getAllSettlement(): List<Settlement> {
        return settlements.toList()
    }

    fun getChunkNeighbours(pos: ChunkPos): List<Triple<UUID, ChunkPos, BasicDirection>> {
        val neighbors: MutableList<Triple<UUID, ChunkPos, BasicDirection>> = mutableListOf()
        for (dir in BasicDirection.entries) {
            val newPos = ChunkPos(pos.x + dir.x, pos.z + dir.z)
            getSettledChunks()[newPos]?.let { neighbors.add(Triple(it, newPos, dir)) }
        }
        return neighbors
    }

    private fun canCreateSettlementInDim(dim: Identifier?): Boolean {
        return dim != null
    }

    fun save(server: MinecraftServer, world: World) {
        if (canReadFiles) {
            canReadFiles = false
            Thread {
                try {
                    FileWriter(getSettlementSaveFile(server, world)).use {
                        it.write(Util.json.encodeToString(ListSerializer(Settlement.serializer()), settlements))
                    }
                } catch (e: Exception) {
                    LOGGER.error("Failed to save Settlements to file! \n {}", e.stackTrace)
                }
                canReadFiles = true
            }.start()
        } else LOGGER.warn("Tired to read files when could not!")

    }

    fun load(server: MinecraftServer, world: World) {
        if (canReadFiles) {
            canReadFiles = false
            try {
                val stringData = FileReader(getSettlementSaveFile(server, world)).use { it.readText() }
                settlements.clear()
                settlements.addAll(Util.json.decodeFromString(ListSerializer(Settlement.serializer()), stringData))
            } catch (e: Exception) {
                LOGGER.error("Failed to read Settlements from file! \n {}", e.stackTrace)
            }
            canReadFiles = true
        } else LOGGER.warn("Tired to read files when could not!")
    }

    private fun getSettlementSaveFile(server: MinecraftServer, world: World): File =
        getModSavePath(server, world).resolve("settlements.json").toFile()

    // (Settlement / Nation) | Role
    data class PlayerData(val settlement: Map<UUID, String>, val citizenship: Map<UUID, String>? = null)
}