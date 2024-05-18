package org.teamvoided.civilization.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public interface LivingEntityMoveCallback {
    Event<LivingEntityMoveCallback> EVENT = EventFactory.createArrayBacked(LivingEntityMoveCallback.class, (callbacks) -> (lastPos, pos, entity) -> {
        for (LivingEntityMoveCallback callback : callbacks) {
            callback.register(lastPos, pos, entity);
        }
    });

    void register(@Nullable BlockPos lastPos, BlockPos pos, LivingEntity entity);
}
