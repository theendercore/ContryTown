package org.teamvoided.civilization


import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.server.MinecraftServer
import net.minecraft.util.Identifier
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.teamvoided.civilization.compat.SquaremapIntegrations
import org.teamvoided.civilization.data.NationManager
import org.teamvoided.civilization.data.SettlementManager
import org.teamvoided.civilization.init.CivilizationCommands
import org.teamvoided.civilization.util.Util


@Suppress("unused", "MemberVisibilityCanBePrivate")
object Civilization {
    const val MODID = "civilization"
    val DEV_ENV = FabricLoader.getInstance().isDevelopmentEnvironment

    @JvmField
    val LOGGER: Logger = LoggerFactory.getLogger(Civilization::class.simpleName)

    fun commonInit() {
        LOGGER.info("Hello from Common")
        CivilizationCommands.init()
        SettlementManager.init()
        ServerLifecycleEvents.SERVER_STARTED.register(::afterServerLoads)
    }

    fun id(path: String) = Identifier(MODID, path)

    private fun afterServerLoads(server: MinecraftServer) {
        for (world in server.worlds) {
            Util.getModSavePath(server, world).toFile().mkdirs()
        }

        SettlementManager.postServerInit(server)
        NationManager.postServerInit(server)
        if (FabricLoader.getInstance().isModLoaded("squaremap"))
            SquaremapIntegrations.reg()
    }
}
