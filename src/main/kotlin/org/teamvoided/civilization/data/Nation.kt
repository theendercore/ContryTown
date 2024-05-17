@file:UseSerializers(UUIDSerializer::class)

package org.teamvoided.civilization.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import net.minecraft.text.MutableText
import org.teamvoided.civilization.serialization.serializers.UUIDSerializer
import org.teamvoided.civilization.util.Util
import org.teamvoided.civilization.util.iface.Textable
import org.teamvoided.civilization.util.buildText
import java.util.*

@Serializable
data class Nation(
    val id: UUID,
    val name: String,
    val nameId: String,
    val settlements: MutableSet<UUID>,
    val leader: UUID,
    val councilDelegate: UUID,
    val capital: UUID,
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

    override fun toText(): MutableText = buildText {
        addString("id", id.toString())
        addString("name", name)
        addString("formatted_name", nameId)
        addList("settlements") { settlements.forEach { addString("settlement", it.toString()) } }
        addString("leader", leader.toString())
        addString("council_delegate", councilDelegate.toString())
        addString("capital", capital.toString())
    }
}
