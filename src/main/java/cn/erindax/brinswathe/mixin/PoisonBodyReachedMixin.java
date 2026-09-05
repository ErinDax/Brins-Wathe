package cn.erindax.brinswathe.mixin;

import cn.erindax.brinswathe.PoisonBridge;
import dev.doctor4t.wathe.cca.PlayerPoisonComponent;
import java.util.UUID;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = PlayerPoisonComponent.class, priority = 1)
public abstract class PoisonBodyReachedMixin {
    @Inject(method = "setPoisonTicks", at = @At("HEAD"))
    private void brinMarkSetPoisonReached(int ticks, UUID poisoner, CallbackInfo ci) {
        PoisonBridge.markSetPoisonBodyReached();
    }

    @Inject(method = "reset", at = @At("HEAD"))
    private void brinMarkResetReached(CallbackInfo ci) {
        PoisonBridge.markResetBodyReached();
    }
}
