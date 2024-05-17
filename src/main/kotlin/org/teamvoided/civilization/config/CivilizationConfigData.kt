package org.teamvoided.civilization.config

import kotlinx.serialization.Required
import kotlinx.serialization.Serializable
import net.minecraft.util.Identifier
import org.teamvoided.civilization.serialization.serializers.IdentifierSerializer

@Serializable

data class CivilizationConfigData(
    @Required val banedDimensions: List<@Serializable(with = IdentifierSerializer::class) Identifier> = listOf(),
    @Required val haveSetlAlias: Boolean = true,
    @Required val haveNatAlias: Boolean = true,
)
