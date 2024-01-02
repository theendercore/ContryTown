package org.teamvoided.civilization.data

import kotlinx.serialization.Serializable
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.ChunkPos
import org.teamvoided.civilization.serializers.BlockPosSerializer
import org.teamvoided.civilization.serializers.ChunkPosSerializer
import org.teamvoided.civilization.serializers.IdentifierSerializer
import org.teamvoided.civilization.serializers.UUIDSerializer
import java.util.UUID

@Serializable
data class Settlement(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID,
    val name: String,
    val nameId: String,
    val type: SettlementType,
    val citizens: MutableSet<@Serializable(with = UUIDSerializer::class) UUID>,
    val chunks: MutableSet<@Serializable(with = ChunkPosSerializer::class) ChunkPos>,
    @Serializable(with = BlockPosSerializer::class)
    val capital: BlockPos,
    @Serializable(with = UUIDSerializer::class)
    val leader: UUID,
    @Serializable(with = UUIDSerializer::class)
    val councilDelegate: UUID?,
    @Serializable(with = IdentifierSerializer::class)
    val dimension: Identifier
) {
    fun formatId(): String = this.id.toString().lowercase()

    @Serializable
    enum class SettlementType { BASE, TOWN, CITY, NATION }
}
