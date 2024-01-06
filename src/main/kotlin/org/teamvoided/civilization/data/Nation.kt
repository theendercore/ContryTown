package org.teamvoided.civilization.data

import kotlinx.serialization.Serializable
import org.teamvoided.civilization.serializers.UUIDSerializer
import org.teamvoided.civilization.util.Util
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
) {
    constructor(name: String, settlement: Settlement, leader: UUID, councilDelegate: UUID) :
            this(UUID.randomUUID(), name, Util.formatId(name), mutableSetOf(settlement.id), leader, councilDelegate, settlement.id)

    constructor( name: String, settlement: Settlement, leader: UUID) :
            this( name, settlement,leader, leader)

    override fun toString(): String {
        return "id - ${this.id}\n" +
                "name - ${this.name}\n" +
                "nameId - ${this.nameId}\n" +
                "settlements - ${this.settlements}\n" +
                "leader - ${this.leader}\n" +
                "councilDelegate - ${this.councilDelegate}\n" +
                "capital - ${this.capital}\n"
    }
}
