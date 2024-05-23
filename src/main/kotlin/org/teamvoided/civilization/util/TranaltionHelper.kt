package org.teamvoided.civilization.util

import org.teamvoided.civilization.Civilization.MODID

fun cmd(vararg args: String): String = (arrayOf("command", MODID) + args).joinToString(".")