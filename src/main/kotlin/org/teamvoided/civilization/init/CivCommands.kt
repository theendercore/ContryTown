package org.teamvoided.civilization.init

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import org.teamvoided.civilization.Civilization
import org.teamvoided.civilization.commands.CivilizationCommand
import org.teamvoided.civilization.commands.NationCommand
import org.teamvoided.civilization.commands.TestCommand
import org.teamvoided.civilization.commands.argument.NationArgumentType
import org.teamvoided.civilization.commands.argument.SettlementArgumentType

object CivCommands {
    var DEBUG_MODE = false

    fun init(){
        SettlementArgumentType.init()
        NationArgumentType.init()
        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
            CivilizationCommand.init(dispatcher)
            NationCommand.init(dispatcher)
            if (Civilization.DEV_ENV) TestCommand.init(dispatcher)
        }
    }
}