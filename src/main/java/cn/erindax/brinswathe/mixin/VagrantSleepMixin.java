package cn.erindax.brinswathe.mixin;

import cn.erindax.brinswathe.Vagrant;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class VagrantSleepMixin {
    @Inject(method = "checkBedExists", at = @At("HEAD"), cancellable = true)
    private void brinKeepGroundSleep(CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof Player player && Vagrant.keepsGroundSleep(player)) cir.setReturnValue(true);
    }

    @Inject(method = "setPosToBed", at = @At("HEAD"), cancellable = true)
    private void brinStayOnGround(BlockPos pos, CallbackInfo ci) {
        if ((Object) this instanceof Player player && Vagrant.isGroundSleep(player, pos)) ci.cancel();
    }

    @Inject(method = "getBedOrientation", at = @At("HEAD"), cancellable = true)
    private void brinLieTowardsFacing(CallbackInfoReturnable<Direction> cir) {
        if ((Object) this instanceof Player player && Vagrant.keepsGroundSleep(player)) {
            cir.setReturnValue(player.getDirection().getOpposite());
        }
    }
}
