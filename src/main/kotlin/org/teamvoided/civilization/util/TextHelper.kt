package org.teamvoided.civilization.util

import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.Formatting

@Suppress("Unused")
class TextBuilder(val connector: String = "") {
    val text = mutableListOf<MutableText>()

    fun text(): MutableText {
        val out = Text.empty()
        text.forEach {
            if (connector.isNotEmpty()) {
                out.append(it).append(text(connector))
            } else out.append(it)
        }
        return out
    }

    fun addBoolRaw(bl: Boolean?) = text.add(bl(bl))
    fun addNumberRaw(num: Number?) = text.add(num(num))
    fun addStringRaw(str: String?) = text.add(str(str))
    fun addObjectRaw(name: String, init: TextBuilder.() -> Unit) = text.add(pair(name, buildText(init = init)))
    fun addListRaw(init: TextBuilder.() -> Unit) =
        text.add(text("[ ").append(buildText(", ", init)).append(text(" ]")))

    fun addBool(name: String, bl: Boolean?) = text.add(pair(name, bl(bl)))
    fun addNumber(name: String, num: Number?) = text.add(pair(name, num(num)))
    fun addString(name: String, str: String?) = text.add(pair(name, str(str)))
    fun addObject(name: String, init: TextBuilder.() -> Unit) =
        text.add(pair(name, text("{ ").append(buildText(", ", init)).append(text(" }"))))

    fun addList(name: String, init: TextBuilder.() -> Unit) =
        text.add(pair(name, text("[ ").append(buildText(", ", init)).append(text(" ]"))))


    private fun pair(name: String, txt: MutableText): MutableText = text(name).append(text(": ")).append(txt)

    private fun bl(bl: Boolean?) = if (bl == null) nil() else text(bl, Formatting.AQUA)
    private fun num(num: Number?) = if (num == null) nil() else text(num, Formatting.GOLD)
    private fun str(str: String?) = if (str == null) nil() else text(str, Formatting.GREEN)
    private fun nil() = text("null", Formatting.GRAY)
}

fun buildText(connector: String = "", init: TextBuilder.() -> Unit): MutableText {
    val text = TextBuilder(connector)
    text.init()
    return text.text()
}

fun text(bl: Any, formatting: Formatting = Formatting.WHITE): MutableText =
    Text.literal(bl.toString()).formatted(formatting)
