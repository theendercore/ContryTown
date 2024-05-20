package org.teamvoided.civilization.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.teamvoided.civilization.events.LivingEntityMoveEvent;


@Mixin(LivingEntity.class)
public class LivingEntityMixin {


    @Shadow
    private BlockPos lastBlockPos;

    @SuppressWarnings("UnreachableCode")
    @Inject(method = "baseTick", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/LivingEntity;lastBlockPos:Lnet/minecraft/util/math/BlockPos;", opcode = Opcodes.PUTFIELD))
    private void onBlockPosChange(CallbackInfo ci, @Local BlockPos pos) {
        LivingEntity entity = (LivingEntity)(Object)this;
        LivingEntityMoveEvent.EVENT.invoker().register(this.lastBlockPos, pos, entity);
    }
}
