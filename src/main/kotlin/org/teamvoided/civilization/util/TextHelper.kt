package org.teamvoided.civilization.util

import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.Formatting


fun text(bl: Any, formatting: Formatting = Formatting.WHITE): MutableText =
    Text.literal(bl.toString()).formatted(formatting)

fun tTxt(text: String, vararg args: Any): MutableText = Text.translatable(text, *args)
fun lTxt(text: String): MutableText = Text.literal(text)
