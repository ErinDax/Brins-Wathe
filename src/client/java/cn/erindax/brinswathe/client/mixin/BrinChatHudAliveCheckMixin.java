package cn.erindax.brinswathe.client.mixin;

import cn.erindax.brinswathe.client.BrinChatHudGate;
import dev.doctor4t.wathe.client.WatheClient;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(value = WatheClient.class, priority = 2000)
public abstract class BrinChatHudAliveCheckMixin {
    @Inject(method = "isPlayerAliveAndInSurvival", at = @At("HEAD"), cancellable = true, remap = false)
    private static void brinShowChatWhileRendering(CallbackInfoReturnable<Boolean> cir) {
        if (BrinChatHudGate.isRendering()) cir.setReturnValue(false);
    }
}
