package cn.erindax.brinswathe.client.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(targets = "dev.doctor4t.ratatouille.client.util.OptionLocker", remap = false)
public abstract class BrinRecordVolumeUnlockMixin {
    @Inject(method = "overrideSoundCategoryVolume(Ljava/lang/String;D)V", at = @At("HEAD"), cancellable = true)
    private static void brinKeepRecordVolumeAdjustable(String option, double value, CallbackInfo ci) {
        if ("record".equals(option)) ci.cancel();
    }
}
