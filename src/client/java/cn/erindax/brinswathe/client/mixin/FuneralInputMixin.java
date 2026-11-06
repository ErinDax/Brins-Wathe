package cn.erindax.brinswathe.client.mixin;

import cn.erindax.brinswathe.client.BrinFuneralClient;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(Minecraft.class)
public abstract class FuneralInputMixin {
    @Inject(method = "startUseItem", at = @At("HEAD"), cancellable = true)
    private void brinFuneralUse(CallbackInfo ci) {
        if (BrinFuneralClient.handleUse()) ci.cancel();
    }

    @Inject(method = "startAttack", at = @At("HEAD"), cancellable = true)
    private void brinFuneralAttack(CallbackInfoReturnable<Boolean> cir) {
        if (BrinFuneralClient.isCarrying()) cir.setReturnValue(false);
    }
}
