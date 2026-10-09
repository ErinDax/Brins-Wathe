package cn.erindax.brinswathe.mixin;

import cn.erindax.brinswathe.BrinPunishment;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class BrinSwapperTrackMixin {
    @Inject(method = "moveTo(DDD)V", at = @At("HEAD"))
    private void brinTrackSwap(double x, double y, double z, CallbackInfo ci) {
        BrinPunishment.onMoved((ServerPlayer) (Object) this);
    }
}
