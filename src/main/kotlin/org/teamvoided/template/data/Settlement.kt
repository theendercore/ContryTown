package org.teamvoided.template.data

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.*
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.ChunkPos
import java.util.UUID

@Serializable(with = Settlement.SettlementSerializer::class)
data class Settlement(
    val id: String,
    val name: String,
    val type: SettlementType,
    val citizens: MutableSet<UUID>,
    val chunks: MutableSet<ChunkPos>,
    val capital: BlockPos,
    val leader: UUID,
    val councilDelegate: UUID?,
    val dimension: Identifier
) {
    fun formatId(): String = this.id.lowercase()

    @Serializable
    enum class SettlementType { BASE, TOWN, CITY, NATION }
    object SettlementSerializer : KSerializer<Settlement> {
        override val descriptor: SerialDescriptor = buildClassSerialDescriptor("settlement") {
            element<String>("id")
            element<String>("name")
            element<String>("type")
            element<List<String>>("citizens")
            element<List<List<Int>>>("chunks")
            element<Long>("capital")
            element<String>("leader")
            element<String>("council_delegate")
            element<String>("dimension")
        }

        override fun serialize(encoder: Encoder, value: Settlement) {
            encoder.encodeStructure(descriptor) {

                encodeStringElement(descriptor, 0, value.id)
                encodeStringElement(descriptor, 1, value.name)
                encodeSerializableElement(descriptor, 2, SettlementType.serializer(), value.type)
                encodeSerializableElement(
                    descriptor,
                    3,
                    ListSerializer(String.serializer()),
                    value.citizens.map { it.toString() })
                encodeSerializableElement(descriptor,
                    4,
                    ListSerializer(ListSerializer(Int.serializer())),
                    value.chunks.map { listOf(it.x, it.z) })
                encodeLongElement(descriptor, 5, value.capital.asLong())
                encodeStringElement(descriptor, 6, value.leader.toString())
                encodeStringElement(descriptor, 7, value.councilDelegate.toString())
                encodeStringElement(descriptor, 8, value.dimension.toString())
            }
        }

        override fun deserialize(decoder: Decoder): Settlement = decoder.decodeStructure(descriptor) {
            var id: String? = null
            var name: String? = null
            var type: SettlementType? = null
            var citizens: MutableSet<UUID>? = null
            var claims: MutableSet<ChunkPos>? = null
            var capital: BlockPos? = null
            var leader: UUID? = null
            var councilDelegate: UUID? = null
            var dimension: Identifier? = null
            while (true) {
                when (val index = decodeElementIndex(descriptor)) {
                    0 -> id = decodeStringElement(descriptor, 0)
                    1 -> name = decodeStringElement(descriptor, 1)
                    2 -> type = decodeSerializableElement(descriptor, 2, SettlementType.serializer())
                    3 -> citizens = decodeSerializableElement(
                        descriptor, 3, ListSerializer(String.serializer())
                    ).map { UUID.fromString(it) }.toMutableSet()

                    4 -> claims = decodeSerializableElement(
                        descriptor, 4, ListSerializer(ListSerializer(Int.serializer()))
                    ).map { ChunkPos(it[0], it[1]) }.toMutableSet()

                    5 -> capital = BlockPos.fromLong(decodeLongElement(descriptor, 5))
                    6 -> leader = UUID.fromString(decodeStringElement(descriptor, 6))
                    7 -> {
                        val temp = decodeStringElement(descriptor, 7)
                        println(temp)
                        if (temp != "null") councilDelegate = UUID.fromString(temp)
                    }

                    8 -> dimension = Identifier(decodeStringElement(descriptor, 8))
                    CompositeDecoder.DECODE_DONE -> break
                    else -> error("Unexpected index: $index")
                }
            }
            Settlement(id!!, name!!, type!!, citizens!!, claims!!, capital!!, leader!!, councilDelegate, dimension!!)
        }

    }

}
