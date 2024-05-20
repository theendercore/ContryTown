package org.teamvoided.civilization.events

import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.EventFactory
import net.minecraft.entity.LivingEntity
import net.minecraft.util.math.BlockPos

object LivingEntityMoveEvent {
    fun interface LivingEntityMoveCallback {
        fun register(lastPos: BlockPos?, pos: BlockPos, entity: LivingEntity)
    }

    @JvmField
    val EVENT: Event<LivingEntityMoveCallback> =
        EventFactory.createArrayBacked(LivingEntityMoveCallback::class.java) { listeners ->
            LivingEntityMoveCallback { lastPos, pos, entity -> listeners.forEach { it.register(lastPos, pos, entity) } }
        }
}
