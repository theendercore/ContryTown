package org.teamvoided.civilization.serialization.serializers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.minecraft.util.math.BlockPos

class BlockPosSerializer : KSerializer<BlockPos> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("block_pos", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: BlockPos) = encoder.encodeLong(value.asLong())
    override fun deserialize(decoder: Decoder): BlockPos = BlockPos.fromLong(decoder.decodeLong())
}
