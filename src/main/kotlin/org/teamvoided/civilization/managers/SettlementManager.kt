package org.teamvoided.civilization.managers

import arrow.core.Either
import arrow.core.raise.catch
import arrow.core.raise.either
import arrow.core.raise.ensure
import kotlinx.serialization.SerializationException
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
import org.teamvoided.civilization.data.*
import org.teamvoided.civilization.managers.PlayerDataManager.removeLeader
import org.teamvoided.civilization.managers.PlayerDataManager.removesSettlement
import org.teamvoided.civilization.managers.PlayerDataManager.setLeader
import org.teamvoided.civilization.util.JSON
import org.teamvoided.civilization.util.Util.getWorldPath
import org.teamvoided.civilization.util.tText
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException
import java.util.*


@Suppress("MemberVisibilityCanBePrivate", "TooManyFunctions")
object SettlementManager {
    private val settlements: MutableList<Settlement> = mutableListOf()
    private val invitesList: MutableMap<UUID, MutableList<UUID>> = mutableMapOf()   //Player - <Settlements>
    private var canReadFiles = true //World Identifier

    fun postServerInit(server: MinecraftServer) {
        loadAll(server)
    }

    fun loadAll(server: MinecraftServer): FailedToLoad? {
        for (world in server.worlds) {
            val success = load(server, world)
            if (success != 1) return FailedToLoad("settlements")
        }
        return null
    }

    fun saveAll(server: MinecraftServer): FailedToSave? {
        for (world in server.worlds) {
            val success = save(server, world)
            if (success != 1) return FailedToSave("settlements")
        }
        return null
    }

    fun getById(id: UUID): Settlement? = settlements.find { it.id == id }
    fun getByName(name: String): Settlement? = settlements.find { it.nameId == name }
    fun getByChunkPos(pos: ChunkPos): Settlement? = settlements.find { it.getChunks().contains(pos) }

    fun getName(id: UUID): String? = settlements.find { it.id == id }?.nameId

    fun getSettledChunks(): Map<ChunkPos, UUID> =
        settlements.flatMap { set -> set.getChunks().map { Pair(it, set.id) } }.toMap()

    fun addSettlement(
        name: String, player: ServerPlayerEntity, chunkPos: ChunkPos, capitalPos: BlockPos, dimension: Identifier
    ): Pair<ResultType, Text> {
        val data = PlayerDataManager.getDataD(player)
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
        val newSet = Settlement(id, name, player, chunkPos, capitalPos, dimension)
        settlements.add(newSet)

        PlayerDataManager.setDataD(
            player, PlayerDataManager.PlayerData(mutableMapOf(Pair(newSet.id, PlayerDataManager.SettlementRole.LEADER)))
        )
        WebMaps.addSettlement(newSet)
        return Pair(ResultType.SUCCESS, tText("Successfully created a base!"))
    }

    fun deleteSettlement(settlement: Settlement, server: MinecraftServer): Either<SettlementNotFound, Unit> = either {
        settlement.getCitizens().forEach { (id, _) -> server.removesSettlement(id, settlement) }
        ensure(settlements.remove(settlement)) { SettlementNotFound }
        WebMaps.removeSettlement(settlement)
    }

    fun removeSettlement(
        settlement: Settlement, player: ServerPlayerEntity, confirmed: Boolean
    ): Pair<ResultType, Text> {
        if (settlement.leader != player.uuid) return Pair(
            ResultType.FAIL, tText("Only the leader can delete the settlement!")
        )

        if (!confirmed) return Pair(
            ResultType.LOGIC, tText("Are you sure you want to delete the claim?")
        )

        settlements.remove(settlement)
        WebMaps.removeSettlement(settlement)
        player.removesSettlement(settlement)
        return Pair(
            ResultType.SUCCESS,
            tText("Successfully delete a settlement %s!", settlement.name)
        )
    }

    fun addChunk(settlement: Settlement, pos: ChunkPos): Pair<ResultType, Text> {
        if (getSettledChunks().contains(pos)) return Pair(
            ResultType.FAIL, tText("This chunk has been settled already!")
        )
        val neighbors = getChunkNeighbours(pos).map { it.first }
        if (!neighbors.contains(settlement.id)) return Pair(
            ResultType.FAIL,
            tText("This chunk isn't connected to any settlements! If you want to make a separate claim do /settlement hamlet")
        )
        settlement.addChunk(pos)
        return Pair(ResultType.SUCCESS, tText("Chunk successfully added!"))
    }

    fun removeChunk(settlement: Settlement, pos: ChunkPos): Pair<ResultType, Text> {
        if (!getSettledChunks().contains(pos)) return Pair(
            ResultType.FAIL, tText("This chunk isn't part of your settlement!")
        )

        settlement.removeChunk(pos)
        updateSettlement(settlement)
        return Pair(ResultType.SUCCESS, tText("Chunk successfully removed!"))
    }

    fun indexOf(settlement: Settlement): Int = settlements.indexOf(settlements.find { it.id == settlement.id }!!)
    fun updateSettlement(settlement: Settlement) {
        settlements[indexOf(settlement)] = settlement
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

    /* fun removeInvite(player: UUID, settlement: Settlement): Int? {
         invitesList[player]?.remove(settlement.id) ?: return null

         return 1
     }*/

    fun clearInvites(player: UUID) = invitesList.remove(player)

    fun addCitizen(player: ServerPlayerEntity, settlement: Settlement) {
        settlement.addCitizen(player.uuid, player.name.string)
        updateSettlement(settlement)

        val data = PlayerDataManager.getDataD(player)
        PlayerDataManager.setDataD(
            player,
            if (data == null)
                PlayerDataManager.PlayerData(
                    mutableMapOf(
                        Pair(
                            settlement.id,
                            PlayerDataManager.SettlementRole.CITIZEN
                        )
                    )
                )
            else {
                data.settlements[settlement.id] = PlayerDataManager.SettlementRole.CITIZEN
                data
            }
        )
    }

    fun removeCitizen(player: ServerPlayerEntity, settlement: Settlement) {
        settlement.removeCitizen(player.uuid)
        updateSettlement(settlement)

        val data = PlayerDataManager.getDataD(player) ?: return
        data.settlements.remove(settlement.id)
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

    // Is Synchronous - Only call this in a Thread
    fun save(server: MinecraftServer, world: World): Int {
        val id = world.registryKey.value
        if (canReadFiles) {
            canReadFiles = false
            catch({
                val file = getSettlementSaveFile(server, world)
                if (!file.exists()) {
                    throw IOException("File doesn't exist!")
                }
                FileWriter(file).use {
                    it.write(
                        JSON.encodeToString(
                            ListSerializer(Settlement.serializer()),
                            settlements.filter { setl -> setl.dimension == id })
                    )
                }
                log.info("Successfully saved {} worlds Settlements!", id)
            }, {
                val error = when (it) {
                    is SerializationException -> "Failed to serialize!"
                    is IOException -> "Failed to write to file!"
                    else -> it.message ?: "bad"
                }
                log.error("$error : {}", it.stackTrace)

            })
            canReadFiles = true
        } else {
            log.warn("Tired to write Settlement files when couldn't!")
            return 0
        }
        return 1
    }

    fun load(server: MinecraftServer, world: World): Int {
        val id = world.registryKey.value
        if (canReadFiles) {
            canReadFiles = false
            catch({
                val file = getSettlementSaveFile(server, world)
                if (!file.exists()) {
                    file.mkdirs()
                    throw IOException("File doesn't exist!")
                }
                val stringData = FileReader(file).use { it.readText() }
                val settlements = JSON.decodeFromString(ListSerializer(Settlement.serializer()), stringData)
                val otherDim = this.settlements.filter { it.dimension != id }
                this.settlements.clear()
                this.settlements.addAll(otherDim + settlements)
                log.info("Successfully read {} worlds Settlements!", id)
            }, {
                val error = when (it) {
                    is SerializationException -> "Failed to serialize, json invalid!"
                    is IllegalArgumentException -> "Failed to serialize, input is not a settlement list!"
                    is IOException -> "Failed to write to file!"
                    else -> it.message ?: "bad"
                }
                log.error("$error : {}", it.stackTrace)
            })
            canReadFiles = true
        } else {
            log.warn("Tired to read Settlement files when couldn't!")
            return 0
        }
        return 1
    }

    private fun getSettlementSaveFile(server: MinecraftServer, world: World): File =
        getWorldPath(server, world).resolve("settlements.json").toFile()

    fun setSettlementLeader(settlement: Settlement, newLeader: ServerPlayerEntity) {
        val oldLeaderId = settlement.leader
        updateSettlement(settlement.setLeader(newLeader))
        newLeader.server.removeLeader(oldLeaderId, settlement)
        newLeader.setLeader(settlement)
    }
}
