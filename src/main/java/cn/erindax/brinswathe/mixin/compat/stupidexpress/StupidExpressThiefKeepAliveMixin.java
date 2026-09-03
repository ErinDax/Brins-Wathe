package cn.erindax.brinswathe.mixin.compat.stupidexpress;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "pro.fazeclan.river.stupid_express.role.thief.ThiefItemTracker", remap = false)
public abstract class StupidExpressThiefKeepAliveMixin {
    @Inject(method = "isWeaponAvailable", at = @At("HEAD"), cancellable = true, require = 0, remap = false)
    private static void brinThiefSettlesWithKillers(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }
}
