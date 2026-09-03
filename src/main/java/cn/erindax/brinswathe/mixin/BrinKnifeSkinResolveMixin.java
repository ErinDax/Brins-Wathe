package cn.erindax.brinswathe.mixin;

import cn.erindax.brinswathe.BrinKnifeSkins;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "dev.doctor4t.wathe.item.KnifeItem$Skin", remap = false)
public class BrinKnifeSkinResolveMixin {
    @Inject(method = "resolveName", at = @At("RETURN"), cancellable = true, require = 0)
    private static void brinResolveDynamicSkin(String input, CallbackInfoReturnable<String> cir) {
        if (cir.getReturnValue() != null || input == null) return;
        String resolved = BrinKnifeSkins.resolveBundled(input);
        if (resolved == null) resolved = BrinKnifeSkins.resolveDynamic(input);
        if (resolved != null) cir.setReturnValue(resolved);
    }
}
