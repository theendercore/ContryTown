package org.teamvoided.civilization.data

import kotlinx.serialization.Serializable
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import org.teamvoided.civilization.serialization.serializers.UUIDSerializer
import org.teamvoided.civilization.util.Util
import org.teamvoided.civilization.util.iface.Textable
import java.util.*

@Serializable
data class Nation(
    @Serializable(with = UUIDSerializer::class) val id: UUID,
    val name: String,
    val nameId: String,
    val settlements: MutableSet<@Serializable(with = UUIDSerializer::class) UUID>,
    @Serializable(with = UUIDSerializer::class) val leader: UUID,
    @Serializable(with = UUIDSerializer::class) val councilDelegate: UUID,
    @Serializable(with = UUIDSerializer::class) val capital: UUID,
) : Textable {
    constructor(name: String, settlement: Settlement, leader: UUID, councilDelegate: UUID) :
            this(
                UUID.randomUUID(),
                name,
                Util.formatId(name),
                mutableSetOf(settlement.id),
                leader,
                councilDelegate,
                settlement.id
            )

    constructor(name: String, settlement: Settlement, leader: UUID) :
            this(name, settlement, leader, leader)


    override fun toString(): String {
        return "id - ${this.id}\n" +
                "name - ${this.name}\n" +
                "nameId - ${this.nameId}\n" +
                "settlements - ${this.settlements}\n" +
                "leader - ${this.leader}\n" +
                "councilDelegate - ${this.councilDelegate}\n" +
                "capital - ${this.capital}\n"
    }

    override fun toText(): MutableText {
        val bl = false
        val num = 1.0
        val str = "hello"
        val nil = null
        val obj = ResultType.SUCCESS
        val lst = listOf("1", "t")

        // bool, number, text, null, object, list
        return wrappedText {
            addBool(bl)
            addNumber(num)
            addString(str)
            addObject("result") {
                addString(obj.toString())
            }
            addList {
                lst.forEach { addString(it) }
            }
        }
    }

}


class WrappedText {
    val text = mutableListOf<MutableText>()

    fun text(): MutableText = text.reduce(MutableText::append)

    fun addBool(bl: Boolean) = text.add(text(bl, Formatting.AQUA))
    fun addNumber(num: Number) = text.add(text(num, Formatting.GOLD))
    fun addString(str: String) = text.add(text(str, Formatting.GREEN))
    fun addObject(name: String, init: WrappedText.() -> Unit) {
        text.add(text("$name: ", Formatting.WHITE).append(wrappedText(init)))
    }

    fun addList(init: WrappedText.() -> Unit) {
        text.add(text("[").append(wrappedText(init)).append(text("]")))
    }

}

fun wrappedText(init: WrappedText.() -> Unit): MutableText {
    val text = WrappedText()
    text.init()
    return text.text()
}

fun text(bl: Any, formatting: Formatting = Formatting.WHITE): MutableText =
    Text.literal(bl.toString()).formatted(formatting)
