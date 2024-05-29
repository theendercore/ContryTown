package org.teamvoided.civilization.data

import net.minecraft.text.Text
import org.teamvoided.civilization.commands.CivilizationCommand.civ
import org.teamvoided.civilization.util.cmd
import org.teamvoided.civilization.util.generic


sealed interface CivilizationError

data object ServerRefNotInitialized : CivilizationError, WitheKey {
    override fun key(): String = generic("server", "ref", "not", "initialized")
}


//              Command Errors

sealed interface CommandError : CivilizationError, WitheKey

// Generic Command Errors
data class GenericCommandError(val key: String) : CommandError {
    override fun key() = key
}

data class DebuggingError(val text: String) : CommandError, WitheKey {
    override fun key() = text
}

data object SenderIsNotPlayerError : CommandError {
    override fun key(): String = cmd("sender", "not", "player")
}


// Civilization Command Errors
interface CivCommandError : CommandError

data class FailedToLoad(val failedSection: String) : CivCommandError {
    override fun key(): String = civ("failed", "to", "load", failedSection)
}
data class FailedToSave(val failedSection: String) : CivCommandError {
    override fun key(): String = civ("failed", "to", "save", failedSection)
}


// Settlement  Errors
interface SettlementError : CivilizationError, WitheKey

data object SettlementNotFound : SettlementError {
    override fun key() = cmd("settlement", "not", "found")
}


// Error Helpers

interface WitheKey {
    fun key(): String
}

interface WitheText {
    fun text(): Text
}
