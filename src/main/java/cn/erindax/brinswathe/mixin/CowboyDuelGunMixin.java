package cn.erindax.brinswathe.mixin;

import cn.erindax.brinswathe.CowboyDuel;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.doctor4t.wathe.util.GunShootPayload;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

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
