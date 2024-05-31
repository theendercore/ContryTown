package org.teamvoided.civilization.managers

import eu.pb4.playerdata.api.PlayerDataApi
import eu.pb4.playerdata.api.storage.JsonDataStorage
import eu.pb4.playerdata.api.storage.PlayerDataStorage
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import org.teamvoided.civilization.data.Settlement
import org.teamvoided.civilization.util.buildText
import org.teamvoided.civilization.util.iface.Textable
import java.util.*

object PlayerDataManager {
    private val PLAYER_DATA: PlayerDataStorage<PlayerData> = JsonDataStorage("civilization", PlayerData::class.java)

    fun init() {
        PlayerDataApi.register(PLAYER_DATA)
    }

    fun MinecraftServer.validatePlayerData(player: ServerPlayerEntity) {
        val data = getDataD(player) ?: return
        //check if data is valid
        println("Validating Player Data $data")
    }

    fun MinecraftServer.setLeader(id: UUID, settlement: Settlement) {
        val data = this.getDataD(id)
            ?.let { it.settlements[settlement.id] = SettlementRole.LEADER;it }
            ?: PlayerData(settlement, SettlementRole.LEADER)
        setDataD(id, data)
    }

    fun ServerPlayerEntity.setLeader(settlement: Settlement) {
        val data = getDataD(this)
            ?.let { it.settlements[settlement.id] = SettlementRole.LEADER;it }
            ?: PlayerData(settlement, SettlementRole.LEADER)
        setDataD(this, data)
    }

    fun MinecraftServer.removeLeader(id: UUID, settlement: Settlement) {
        this.getDataD(id)?.let {
            it.settlements[settlement.id] = SettlementRole.CITIZEN
            setDataD(id, it)
        }
    }

    fun ServerPlayerEntity.removeLeader(settlement: Settlement) {
        getDataD(this)?.let {
            it.settlements[settlement.id] = SettlementRole.CITIZEN
            setDataD(this, it)
        }
    }

    fun MinecraftServer.removesSettlement(id: UUID, settlement: Settlement) {
        this.getDataD(id)?.let {
            it.settlements.remove(settlement.id)
            setDataD(id, it)
        }
    }

    fun ServerPlayerEntity.removesSettlement(settlement: Settlement) {
        getDataD(this)?.let {
            it.settlements.remove(settlement.id)
            setDataD(this, it)
        }
    }


    fun ServerPlayerEntity.getSettlements(role: SettlementRole = SettlementRole.LEADER): List<Settlement>? {
        val data = getDataD(this) ?: return null
        val settlement =
            data.settlements.filterValues { it == role }.keys.mapNotNull { SettlementManager.getById(it) }

        return settlement.ifEmpty { null }
    }

    fun ServerPlayerEntity.isInSettlement(): Boolean {
        val data = getDataD(this) ?: return false
        val settlement = data.settlements.keys.mapNotNull { SettlementManager.getById(it) }

        return settlement.isNotEmpty()
    }

    fun ServerPlayerEntity.getRole(setl: Settlement): SettlementRole? {
        return getDataD(this)?.settlements?.get(setl.id)
    }

    //    Refactor this later && fix the usages to not re-write data every time
    fun getDataD(player: ServerPlayerEntity): PlayerData? = PlayerDataApi.getCustomDataFor(player, PLAYER_DATA)
    fun setDataD(player: ServerPlayerEntity, data: PlayerData) =
        PlayerDataApi.setCustomDataFor(player, PLAYER_DATA, data)

    private fun MinecraftServer.getDataD(id: UUID): PlayerData? = PlayerDataApi.getCustomDataFor(this, id, PLAYER_DATA)
    private fun MinecraftServer.setDataD(id: UUID, data: PlayerData) =
        PlayerDataApi.setCustomDataFor(this, id, PLAYER_DATA, data)

    fun clearD(player: ServerPlayerEntity) = PlayerDataApi.setCustomDataFor(player, PLAYER_DATA, null)

    data class PlayerData(val settlements: MutableMap<UUID, SettlementRole>, val nations: Map<UUID, NationRole>? = null) : Textable {
        constructor(settlement: Settlement, role: SettlementRole) : this(mutableMapOf(settlement.id to role), null)

        override fun toText() = buildText {
            addList("settlements") {
                settlements.forEach {
                    addObjectRaw {
                        add("id", SettlementManager.getName(it.key))
                        add("role", it.value.toString())
                    }
                }
            }
            addList("nations") {
                if (nations != null) {
                    nations.forEach {
                        addObjectRaw {
                            add("id", NationManager.getName(it.key))
                            add("role", it.value.toString())
                        }
                    }
                } else add(null as String?)
            }
        }
    }

    enum class SettlementRole { CITIZEN,  LEADER }
    enum class NationRole { CITIZEN, COUNCIL_DELEGATE, LEADER }
}
