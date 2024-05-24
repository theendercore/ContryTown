package org.teamvoided.civilization.data

import net.minecraft.text.Text
import org.teamvoided.civilization.util.cmd


sealed interface CivilizationError


interface CommandError : CivilizationError, WitheKey


data class GenericCommandError(val key: String) : CommandError {
    override fun key() = key
}

class SenderIsNotPlayerError : CommandError {
    override fun key(): String = cmd("sender", "not", "player")
}



interface WitheKey {
    fun key(): String
}

interface WitheText {
    fun text(): Text
}
