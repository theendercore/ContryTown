package org.teamvoided.civilization.data

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
import org.teamvoided.civilization.util.Util.getWorldPath
import org.teamvoided.civilization.util.Util.tText
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.util.*


@Suppress("MemberVisibilityCanBePrivate")
object SettlementManager {
    private val settlements: MutableList<Settlement> = mutableListOf()
    private var canReadFiles = true
    fun postServerInit(server: MinecraftServer) {
        for (world in server.worlds) load(server, world)
    }

    @Suppress("unused")
    fun getById(id: UUID): Settlement? = settlements.find { it.id == id }
    fun getByName(name: String): Settlement? = settlements.find { it.nameId == name }
    fun getSettledChunks(): Map<ChunkPos, UUID> =
        settlements.flatMap { set -> set.chunks.map { Pair(it, set.id) } }.toMap()

    fun addSettlement(
        name: String, player: ServerPlayerEntity, chunkPos: ChunkPos, capitalPos: BlockPos, dimension: Identifier
    ): Pair<ResultType, Text> {
        val leader = player.uuid
        val data = PlayerDataManager.getData(player)
        if (data != null && data.settlements.isNotEmpty()) return Pair(
            ResultType.FAIL, tText("You are in a settlement you cant crete a new one!")
        )
        if (!canCreateSettlementInDim(dimension)) return Pair(
            ResultType.FAIL, tText("Can't settle in this dimension")
        )
        if (getSettledChunks().contains(chunkPos)) return Pair(
            ResultType.FAIL, tText("This chunk has been settled already!")
        )
        val id = UUID.randomUUID()
        val newSet = Settlement(id, name, leader, chunkPos, capitalPos, dimension)
        settlements.add(newSet)

        PlayerDataManager.setData(
            player, PlayerDataManager.PlayerData(mapOf(Pair(newSet.id, PlayerDataManager.Role.LEADER)))
        )
        WebMaps.addSettlement(newSet)
        return Pair(ResultType.SUCCESS, tText("Successfully created a base!"))
    }

    fun addChunk(settlement: Settlement, pos: ChunkPos): Pair<ResultType, Text> {
        if (getSettledChunks().contains(pos)) return Pair(
            ResultType.FAIL, tText("This chunk has been settled already!")
        )
        val neighbors = getChunkNeighbours(pos).map { it.first }
        if (neighbors.isEmpty()) return Pair(
            ResultType.FAIL, tText("This chunk isn't connected to any settlements! Try /settlement hamlet")
        )
        settlement.chunks.add(pos)
        updateSettlement(settlement)
        WebMaps.modifySettlement(settlement)
        return Pair(ResultType.SUCCESS, tText("Chunk successfully added!"))
    }

    fun removeChunk(settlement: Settlement, pos: ChunkPos): Pair<ResultType, Text> {
        if (!getSettledChunks().contains(pos)) return Pair(
            ResultType.FAIL, tText("This chunk isn't part of your settlement!")
        )

        settlement.chunks.remove(pos)
        updateSettlement(settlement)
        WebMaps.modifySettlement(settlement)
        return Pair(ResultType.SUCCESS, tText("Chunk successfully removed!"))
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

    fun save(server: MinecraftServer, world: World): Int {
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
        } else {
            LOGGER.warn("Tired to write Settlement files when couldn't!")
            return 0
        }
        return 1
    }

    fun load(server: MinecraftServer, world: World): Int {
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
        } else {
            LOGGER.warn("Tired to read Settlement files when couldn't!")
            return 0
        }
        return 1
    }

    private fun getSettlementSaveFile(server: MinecraftServer, world: World): File =
        getWorldPath(server, world).resolve("settlements.json").toFile()

}