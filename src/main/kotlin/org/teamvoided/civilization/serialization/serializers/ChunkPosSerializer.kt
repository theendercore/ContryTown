package org.teamvoided.civilization.serialization.serializers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.minecraft.util.math.ChunkPos

class ChunkPosSerializer : KSerializer<ChunkPos> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("chunk_pos", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: ChunkPos) = encoder.encodeLong(value.toLong())
    override fun deserialize(decoder: Decoder): ChunkPos = ChunkPos(decoder.decodeLong())
}