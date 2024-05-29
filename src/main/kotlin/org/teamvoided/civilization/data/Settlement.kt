package org.teamvoided.civilization.data

import arrow.optics.OpticsCopyMarker
import kotlinx.serialization.Serializable
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.MutableText
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.ChunkPos
import org.teamvoided.civilization.serialization.serializers.BlockPosSerializer
import org.teamvoided.civilization.serialization.serializers.ChunkPosSerializer
import org.teamvoided.civilization.serialization.serializers.IdentifierSerializer
import org.teamvoided.civilization.serialization.serializers.UUIDSerializer
import org.teamvoided.civilization.util.Util
import org.teamvoided.civilization.util.Util.toWord
import org.teamvoided.civilization.util.buildText
import org.teamvoided.civilization.util.iface.Textable
import java.util.*

@OpticsCopyMarker
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
) : Textable {

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

    fun setLeader(newLeader: ServerPlayerEntity): Settlement {
        return this.copy(
            leader = newLeader.uuid,
            citizens = (citizens + mutableMapOf(newLeader.uuid to newLeader.name.string)).toMutableMap()
        )
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

    override fun toText(): MutableText = buildText {
        add("id", formatId())
        add("name", name)
        add("formatted_name", nameId)
        add("type", type.toString())
        addList("citizens") {
            citizens.forEach { add(it.value) }
        }
        addList("chunks") {
            chunks.forEach {
                addListRaw {
                    add(it.x)
                    add(it.z)
                }
            }
        }
        addList("hamlets") {
            hamlets?.forEach { add("hamlet", it.toString()) }
        }
        add("isHamlet", isHamlet)
        add("nation", nation?.toString())
        add("isCapital", isCapital)
        addObject("center") {
            add("x", center.x)
            add("y", center.y)
            add("z", center.z)
        }
        add("leader", leader.toString())
        add("dimension", dimension.toString())
    }

    @Serializable
    enum class SettlementType {
        BASE, TOWN, CITY;

        fun formatted() = this.toString().toWord()
    }

    @Serializable
    enum class JoinPolicy {
        INVITE, OPEN, CLOSED;

        fun formatted() = this.toString().toWord()
    }
}
