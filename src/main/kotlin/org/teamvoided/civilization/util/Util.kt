package org.teamvoided.civilization.util


@Suppress("MemberVisibilityCanBePrivate")
object Util {
    val idRegex = Regex("[^\\w/\\\\._-]")
    fun formatId(string: String): String =
        string.lowercase().replace(idRegex, "_").replace(Regex("_{2,}"), "_")
}