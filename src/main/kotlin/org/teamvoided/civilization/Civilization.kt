package org.teamvoided.civilization


import arrow.core.Either
import arrow.core.raise.either
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.MinecraftServer
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.ChunkPos
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.teamvoided.civilization.compat.SquaremapIntegrations
import org.teamvoided.civilization.data.ServerRefNotInitialized
import org.teamvoided.civilization.events.LivingEntityMoveEvent
import org.teamvoided.civilization.init.CivCommands
import org.teamvoided.civilization.managers.PlayerDataManager
import org.teamvoided.civilization.managers.SettlementManager
import org.teamvoided.civilization.util.Util
import org.teamvoided.civilization.util.tText


@Suppress("unused", "MemberVisibilityCanBePrivate")
object Civilization {
    const val MODID = "civilization"
    val DEV_ENV = FabricLoader.getInstance().isDevelopmentEnvironment

    var SERVER_REF: MinecraftServer? = null
        private set

    @JvmField
    val log: Logger = LoggerFactory.getLogger(Civilization::class.simpleName)

    fun commonInit() {
        log.info("Hello from Common")
        Util.getGlobalPath().toFile().mkdirs()
        CivCommands.init()
        PlayerDataManager.init()
        ServerLifecycleEvents.SERVER_STARTING.register(::beforeServerLoads)
        ServerLifecycleEvents.SERVER_STARTED.register(::afterServerLoads)
        LivingEntityMoveEvent.EVENT.register(::playerChangeChunk)

    }

    private fun playerChangeChunk(last: BlockPos?, pos: BlockPos, entity: LivingEntity) {
        if (entity is PlayerEntity) {
            val currentChunk = ChunkPos(pos)
            if (last?.let { ChunkPos(it) != currentChunk } == true) {
                SettlementManager.getByChunkPos(currentChunk)?.let {
                    entity.sendMessage(tText("You have entered ${it.name} Settlement!"), true)
                }
            }
        }
    }

    fun id(path: String) = Identifier(MODID, path)
    private fun beforeServerLoads(server: MinecraftServer) {
        SERVER_REF = server
    }

    private fun afterServerLoads(server: MinecraftServer) {
        for (world in server.worlds) Util.getWorldPath(server, world).toFile().mkdirs()

        SettlementManager.postServerInit(server)
//        NationManager.postServerInit(server)
        if (FabricLoader.getInstance().isModLoaded("squaremap"))
            SquaremapIntegrations.reg()

        SERVER_REF = server
    }
}
