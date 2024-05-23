package org.teamvoided.civilization.util

import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.Formatting


fun text(bl: Any, formatting: Formatting = Formatting.WHITE): MutableText =
    Text.literal(bl.toString()).formatted(formatting)

fun tText(text: String, vararg args: Any): MutableText = Text.translatable(text, *args)
fun String.toTTxt(): Text = tText(this)

fun lText(text: String): MutableText = Text.literal(text)
fun String.toLTxt(): Text = lText(this)

