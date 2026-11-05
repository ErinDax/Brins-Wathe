package cn.erindax.brinswathe.mixin;

import cn.erindax.brinswathe.BrinConspirator;
import cn.erindax.brinswathe.CowboyDuel;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.util.GunShootPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GunShootPayload.Receiver.class)
public abstract class ConspiratorGunShootMixin {
    @Inject(
        method = "receive(Ldev/doctor4t/wathe/util/GunShootPayload;Lnet/fabricmc/fabric/api/networking/v1/ServerPlayNetworking$Context;)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void brinRejectConspiratorShot(GunShootPayload payload, ServerPlayNetworking.Context context, CallbackInfo ci) {
        ServerPlayer shooter = context.player();
        if (!BrinConspirator.isConspirator(GameWorldComponent.KEY.get(shooter.level()), shooter)) return;
        if (CowboyDuel.isDuelGun(shooter.getMainHandItem())) return;
        ci.cancel();
    }
}
