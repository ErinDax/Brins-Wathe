package cn.erindax.brinswathe.mixin;

import cn.erindax.brinswathe.component.MorticianComponent;
import dev.doctor4t.wathe.index.WatheItems;
import dev.doctor4t.wathe.util.KnifeStabPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KnifeStabPayload.Receiver.class)
public abstract class MorticianKnifeStabMixin {
    @Inject(
        method = "receive(Ldev/doctor4t/wathe/util/KnifeStabPayload;Lnet/fabricmc/fabric/api/networking/v1/ServerPlayNetworking$Context;)V",
        at = @At("TAIL")
    )
    private void brinRefundAmbushKnife(
        KnifeStabPayload payload,
        ServerPlayNetworking.Context context,
        CallbackInfo ci
    ) {
        ServerPlayer attacker = context.player();
        MorticianComponent component = MorticianComponent.KEY.get(attacker);
        if (component == null || !component.consumeAmbushKnifeRefund()) return;
        attacker.getCooldowns().removeCooldown(WatheItems.KNIFE);
    }
}
