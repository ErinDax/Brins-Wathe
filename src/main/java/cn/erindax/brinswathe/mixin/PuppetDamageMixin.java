package cn.erindax.brinswathe.mixin;

import cn.erindax.brinswathe.component.PuppeteerControlComponent;
import dev.doctor4t.wathe.entity.PlayerBodyEntity;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class PuppetDamageMixin {
    @Inject(method = "hurt", at = @At("HEAD"), cancellable = true)
    private void brinHandlePuppetModelDamage(DamageSource source, float amount,
                                             CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof ServerPlayer puppeteer) {
            PuppeteerControlComponent component = PuppeteerControlComponent.KEY.get(puppeteer);
            if (component != null && component.isControlling()) {
                Entity attacker = source.getEntity();
                if (attacker == puppeteer) {
                    cir.setReturnValue(false);
                    return;
                }
                if (attacker instanceof LivingEntity) {
                    component.handlePuppetKilled(puppeteer, attacker);
                    cir.setReturnValue(true);
                } else {
                    cir.setReturnValue(false);
                }
                return;
            }
        }
        if (!((Object) this instanceof PlayerBodyEntity body) || body.level().isClientSide) return;
        boolean puppet = PuppeteerControlComponent.isPuppet(body);
        boolean bodyProxy = PuppeteerControlComponent.isPuppetBodyProxy(body);
        if (!puppet && !bodyProxy) return;

        Entity sourceEntity = source.getEntity();
        if (!(sourceEntity instanceof LivingEntity)) {
            cir.setReturnValue(false);
            return;
        }
        if (!(PuppeteerControlComponent.puppeteerOf(body) instanceof ServerPlayer owner)) {
            body.discard();
            cir.setReturnValue(false);
            return;
        }

        PuppeteerControlComponent component = PuppeteerControlComponent.KEY.get(owner);
        if (component == null) {
            body.discard();
            cir.setReturnValue(false);
            return;
        }
        if (sourceEntity == owner) {
            cir.setReturnValue(false);
            return;
        }
        if (puppet) {
            component.handlePuppetKilled(owner, sourceEntity);
            cir.setReturnValue(true);
            return;
        }

        component.handleBodyProxyDestroyed(owner);
        Player killer = sourceEntity instanceof Player attacker ? attacker : owner;
        GameFunctions.killPlayer(owner, false, killer);
        cir.setReturnValue(true);
    }
}
