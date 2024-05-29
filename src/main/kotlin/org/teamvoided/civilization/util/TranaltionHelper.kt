package org.teamvoided.civilization.util

import org.teamvoided.civilization.Civilization.MODID

fun cmd(vararg args: String): String = "command.$MODID.${args.joinToString(".")}"

fun generic(vararg args: String): String = "$MODID.${args.joinToString(".")}"
