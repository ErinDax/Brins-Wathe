package cn.autoforged.brinswathe.mixin;

import cn.autoforged.brinswathe.component.PuppeteerControlComponent;
import dev.doctor4t.wathe.entity.PlayerBodyEntity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class PuppetCollisionMixin {
    /**
     * The puppet rides on top of the puppeteer's own hitbox, so letting it push as well would double every
     * shove the controller already delivers.
     */
    @Inject(method = "isPushable", at = @At("HEAD"), cancellable = true)
    private void brinConfigurePuppetPushing(CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof PlayerBodyEntity body
            && PuppeteerControlComponent.isPuppetModel(body)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "isPickable", at = @At("HEAD"), cancellable = true)
    private void brinConfigurePuppetTargeting(CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof PlayerBodyEntity body
            && PuppeteerControlComponent.isPuppetModel(body)) {
            cir.setReturnValue(true);
        }
    }
}
