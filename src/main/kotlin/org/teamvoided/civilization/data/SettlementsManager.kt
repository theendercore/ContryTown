package org.teamvoided.civilization.data

import eu.pb4.playerdata.api.PlayerDataApi
import eu.pb4.playerdata.api.storage.JsonDataStorage
import eu.pb4.playerdata.api.storage.PlayerDataStorage
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import net.minecraft.registry.RegistryKey
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.util.WorldSavePath
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.ChunkPos
import net.minecraft.world.World
import net.minecraft.world.dimension.DimensionType
import org.teamvoided.civilization.Civilization.LOGGER
import org.teamvoided.civilization.compat.WebMaps
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.nio.file.Path
import java.util.*


object SettlementsManager {
    private val settlements: MutableList<Settlement> = mutableListOf()
    private val PLAYER_DATA: PlayerDataStorage<PlayerData> = JsonDataStorage("civilization", PlayerData::class.java)

    @OptIn(ExperimentalSerializationApi::class)
    private val json = Json { prettyPrint = true; prettyPrintIndent = "  " }

    fun init() {
        PlayerDataApi.register(PLAYER_DATA)
    }

    fun postServerInit(server: MinecraftServer) {
        LOGGER.info("Server Loaded!")
        for (world in server.worlds) load(server, world.registryKey)
    }

    fun getById(id: String): Settlement? = settlements.find { it.id == UUID.fromString(id) }
    fun getByName(name: String): Settlement? = settlements.find { it.nameId == name }
    fun getSettledChunks(): Map<ChunkPos, UUID> =
        settlements.flatMap { set -> set.chunks.map { Pair(it, set.id) } }.toMap()

    fun addSettlement(
        name: String, player: ServerPlayerEntity, chunkPos: ChunkPos, capitalPos: BlockPos, dimension: Identifier
    ): Pair<ResultType, Text> {
        val leader = player.uuid
        val data = PlayerDataApi.getCustomDataFor(player, PLAYER_DATA)
        if (data != null && data.citizenship.isNotEmpty()) return Pair(
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
        val index = settlements.indexOfFirst { it.id == settlement.id }
        settlements[index] = settlement
    }

    fun getAllSettlement(): List<Settlement> {
        return settlements.toList()
    }

    fun getChunkNeighbours(pos: ChunkPos): List<Triple<UUID, ChunkPos, ChunkDirection>> {
        val neighbors: MutableList<Triple<UUID, ChunkPos, ChunkDirection>> = mutableListOf()
        for (dir in ChunkDirection.entries) {
            val newPos = ChunkPos(pos.x + dir.x, pos.z + dir.z)
            getSettledChunks()[newPos]?.let { neighbors.add(Triple(it, newPos, dir)) }
        }
        return neighbors
    }

    private fun canCreateSettlementInDim(dim: Identifier?): Boolean {
        return dim != null
    }

    fun save(server: MinecraftServer, world: World) {
        getModSavePath(server, world.registryKey).toFile().mkdirs()
        Thread {
            try {
                FileWriter(getSettlementSaveFile(server, world.registryKey)).use {
                    it.write(json.encodeToString(ListSerializer(Settlement.serializer()), settlements))
                }
            } catch (e: Exception) {
                LOGGER.error("Failed to save Settlements to file! \n {}", e.stackTrace)
            }
        }.start()
    }

    fun load(server: MinecraftServer, world: RegistryKey<World>) {
        try {
            val stringData = FileReader(getSettlementSaveFile(server, world)).use { it.readText() }
            settlements.clear()
            settlements.addAll(json.decodeFromString(ListSerializer(Settlement.serializer()), stringData))
        } catch (e: Exception) {
            LOGGER.error("Failed to read Settlements from file! \n {}", e.stackTrace)
        }
    }

    private fun getSettlementSaveFile(server: MinecraftServer, world: RegistryKey<World>): File {
        return getModSavePath(server, world).resolve("settlements.json").toFile()
    }

    private fun getModSavePath(server: MinecraftServer, world: RegistryKey<World>): Path {
        return DimensionType.getSaveDirectory(world, server.getSavePath(WorldSavePath.ROOT)).parent.resolve("data")
            .resolve("settlements")
    }

    enum class ResultType { SUCCESS, FAIL }

    // Country | Role
    data class PlayerData(val citizenship: Map<UUID, String>)

    enum class ChunkDirection(val x: Int, val z: Int) { UP(0, -1), LEFT(-1, 0), DOWN(0, 1), RIGHT(1, 0) }
}