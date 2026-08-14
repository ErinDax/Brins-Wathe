package cn.autoforged.brinswathe.mixin;

import cn.autoforged.brinswathe.CowboyDuel;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.doctor4t.wathe.util.GunShootPayload;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Wathe punishes an innocent gunning down another innocent: the shot backfires and kills the shooter
 * instead, and the gun is confiscated. A duel is faction-blind by design - the cowboy may call out
 * anybody - so while one is running the victim-is-innocent check is answered with "no", which routes
 * the shot straight to the plain kill.
 */
@Mixin(GunShootPayload.Receiver.class)
public abstract class CowboyDuelGunMixin {

    @ModifyExpressionValue(
        method = "receive(Ldev/doctor4t/wathe/util/GunShootPayload;Lnet/fabricmc/fabric/api/networking/v1/ServerPlayNetworking$Context;)V",
        at = @At(
            value = "INVOKE",
            target = "Ldev/doctor4t/wathe/cca/GameWorldComponent;isInnocent(Lnet/minecraft/world/entity/player/Player;)Z",
            ordinal = 0
        )
    )
    private boolean brinDuelIgnoresFactions(boolean original) {
        return !CowboyDuel.isActive() && original;
    }
}
