package cn.erindax.brinswathe.mixin;

import cn.erindax.brinswathe.BrinItemCooldowns;
import cn.erindax.brinswathe.component.PuppeteerControlComponent;
import dev.doctor4t.wathe.entity.PlayerBodyEntity;
import dev.doctor4t.wathe.game.GameConstants;
import dev.doctor4t.wathe.index.WatheSounds;
import dev.doctor4t.wathe.util.KnifeStabPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KnifeStabPayload.Receiver.class)
public abstract class PuppetKnifeStabMixin {
    @Inject(
        method = "receive(Ldev/doctor4t/wathe/util/KnifeStabPayload;Lnet/fabricmc/fabric/api/networking/v1/ServerPlayNetworking$Context;)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void brinHandlePuppetKnife(KnifeStabPayload payload,
                                        ServerPlayNetworking.Context context,
                                        CallbackInfo ci) {
        ServerPlayer attacker = context.player();
        Entity target = attacker.serverLevel().getEntity(payload.target());
        if (!(target instanceof PlayerBodyEntity body)) return;
        if (!PuppeteerControlComponent.isPuppetModel(body)) return;
        if (PuppeteerControlComponent.puppeteerOf(body) == attacker) {
            ci.cancel();
            return;
        }

        if (body.distanceTo(attacker) > 3.0F) {
            ci.cancel();
            return;
        }

        body.playSound(WatheSounds.ITEM_KNIFE_STAB, 1.0F, 1.0F);
        attacker.swing(InteractionHand.MAIN_HAND);
        BrinItemCooldowns.applyKnifeStab(attacker);
        PuppeteerControlComponent.resolveWeaponHit(body, attacker, GameConstants.DeathReasons.KNIFE);
        ci.cancel();
    }
}
