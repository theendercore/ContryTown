package org.teamvoided.civilization.data

import kotlinx.serialization.Serializable
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
    private val citizens: MutableSet<@Serializable(with = UUIDSerializer::class) UUID>,
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
        leader: UUID,
        chunkPos: ChunkPos,
        centerPos: BlockPos,
        dimension: Identifier
    ) : this(
        id,
        name,
        Util.formatId(name),
        Settlement.SettlementType.BASE,
        mutableSetOf(leader),
        mutableSetOf(chunkPos),
        null,
        false,
        null,
        false,
        centerPos,
        leader,
        dimension
    )

    fun getChunks(): List<ChunkPos> = chunks.toList()
    fun addChunk(pos: ChunkPos) = chunks.add(pos)
    fun removeChunk(pos: ChunkPos) = chunks.remove(pos)
    fun clearChunks() {
        chunks.clear()
        //make it read the center chunk
    }

    fun getCitizens(): List<UUID> = citizens.toList()
    fun addCitizen(pos: UUID) {
        citizens.add(pos)
        updateType()
    }

    fun removeCitizen(pos: UUID) {
        citizens.remove(pos)
        updateType()
    }

    fun clearCitizens() {
        citizens.clear()
        citizens.add(leader)
    }

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
                "capital - ${this.center}\n" +
                "leader - ${this.leader}\n" +
                "dimension - ${this.dimension}"
    }

    @Serializable
    enum class SettlementType { BASE, TOWN, CITY }
}
