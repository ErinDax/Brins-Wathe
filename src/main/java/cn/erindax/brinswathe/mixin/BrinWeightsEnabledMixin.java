package cn.erindax.brinswathe.mixin;

import cn.erindax.brinswathe.BrinIcFlags;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "dev.doctor4t.wathe.cca.GameWorldComponent", remap = false)
public class BrinWeightsEnabledMixin {
    @Inject(method = "areWeightsEnabled", at = @At("HEAD"), cancellable = true)
    private void brinPureRandomUnlessEnabled(CallbackInfoReturnable<Boolean> cir) {
        if (!BrinIcFlags.roleWeights) {
            cir.setReturnValue(false);
        }
    }
}
