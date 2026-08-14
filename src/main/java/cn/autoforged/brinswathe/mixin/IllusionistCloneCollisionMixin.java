package cn.autoforged.brinswathe.mixin;

import cn.autoforged.brinswathe.component.IllusionistComponent;
import dev.doctor4t.wathe.entity.PlayerBodyEntity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class IllusionistCloneCollisionMixin {
    @Inject(method = "isPushable", at = @At("HEAD"), cancellable = true)
    private void brinConfigureIllusionModelPushing(CallbackInfoReturnable<Boolean> cir) {
        if (!((Object) this instanceof PlayerBodyEntity body)) return;
        if (IllusionistComponent.isClone(body)) {
            cir.setReturnValue(true);
        } else if (IllusionistComponent.isBodyProxy(body)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "isPickable", at = @At("HEAD"), cancellable = true)
    private void brinConfigureIllusionModelTargeting(CallbackInfoReturnable<Boolean> cir) {
        if (!((Object) this instanceof PlayerBodyEntity body)) return;
        if (IllusionistComponent.isIllusionModel(body)) {
            cir.setReturnValue(true);
        }
    }
}
