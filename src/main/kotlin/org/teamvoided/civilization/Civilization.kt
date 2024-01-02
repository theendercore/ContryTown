package org.teamvoided.civilization


import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.server.MinecraftServer
import net.minecraft.util.Identifier
import org.slf4j.LoggerFactory
import org.teamvoided.civilization.commands.TestCommand
import org.teamvoided.civilization.compat.SquaremapIntegrations
import org.teamvoided.civilization.data.SettlementsManager


@Suppress("unused")
object Civilization {
    const val MODID = "civilization"

    @JvmField
    val LOGGER = LoggerFactory.getLogger(Civilization::class.simpleName)

    fun commonInit() {
        LOGGER.info("Hello from Common")
        SettlementsManager.init()
        TestCommand.init()
        ServerLifecycleEvents.SERVER_STARTED.register(::afterServerLoads)
    }

    fun id(path: String) = Identifier(MODID, path)

    private fun afterServerLoads(server: MinecraftServer) {
        SettlementsManager.postServerInit(server)
        if (FabricLoader.getInstance().isModLoaded("squaremap"))
            SquaremapIntegrations.reg()
    }
}
