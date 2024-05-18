package org.teamvoided.civilization.managers

import kotlinx.serialization.builtins.ListSerializer
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.ChunkPos
import net.minecraft.world.World
import org.teamvoided.civilization.Civilization.log
import org.teamvoided.civilization.compat.WebMaps
import org.teamvoided.civilization.config.CivilizationConfig
import org.teamvoided.civilization.data.Settlement
import org.teamvoided.civilization.data.BasicDirection
import org.teamvoided.civilization.data.ResultType
import org.teamvoided.civilization.util.Util
import org.teamvoided.civilization.util.Util.getWorldPath
import org.teamvoided.civilization.util.tTxt
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.util.*


@Suppress("MemberVisibilityCanBePrivate")
object SettlementManager {
    private val settlements: MutableList<Settlement> = mutableListOf()
    private val invitesList: MutableMap<UUID, MutableList<UUID>> = mutableMapOf()   //Player - <Settlements>
    private var canReadFiles: MutableList<Identifier> = mutableListOf() //World Identifier
    fun postServerInit(server: MinecraftServer) {
        for (world in server.worlds) load(server, world)
    }

    fun saveAll(server: MinecraftServer) {
        for (world in server.worlds) save(server, world)
    }

    @Suppress("unused")
    fun getById(id: UUID): Settlement? = settlements.find { it.id == id }
    fun getByName(name: String): Settlement? = settlements.find { it.nameId == name }
    fun getSettledChunks(): Map<ChunkPos, UUID> =
        settlements.flatMap { set -> set.getChunks().map { Pair(it, set.id) } }.toMap()

    fun addSettlement(
        name: String, player: ServerPlayerEntity, chunkPos: ChunkPos, capitalPos: BlockPos, dimension: Identifier
    ): Pair<ResultType, Text> {
        val data = PlayerDataManager.getDataD(player)
        if (data != null && data.settlements.isNotEmpty()) return Pair(
            ResultType.FAIL, tTxt("You are in a settlement you cant crete a new one!")
        )
        if (!canCreateSettlementInDim(dimension)) return Pair(
            ResultType.FAIL, tTxt("Can't settle in this dimension")
        )
        if (getSettledChunks().contains(chunkPos)) return Pair(
            ResultType.FAIL, tTxt("This chunk has been settled already!")
        )
        val id = UUID.randomUUID()
        val newSet = Settlement(id, name, player, chunkPos, capitalPos, dimension)
        settlements.add(newSet)

        PlayerDataManager.setDataD(
            player, PlayerDataManager.PlayerData(mutableMapOf(Pair(newSet.id, PlayerDataManager.Role.LEADER)))
        )
        WebMaps.addSettlement(newSet)
        return Pair(ResultType.SUCCESS, tTxt("Successfully created a base!"))
    }

    fun removeSettlement(
        settlement: Settlement, player: ServerPlayerEntity, confirmed: Boolean
    ): Pair<ResultType, Text> {
        if (settlement.leader != player.uuid) return Pair(
            ResultType.FAIL, tTxt("Only the leader can delete the settlement!")
        )

        if (!confirmed) return Pair(
            ResultType.LOGIC, tTxt("Are you sure you want to delete the claim?")
        )

        settlements.remove(settlement)
        WebMaps.removeSettlement(settlement)
        PlayerDataManager.clearD(player)
        return Pair(
            ResultType.SUCCESS,
            tTxt("Successfully delete a settlement %s!", settlement.name)
        )
    }

    fun addChunk(settlement: Settlement, pos: ChunkPos): Pair<ResultType, Text> {
        if (getSettledChunks().contains(pos)) return Pair(
            ResultType.FAIL, tTxt("This chunk has been settled already!")
        )
        val neighbors = getChunkNeighbours(pos).map { it.first }
        if (!neighbors.contains(settlement.id)) return Pair(
            ResultType.FAIL,
            tTxt("This chunk isn't connected to any settlements! If you want to make a separate claim do /settlement hamlet")
        )
        settlement.addChunk(pos)
        return Pair(ResultType.SUCCESS, tTxt("Chunk successfully added!"))
    }

    fun removeChunk(settlement: Settlement, pos: ChunkPos): Pair<ResultType, Text> {
        if (!getSettledChunks().contains(pos)) return Pair(
            ResultType.FAIL, tTxt("This chunk isn't part of your settlement!")
        )

        settlement.removeChunk(pos)
        updateSettlement(settlement)
        return Pair(ResultType.SUCCESS, tTxt("Chunk successfully removed!"))
    }

    fun updateSettlement(settlement: Settlement) {
        settlements[settlements.indexOf(settlement)] = settlement
        WebMaps.modifySettlement(settlement)
    }

    fun getAllSettlement(): List<Settlement> {
        return settlements.toList()
    }

    fun getInvites(player: UUID): List<Settlement>? {
        val invites = invitesList[player] ?: return null
        return invites.mapNotNull { getById(it) }
    }

    fun getInvite(player: UUID, settlement: Settlement): Settlement? {
        val invites = invitesList[player] ?: return null
        return invites.mapNotNull { getById(it) }.find { it.id == settlement.id }
    }

    fun addInvites(player: UUID, settlement: Settlement) {
        if (invitesList[player] == null) invitesList[player] = mutableListOf(settlement.id)
        invitesList[player]?.add(settlement.id)
    }

    fun removeInvite(player: UUID, settlement: Settlement): Int? {
        invitesList[player]?.remove(settlement.id) ?: return null

        return 1
    }
    fun clearInvites(player: UUID) = invitesList.remove(player)

    fun addCitizen(player: ServerPlayerEntity, settlement: Settlement) {
        settlement.addCitizen(player.uuid, player.name.string)
        updateSettlement(settlement)

        val data = PlayerDataManager.getDataD(player)
        PlayerDataManager.setDataD(
            player,
            if (data == null)
                PlayerDataManager.PlayerData(mutableMapOf(Pair(settlement.id, PlayerDataManager.Role.CITIZEN)))
            else {
                data.settlements[settlement.id] = PlayerDataManager.Role.CITIZEN
                data
            }
        )
    }

    fun removeCitizen(player: ServerPlayerEntity, settlement: Settlement) {
        settlement.removeCitizen(player.uuid)
        updateSettlement(settlement)

        val data = PlayerDataManager.getDataD(player) ?: return
        println(data)
        data.settlements.remove(settlement.id)
        println(data)
        PlayerDataManager.setDataD(player, data)
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
        return !CivilizationConfig.config().banedDimensions.contains(dim)
    }

    fun save(server: MinecraftServer, world: World): Int {
        val id = world.registryKey.value
        if (!canReadFiles.contains(id)) {
            canReadFiles.add(id)
            Thread {
                try {
                    FileWriter(getSettlementSaveFile(server, world)).use {
                        it.write(Util.json.encodeToString(ListSerializer(Settlement.serializer()), settlements))
                    }
                    log.info("Successfully saved {} worlds Settlements!", id)
                } catch (e: Exception) {
                    log.error("Failed to save Settlements to file! \n {}", e.stackTrace)
                }
                canReadFiles.remove(id)
            }.start()
        } else {
            log.warn("Tired to write Settlement files when couldn't!")
            return 0
        }
        return 1
    }

    fun load(server: MinecraftServer, world: World): Int {
        val id = world.registryKey.value
        if (!canReadFiles.contains(id)) {
            canReadFiles.add(id)
            try {
                val stringData = FileReader(getSettlementSaveFile(server, world)).use { it.readText() }
                val otherDim = settlements.filter { it.dimension != id }
                settlements.clear()
                settlements.addAll(otherDim)
                settlements.addAll(Util.json.decodeFromString(ListSerializer(Settlement.serializer()), stringData))
                log.info("Successfully read {} worlds Settlements!", id)
            } catch (e: Exception) {
                log.error("Failed to read Settlements from file! \n {}", e.stackTrace)
            }
            canReadFiles.remove(id)
        } else {
            log.warn("Tired to read Settlement files when couldn't!")
            return 0
        }
        return 1
    }

    private fun getSettlementSaveFile(server: MinecraftServer, world: World): File =
        getWorldPath(server, world).resolve("settlements.json").toFile()

}
