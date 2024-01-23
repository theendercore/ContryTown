package org.teamvoided.civilization.config

import kotlinx.serialization.Serializable
import net.minecraft.util.Identifier
import org.teamvoided.civilization.serializers.IdentifierSerializer

@Serializable
data class CivilizationConfigData(val banedDimensions: List<@Serializable(with = IdentifierSerializer::class) Identifier> = listOf())
