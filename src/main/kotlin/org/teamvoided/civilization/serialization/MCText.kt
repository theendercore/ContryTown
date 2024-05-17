package org.teamvoided.civilization.serialization

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.CompositeEncoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.serializer
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.Formatting


// AS of the current moment this is abandoned for the sake of the project being done faster
@Suppress("TooManyFunctions")
@OptIn(ExperimentalSerializationApi::class)
class MCText(val text: MutableText = Text.empty()) : Encoder, CompositeEncoder {

    private var elementIndex = 0
    override val serializersModule: SerializersModule = EmptySerializersModule()
    override fun beginStructure(descriptor: SerialDescriptor): CompositeEncoder = MCText(text)
    override fun endStructure(descriptor: SerialDescriptor) {}
    fun encodeElement(descriptor: SerialDescriptor, index: Int): Boolean = true


    private fun addText(value: Any, formatting: Formatting) {
        text.append(Text.literal(value.toString()).formatted(formatting))
    }

    private fun addText(value: Text, formatting: Formatting) {
        text.append(value.copy().formatted(formatting))
    }

    private fun addText(value: Text) {
        text.append(text)
    }


    override fun encodeInline(descriptor: SerialDescriptor): Encoder = this
    override fun encodeInlineElement(descriptor: SerialDescriptor, index: Int): Encoder =
        encodeInline(descriptor.getElementDescriptor(index))


    override fun encodeBoolean(value: Boolean): Unit = addText(value, Formatting.AQUA)

    override fun encodeByte(value: Byte): Unit = addText(value, Formatting.GOLD)
    override fun encodeShort(value: Short): Unit = addText(value, Formatting.GOLD)
    override fun encodeInt(value: Int): Unit = addText(value, Formatting.GOLD)
    override fun encodeLong(value: Long): Unit = addText(value, Formatting.GOLD)
    override fun encodeFloat(value: Float): Unit = addText(value, Formatting.GOLD)
    override fun encodeDouble(value: Double): Unit = addText(value, Formatting.GOLD)

    override fun encodeChar(value: Char): Unit = addText(value, Formatting.GREEN)
    override fun encodeString(value: String): Unit = addText(value, Formatting.GREEN)

    override fun encodeNull() = addText("null", Formatting.DARK_GRAY)


    override fun encodeBooleanElement(descriptor: SerialDescriptor, index: Int, value: Boolean) {
        if (encodeElement(descriptor, index)) {
            addText(
                base(descriptor.getElementName(index))
                    .boolean(value)
            )
        }
    }

    override fun encodeByteElement(descriptor: SerialDescriptor, index: Int, value: Byte) {
        if (encodeElement(descriptor, index)) {
            addText(
                base(descriptor.getElementName(index))
                    .number(value)
            )
        }
    }

    override fun encodeShortElement(descriptor: SerialDescriptor, index: Int, value: Short) {
        if (encodeElement(descriptor, index)) {
            addText(
                base(descriptor.getElementName(index))
                    .number(value)
            )
        }
    }

    override fun encodeIntElement(descriptor: SerialDescriptor, index: Int, value: Int) {
        if (encodeElement(descriptor, index)) {
            addText(
                base(descriptor.getElementName(index))
                    .number(value)
            )
        }
    }

    override fun encodeLongElement(descriptor: SerialDescriptor, index: Int, value: Long) {
        if (encodeElement(descriptor, index)) {
            addText(
                base(descriptor.getElementName(index))
                    .number(value)
            )
        }
    }


    override fun encodeFloatElement(descriptor: SerialDescriptor, index: Int, value: Float) {
        if (encodeElement(descriptor, index)) {
            addText(
                base(descriptor.getElementName(index))
                    .number(value)
            )

        }
    }


    override fun encodeDoubleElement(descriptor: SerialDescriptor, index: Int, value: Double) {
        if (encodeElement(descriptor, index)) {
            addText(
                base(descriptor.getElementName(index))
                    .number(value)
            )
        }
    }

    override fun encodeCharElement(descriptor: SerialDescriptor, index: Int, value: Char) {
        if (encodeElement(descriptor, index)) {
            addText(
                base(descriptor.getElementName(index))
                    .string(value.toString())
            )
        }
    }

    override fun encodeStringElement(descriptor: SerialDescriptor, index: Int, value: String) {
        if (encodeElement(descriptor, index)) {
            addText(
                base(descriptor.getElementName(index))
                    .string(value)
            )
        }
    }


    override fun encodeEnum(enumDescriptor: SerialDescriptor, index: Int): Unit =
        addText(enumDescriptor.getElementName(index), Formatting.YELLOW)


    @ExperimentalSerializationApi
    override fun <T : Any> encodeNullableSerializableElement(
        descriptor: SerialDescriptor, index: Int, serializer: SerializationStrategy<T>, value: T?
    ) {
        if (encodeElement(descriptor, index)) encodeNullableSerializableValue(serializer, value)
    }

    override fun <T> encodeSerializableElement(
        descriptor: SerialDescriptor, index: Int, serializer: SerializationStrategy<T>, value: T
    ) {
        if (encodeElement(descriptor, index)) encodeSerializableValue(serializer, value)
    }

    private fun base(value: String): MutableText = Text.literal("$value :=").formatted(Formatting.WHITE)
    private fun MutableText.boolean(value: Boolean): MutableText =
        this.append(Text.literal(value.toString()).formatted(Formatting.AQUA))

    private fun MutableText.number(value: Number): MutableText =
        this.append(Text.literal(value.toString()).formatted(Formatting.GOLD))

    private fun MutableText.string(value: String): MutableText =
        this.append(Text.literal(value).formatted(Formatting.GREEN))
}


fun <T> encodeToText(serializer: SerializationStrategy<T>, value: T): Text {
    val encoder = MCText()
    encoder.encodeSerializableValue(serializer, value)
    return encoder.text
}

inline fun <reified T> encodeToText(value: T) = encodeToText(serializer(), value)
