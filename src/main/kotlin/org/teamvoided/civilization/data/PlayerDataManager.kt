package org.teamvoided.civilization.data

import eu.pb4.playerdata.api.PlayerDataApi
import eu.pb4.playerdata.api.storage.JsonDataStorage
import eu.pb4.playerdata.api.storage.PlayerDataStorage
import net.minecraft.server.network.ServerPlayerEntity
import java.util.*

object PlayerDataManager {
    private val PLAYER_DATA: PlayerDataStorage<PlayerData> = JsonDataStorage("civilization", PlayerData::class.java)

    fun init() {
        PlayerDataApi.register(PLAYER_DATA)
    }

    fun getData(player: ServerPlayerEntity): PlayerData? = PlayerDataApi.getCustomDataFor(player, PLAYER_DATA)
    fun setData(player: ServerPlayerEntity, data: PlayerData) =
        PlayerDataApi.setCustomDataFor(player, PLAYER_DATA, data)

    fun clear(player: ServerPlayerEntity) = PlayerDataApi.setCustomDataFor(player, PLAYER_DATA, null)

    data class PlayerData(val settlements: Map<UUID, Role>, val nations: Map<UUID, Role>? = null)

    enum class Role { CITIZEN, COUNCIL_DELEGATE, LEADER }
}