package org.teamvoided.civilization


import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.server.MinecraftServer
import net.minecraft.util.Identifier
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.teamvoided.civilization.compat.SquaremapIntegrations
import org.teamvoided.civilization.managers.NationManager
import org.teamvoided.civilization.managers.PlayerDataManager
import org.teamvoided.civilization.managers.SettlementManager
import org.teamvoided.civilization.init.CivCommands
import org.teamvoided.civilization.util.Util


@Suppress("unused", "MemberVisibilityCanBePrivate")
object Civilization {
    const val MODID = "civilization"
    val DEV_ENV = FabricLoader.getInstance().isDevelopmentEnvironment

    @JvmField
    val log: Logger = LoggerFactory.getLogger(Civilization::class.simpleName)

    fun commonInit() {
        log.info("Hello from Common")
        Util.getGlobalPath().toFile().mkdirs()
        CivCommands.init()
        PlayerDataManager.init()
        ServerLifecycleEvents.SERVER_STARTED.register(::afterServerLoads)
    }

    fun id(path: String) = Identifier(MODID, path)

    private fun afterServerLoads(server: MinecraftServer) {
        for (world in server.worlds) Util.getWorldPath(server, world).toFile().mkdirs()

        SettlementManager.postServerInit(server)
        NationManager.postServerInit(server)
        if (FabricLoader.getInstance().isModLoaded("squaremap"))
            SquaremapIntegrations.reg()
    }
}
