package org.teamvoided.civilization.managers

import arrow.optics.copy
import eu.pb4.playerdata.api.PlayerDataApi
import eu.pb4.playerdata.api.storage.JsonDataStorage
import eu.pb4.playerdata.api.storage.PlayerDataStorage
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.MutableText
import org.teamvoided.civilization.data.Settlement
import org.teamvoided.civilization.util.buildText
import org.teamvoided.civilization.util.iface.Textable
import java.util.*

@Suppress("TooManyFunctions", "deprecation")
object PlayerDataManager {
    @Deprecated("Use PLAYER_SETTLEMENT_DATA instead!")
    private val PLAYER_DATA: PlayerDataStorage<PlayerData> = JsonDataStorage("civilization", PlayerData::class.java)
    private val PLAYER_SETTLEMENT_DATA: PlayerDataStorage<PlayerSettlementData> =
        JsonDataStorage("civilization", PlayerSettlementData::class.java)

    fun init() {
        PlayerDataApi.register(PLAYER_DATA)
        PlayerDataApi.register(PLAYER_SETTLEMENT_DATA)
    }

    fun MinecraftServer.validatePlayerData(player: ServerPlayerEntity) {
        val data = player.getSettlementData() ?: return
        //check if data is valid
        println("Validating Player Data $data")
    }

    @Deprecated("User setRole instead!")
    fun MinecraftServer.setLeader(id: UUID, settlement: Settlement) {
        val data = this.getDataD(id)
            ?.let { it.settlements[settlement.id] = SettlementRole.LEADER;it }
            ?: PlayerData(settlement, SettlementRole.LEADER)
        setDataD(id, data)
    }

    @Deprecated("User setRole instead!")
    fun ServerPlayerEntity.setLeader(settlement: Settlement) {
        val data = getDataD(this)
            ?.let { it.settlements[settlement.id] = SettlementRole.LEADER;it }
            ?: PlayerData(settlement, SettlementRole.LEADER)
        setDataD(this, data)
    }

    @Deprecated("User setRole instead!")
    fun MinecraftServer.removeLeader(id: UUID, settlement: Settlement) {
        this.getDataD(id)?.let {
            it.settlements[settlement.id] = SettlementRole.CITIZEN
            setDataD(id, it)
        }
    }

    @Deprecated("User setRole instead!")
    fun ServerPlayerEntity.removeLeader(settlement: Settlement) {
        getDataD(this)?.let {
            it.settlements[settlement.id] = SettlementRole.CITIZEN
            setDataD(this, it)
        }
    }

    @Deprecated("Use removeRole instead!")
    fun MinecraftServer.removesSettlement(id: UUID, settlement: Settlement) {
        this.getDataD(id)?.let {
            it.settlements.remove(settlement.id)
            setDataD(id, it)
        }
    }

    @Deprecated("User removeRole instead!")
    fun ServerPlayerEntity.removesSettlement(settlement: Settlement) {
        getDataD(this)?.let {
            it.settlements.remove(settlement.id)
            setDataD(this, it)
        }
    }

    @Deprecated("User getRoles instead!")
    fun ServerPlayerEntity.getSettlements(role: SettlementRole = SettlementRole.LEADER): List<Settlement>? {
        val data = getDataD(this) ?: return null
        val settlement =
            data.settlements.filterValues { it == role }.keys.mapNotNull { SettlementManager.getById(it) }

        return settlement.ifEmpty { null }
    }


    @Deprecated("Use getRole instead!" )
    fun ServerPlayerEntity.getRoleOld(setl: Settlement): SettlementRole? = getDataD(this)?.settlements?.get(setl.id)

    @Deprecated("Use getSettlementData instead!")
    fun getDataD(player: ServerPlayerEntity): PlayerData? = PlayerDataApi.getCustomDataFor(player, PLAYER_DATA)

    @Deprecated("Use setSettlementData instead!")
    fun setDataD(player: ServerPlayerEntity, data: PlayerData) =
        PlayerDataApi.setCustomDataFor(player, PLAYER_DATA, data)

    @Deprecated("Use getSettlementData instead!")
    private fun MinecraftServer.getDataD(id: UUID): PlayerData? = PlayerDataApi.getCustomDataFor(this, id, PLAYER_DATA)

    @Deprecated("Use setSettlementData instead!")
    private fun MinecraftServer.setDataD(id: UUID, data: PlayerData) =
        PlayerDataApi.setCustomDataFor(this, id, PLAYER_DATA, data)

    @Deprecated("Use setSettlementData instead!")
    fun clearD(player: ServerPlayerEntity) = PlayerDataApi.setCustomDataFor(player, PLAYER_DATA, null)


    // Get Settlement Data Internal
    private fun ServerPlayerEntity.getSettlementData(): PlayerSettlementData? =
        PlayerDataApi.getCustomDataFor(this, PLAYER_SETTLEMENT_DATA)

    private fun UUID.getSettlementData(server: MinecraftServer): PlayerSettlementData? =
        PlayerDataApi.getCustomDataFor(server, this, PLAYER_SETTLEMENT_DATA)


    // Set Settlement Data Internal
    private fun ServerPlayerEntity.setSettlementData(data: PlayerSettlementData?) =
        PlayerDataApi.setCustomDataFor(this, PLAYER_SETTLEMENT_DATA, data)

    private fun UUID.setSettlementData(server: MinecraftServer, data: PlayerSettlementData?) =
        PlayerDataApi.setCustomDataFor(server, this, PLAYER_SETTLEMENT_DATA, data)


    // Get Role
    fun ServerPlayerEntity.getRole(settlement: Settlement): SettlementRole? {
        return this.getSettlementData()?.roles?.get(settlement.id)
    }

    fun UUID.getRole(server: MinecraftServer, settlement: Settlement): SettlementRole? {
        return this.getSettlementData(server)?.roles?.get(settlement.id)
    }

    // Get Roles
    fun ServerPlayerEntity.getRoles(): Map<UUID, SettlementRole>? {
        return this.getSettlementData()?.roles
    }

    fun UUID.getRoles(server: MinecraftServer): Map<UUID, SettlementRole>? {
        return this.getSettlementData(server)?.roles
    }

    // Set Role
    fun ServerPlayerEntity.setRole(settlement: Settlement, role: SettlementRole = SettlementRole.CITIZEN) {
        val data = this.getSettlementData()
            ?.setRole(settlement, role)
            ?: PlayerSettlementData(settlement, role)

        this.setSettlementData(data)
    }

    fun UUID.setRole(server: MinecraftServer, settlement: Settlement, role: SettlementRole = SettlementRole.CITIZEN) {
        val data = this.getSettlementData(server)
            ?.setRole(settlement, role)
            ?: PlayerSettlementData(settlement, role)

        this.setSettlementData(server, data)
    }

    // Remove Role
    fun ServerPlayerEntity.removeRole(settlement: Settlement) {
        val data = this.getSettlementData()?.removeRole(settlement)

        this.setSettlementData(data)
    }

    fun UUID.removeRole(server: MinecraftServer, settlement: Settlement) {
        val data = this.getSettlementData(server)?.removeRole(settlement)

        this.setSettlementData(server, data)
    }


    // Is in Settlement
    fun ServerPlayerEntity.isInSettlement(): Boolean {
        return this.getSettlementData()?.roles?.isNotEmpty() ?: false
    }

    fun UUID.isInSettlement(server: MinecraftServer): Boolean {
        return this.getSettlementData(server)?.roles?.isNotEmpty() ?: false
    }


    data class PlayerSettlementData(private var settlementsToRoles: Map<UUID, SettlementRole>) : Textable {
        val roles: Map<UUID, SettlementRole> get() = settlementsToRoles

        constructor(settlement: Settlement, role: SettlementRole) : this(mapOf(settlement.id to role))

        fun setRole(settlement: Settlement, role: SettlementRole): PlayerSettlementData {
            return this.copy(settlementsToRoles = roles.filter { it.key != settlement.id } + (settlement.id to role))
        }

        fun removeRole(settlement: Settlement): PlayerSettlementData {
            return this.copy { settlementsToRoles = settlementsToRoles.filter { it.key != settlement.id } }
        }

        override fun toText(): MutableText = buildText {
            addList("settlements") {
                settlementsToRoles.forEach {
                    addObjectRaw {
                        add("id", SettlementManager.getName(it.key))
                        add("role", it.value.toString())
                    }
                }
            }
        }
    }

    // For now unused
    data class PlayerNationData(val nations: Map<UUID, NationRole>) : Textable {
        override fun toText() = buildText {
            addList("nations") {
                nations.forEach {
                    addObjectRaw {
                        add("id", NationManager.getName(it.key))
                        add("role", it.value.toString())
                    }
                }
            }
        }
    }


    @Deprecated("Use PlayerSettlementData instead! This will be removed in the future!")
    data class PlayerData(
        val settlements: MutableMap<UUID, SettlementRole>,
        val nations: Map<UUID, NationRole>? = null
    ) : Textable {
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

    enum class SettlementRole { CITIZEN, LEADER }
    enum class NationRole { CITIZEN, COUNCIL_DELEGATE, LEADER }
}
