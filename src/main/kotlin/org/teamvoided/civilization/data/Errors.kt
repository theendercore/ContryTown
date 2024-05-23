package org.teamvoided.civilization.data

import org.teamvoided.civilization.util.cmd


sealed interface CivilizationError


interface CommandError : CivilizationError {
    fun key(): String
}


data class GenericCommandError(val key: String) : CommandError {
    override fun key() = key
}

class SenderIsNotPlayerError : CommandError {
    override fun key(): String = cmd("sender", "not", "player")
}

/*

@Suppress("SpreadOperator")
data class ErrorData(val key: String, val args: List<Any> = listOf()) {
    fun toTTxt(): MutableText = tTxt(key, *args.toTypedArray())
}*/
