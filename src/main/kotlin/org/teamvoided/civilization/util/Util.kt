package org.teamvoided.civilization.util

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.server.MinecraftServer
import net.minecraft.text.MutableText
import net.minecraft.util.WorldSavePath
import net.minecraft.world.World
import net.minecraft.world.dimension.DimensionType
import org.teamvoided.civilization.Civilization
import org.teamvoided.civilization.data.ResultType
import java.nio.file.Path


@Suppress("MemberVisibilityCanBePrivate")
object Util {
    val idRegex = Regex("[^\\w/\\\\._-]")

    @OptIn(ExperimentalSerializationApi::class)
    val json = Json { prettyPrint = true; prettyPrintIndent = "  " }
    fun formatId(string: String): String =
        string.lowercase().replace(idRegex, "_").replace(Regex("_{2,}"), "_")

    fun String.toWord(): String = this.lowercase().replaceFirstChar { it.titlecase() }

    fun getWorldPath(server: MinecraftServer, world: World): Path =
        DimensionType.getSaveDirectory(world.registryKey, server.getSavePath(WorldSavePath.ROOT)).parent
            .resolve("data")
            .resolve(Civilization.MODID)

    fun getGlobalPath(): Path =
        FabricLoader.getInstance().gameDir.resolve("data").resolve(Civilization.MODID)

    fun emptyResult(): Pair<ResultType, MutableText> = Pair(ResultType.SUCCESS, tTxt("Result!"))
}
