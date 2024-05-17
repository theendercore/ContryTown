package org.teamvoided.civilization.util

import net.minecraft.server.MinecraftServer
import org.teamvoided.civilization.Civilization.log
import org.teamvoided.civilization.managers.NationManager
import org.teamvoided.civilization.managers.SettlementManager

object CivilizationSaver {
    fun save(server: MinecraftServer){
        log.info("Civilization started saving...")
        Thread{
            SettlementManager.saveAll(server)
            NationManager.save()
        }.start()
    }
}