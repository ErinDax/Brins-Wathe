package cn.erindax.brinswathe.mixin;

import cn.erindax.brinswathe.BrinDisarm;
import dev.doctor4t.wathe.util.GunShootPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GunShootPayload.Receiver.class)
public abstract class BrinDisarmGunShootMixin {
    @Inject(
        method = "receive(Ldev/doctor4t/wathe/util/GunShootPayload;Lnet/fabricmc/fabric/api/networking/v1/ServerPlayNetworking$Context;)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void brinRejectDisarmedShot(GunShootPayload payload, ServerPlayNetworking.Context context, CallbackInfo ci) {
        if (BrinDisarm.isDisarmed(context.player())) ci.cancel();
    }
}
