package org.teamvoided.civilization.init

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import org.teamvoided.civilization.Civilization
import org.teamvoided.civilization.commands.CivilizationCommand
import org.teamvoided.civilization.commands.SettlementCommand
import org.teamvoided.civilization.commands.TestCommand

object CivCommands {
    var DEBUG_MODE = false

    fun init(){
        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
            CivilizationCommand.init(dispatcher)
            SettlementCommand.init(dispatcher)
//            NationCommand.init(dispatcher)
            if (Civilization.DEV_ENV) TestCommand.init(dispatcher)
        }
    }
}
