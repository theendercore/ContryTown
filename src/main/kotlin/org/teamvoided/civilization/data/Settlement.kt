package org.teamvoided.civilization.data

import kotlinx.serialization.Serializable
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.ChunkPos
import org.teamvoided.civilization.serializers.BlockPosSerializer
import org.teamvoided.civilization.serializers.ChunkPosSerializer
import org.teamvoided.civilization.serializers.IdentifierSerializer
import org.teamvoided.civilization.serializers.UUIDSerializer
import org.teamvoided.civilization.util.Util
import java.util.*

@Serializable
data class Settlement(
    @Serializable(with = UUIDSerializer::class) val id: UUID,
    val name: String,
    val nameId: String,
    private var type: SettlementType,
    val joinPolicy: JoinPolicy,
    private val citizens: MutableMap<@Serializable(with = UUIDSerializer::class) UUID, String>, // UUID - Name
    private val chunks: MutableSet<@Serializable(with = ChunkPosSerializer::class) ChunkPos>,
    val hamlets: MutableSet<@Serializable(with = UUIDSerializer::class) UUID>?,
    val isHamlet: Boolean,
    @Serializable(with = UUIDSerializer::class) var nation: UUID?,
    var isCapital: Boolean,
    @Serializable(with = BlockPosSerializer::class) val center: BlockPos,
    @Serializable(with = UUIDSerializer::class) val leader: UUID,
    @Serializable(with = IdentifierSerializer::class) val dimension: Identifier
) {

    constructor(
        id: UUID,
        name: String,
        leader: ServerPlayerEntity,
        chunkPos: ChunkPos,
        centerPos: BlockPos,
        dimension: Identifier
    ) : this(
        id,
        name,
        Util.formatId(name),
        Settlement.SettlementType.BASE,
        JoinPolicy.INVITE,
        mutableMapOf(Pair(leader.uuid, leader.name.string)),
        mutableSetOf(chunkPos),
        null,
        false,
        null,
        false,
        centerPos,
        leader.uuid,
        dimension
    )

    fun getChunks(): List<ChunkPos> = chunks.toList()
    fun addChunk(pos: ChunkPos) = chunks.add(pos)
    fun removeChunk(pos: ChunkPos) = chunks.remove(pos)
    fun clearChunks() {
        chunks.clear()
        //make it read the center chunk
    }

    fun getCitizens(): Map<UUID, String> = citizens.toMap()
    fun addCitizen(cit: UUID, name: String) {
        citizens[cit] = name
        updateType()
    }

    fun removeCitizen(cit: UUID) {
        citizens.remove(cit)
        updateType()
    }

    fun clearCitizens() {
        val x = citizens[leader]!!
        citizens.clear()
        citizens[leader] = x
    }

    fun leaderName() = citizens[leader]!!

    fun getType(): SettlementType = type
    private fun updateType() {
        val newType = when (citizens.size) {
            in 0..2 -> SettlementType.BASE
            in 3..5 -> SettlementType.TOWN
            in 6..Int.MAX_VALUE -> SettlementType.CITY
            else -> null
        }
        if (newType != null) type = newType
    }

    fun formatId(): String = this.id.toString().lowercase()
    override fun toString(): String {
        return "id - ${this.id}\n" +
                "name - ${this.name}\n" +
                "nameId - ${this.nameId}\n" +
                "type - ${this.type}\n" +
                "citizens - ${this.citizens.map { it.toString() }}\n" +
                "chunks - ${this.chunks.map { it.toString() }}\n" +
                "hamlets - ${this.hamlets?.map { it.toString() }}\n" +
                "isHamlet - ${this.isHamlet}\n" +
                "nation - ${this.nation}\n" +
                "isCapital - ${this.isCapital}\n" +
                "center - ${this.center}\n" +
                "leader - ${this.leader}\n" +
                "dimension - ${this.dimension}"
    }

    @Serializable
    enum class SettlementType {
        BASE, TOWN, CITY;

        fun formatted() = this.toString().lowercase().replaceFirstChar { it.titlecase() }
    }

    @Serializable
    enum class JoinPolicy {
        INVITE, OPEN, CLOSED;

        fun formatted() = this.toString().lowercase().replaceFirstChar { it.titlecase() }
    }
}
