package org.teamvoided.civilization


import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.server.MinecraftServer
import net.minecraft.util.Identifier
import org.slf4j.LoggerFactory
import org.teamvoided.civilization.commands.TestCommand
import org.teamvoided.civilization.commands.argument.SettlementArgumentType
import org.teamvoided.civilization.compat.SquaremapIntegrations
import org.teamvoided.civilization.data.SettlementManager


@Suppress("unused")
object Civilization {
    const val MODID = "civilization"
    val DEV_ENV = FabricLoader.getInstance().isDevelopmentEnvironment

    @JvmField
    val LOGGER = LoggerFactory.getLogger(Civilization::class.simpleName)

    fun commonInit() {
        LOGGER.info("Hello from Common")
        SettlementManager.init()
        SettlementArgumentType.init()
        if (DEV_ENV) TestCommand.init()
        ServerLifecycleEvents.SERVER_STARTED.register(::afterServerLoads)
    }

    fun id(path: String) = Identifier(MODID, path)

    private fun afterServerLoads(server: MinecraftServer) {
        SettlementManager.postServerInit(server)
        if (FabricLoader.getInstance().isModLoaded("squaremap"))
            SquaremapIntegrations.reg()
    }
}
