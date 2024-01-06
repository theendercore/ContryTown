package org.teamvoided.civilization.util

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import net.minecraft.server.MinecraftServer
import net.minecraft.util.WorldSavePath
import net.minecraft.world.World
import net.minecraft.world.dimension.DimensionType
import org.teamvoided.civilization.Civilization
import java.nio.file.Path


@Suppress("MemberVisibilityCanBePrivate")
object Util {
    val idRegex = Regex("[^\\w/\\\\._-]")

    @OptIn(ExperimentalSerializationApi::class)
    val json = Json { prettyPrint = true; prettyPrintIndent = "  " }
    fun formatId(string: String): String =
        string.lowercase().replace(idRegex, "_").replace(Regex("_{2,}"), "_")

    fun getModSavePath(server: MinecraftServer, world: World): Path {
        return DimensionType.getSaveDirectory(world.registryKey, server.getSavePath(WorldSavePath.ROOT)).parent
            .resolve("data")
            .resolve(Civilization.MODID)
    }
}