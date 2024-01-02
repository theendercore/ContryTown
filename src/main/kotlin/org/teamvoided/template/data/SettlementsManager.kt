package org.teamvoided.template.data

import eu.pb4.playerdata.api.PlayerDataApi
import eu.pb4.playerdata.api.storage.JsonDataStorage
import eu.pb4.playerdata.api.storage.PlayerDataStorage
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
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
import org.teamvoided.template.Civilisation.LOGGER
import org.teamvoided.template.compat.WebMaps
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.nio.file.Path
import java.util.*


object SettlementsManager {
    private val settlements: MutableList<Settlement> = mutableListOf()
    private val settledChunks: MutableList<ChunkPos> = mutableListOf()

    private val PLAYER_DATA: PlayerDataStorage<PlayerData> = JsonDataStorage("civilization", PlayerData::class.java)

    @OptIn(ExperimentalSerializationApi::class)
    private val json = Json { prettyPrint = true; prettyPrintIndent = "  " }

    fun init() {
        PlayerDataApi.register(PLAYER_DATA)
    }

    fun postServerInit(server: MinecraftServer) {
        for (world in server.worlds) load(server, world.registryKey)
    }

    fun getById(id: String): Settlement? = settlements.find { it.id == UUID.fromString(id) }
    fun getByName(name: String): Settlement? = settlements.find { it.name == name }
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
        if (settledChunks.contains(chunkPos)) return Pair(
            ResultType.FAIL, Text.translatable("This chunk has been settled already!")
        )

        val newSet = Settlement(
            UUID.randomUUID(),
            name,
            Settlement.SettlementType.BASE,
            mutableSetOf(leader),
            mutableSetOf(chunkPos),
            capitalPos,
            leader,
            null,
            dimension
        )
        settlements.add(newSet)
        settledChunks.add(chunkPos)

        PlayerDataApi.setCustomDataFor(player, PLAYER_DATA, PlayerData(mapOf(Pair(newSet.id, "leader"))))
        WebMaps.addSettlement(newSet)
        return Pair(ResultType.SUCCESS, Text.translatable("Successfully created a base!"))
    }

    fun addChunk(settlement: Settlement, pos: ChunkPos): Pair<ResultType, Text> {
        if (settledChunks.contains(pos)) return Pair(
            ResultType.FAIL, Text.translatable("This chunk has been settled already!")
        )
        settlement.chunks.add(pos)
        updateSettlement(settlement)
        WebMaps.modifySettlement(settlement)
        return Pair(ResultType.SUCCESS, Text.translatable("Chunk successfully added!"))
    }

    fun updateSettlement(settlement: Settlement) {
        val index = settlements.indexOfFirst { it.id == settlement.id }
        settlements[index] = settlement
    }

    fun getAllSettlement(): List<Settlement> {
        return settlements.toList()
    }

    private fun canCreateSettlementInDim(dim: Identifier): Boolean {
        return true
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
        Thread {
            try {
                FileWriter(getSettledChunksSaveFile(server, world.registryKey)).use { fw ->
                    fw.write(
                        json.encodeToString(ListSerializer(Long.serializer()), settledChunks.map { it.toLong() })
                    )
                }
            } catch (e: Exception) {
                LOGGER.error("Failed to save Settled Chunks to file! \n {}", e.stackTrace)
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
        try {
            val stringData = FileReader(getSettledChunksSaveFile(server, world)).use { it.readText() }
            settledChunks.clear()
            settledChunks.addAll(
                json.decodeFromString(ListSerializer(Long.serializer()), stringData).map { ChunkPos(it) })
        } catch (e: Exception) {
            LOGGER.error("Failed to read Settlements from file! \n {}", e.stackTrace)
        }
    }

    private fun getSettlementSaveFile(server: MinecraftServer, world: RegistryKey<World>): File {
        return getModSavePath(server, world).resolve("settlements.json").toFile()
    }

    private fun getSettledChunksSaveFile(server: MinecraftServer, world: RegistryKey<World>): File {
        return getModSavePath(server, world).resolve("settled_chunks.json").toFile()
    }

    private fun getModSavePath(server: MinecraftServer, world: RegistryKey<World>): Path {
        return DimensionType.getSaveDirectory(world, server.getSavePath(WorldSavePath.ROOT)).parent.resolve("data")
            .resolve("settlements")
    }

    enum class ResultType { SUCCESS, FAIL }

    // Country | Role
    data class PlayerData(val citizenship: Map<UUID, String>)
}