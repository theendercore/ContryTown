package org.teamvoided.template.util

object Util {
    fun formatId(string: String): String = string.lowercase().replace(" ", "_")
}