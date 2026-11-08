package cn.erindax.brinswathe.mixin;

import cn.erindax.brinswathe.BrinDisarm;
import dev.doctor4t.wathe.util.KnifeStabPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KnifeStabPayload.Receiver.class)
public abstract class BrinDisarmKnifeStabMixin {
    @Inject(
        method = "receive(Ldev/doctor4t/wathe/util/KnifeStabPayload;Lnet/fabricmc/fabric/api/networking/v1/ServerPlayNetworking$Context;)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void brinRejectDisarmedStab(KnifeStabPayload payload, ServerPlayNetworking.Context context, CallbackInfo ci) {
        if (BrinDisarm.isDisarmed(context.player())) ci.cancel();
    }
}
