package cn.erindax.brinswathe.mixin;

import cn.erindax.brinswathe.component.BombComponent;
import cn.erindax.brinswathe.component.MorticianComponent;
import dev.doctor4t.wathe.entity.PlayerBodyEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class MorticianDisguiseMixin {
    @Inject(method = "isPickable", at = @At("HEAD"), cancellable = true)
    private void brinIgnoreDisguiseBodyPicking(CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof PlayerBodyEntity body && MorticianComponent.isDisguiseBody(body)) {
            cir.setReturnValue(false);
        }
    }
    @Inject(method = "isPushable", at = @At("HEAD"), cancellable = true)
    private void brinAnchorDisguisedMortician(CallbackInfoReturnable<Boolean> cir) {
        if (brinIsDisguised()) cir.setReturnValue(false);
    }
    @Inject(method = "pushEntities", at = @At("HEAD"), cancellable = true)
    private void brinSkipDisguisedMorticianPush(CallbackInfo ci) {
        if (brinIsDisguised()) ci.cancel();
    }

    @Inject(method = "knockback", at = @At("HEAD"), cancellable = true)
    private void brinSkipAnchoredKnockback(double strength, double x, double z, CallbackInfo ci) {
        if (brinIsAnchored()) ci.cancel();
    }

    @Unique
    private boolean brinIsDisguised() {
        return brinIsAnchored();
    }
    @Unique
    private boolean brinIsAnchored() {
        if (!((Object) this instanceof Player player)) return false;
        MorticianComponent mortician = MorticianComponent.KEY.get(player);
        if (mortician != null && mortician.isDisguised()) return true;
        BombComponent bomb = BombComponent.KEY.get(player);
        return bomb != null && bomb.isMinePinned();
    }
}
