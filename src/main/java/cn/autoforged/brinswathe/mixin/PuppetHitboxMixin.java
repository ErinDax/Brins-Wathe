package cn.autoforged.brinswathe.mixin;

import cn.autoforged.brinswathe.component.PuppeteerControlComponent;
import dev.doctor4t.wathe.entity.PlayerBodyEntity;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class PuppetHitboxMixin {
    @Inject(method = "getDimensions", at = @At("HEAD"), cancellable = true)
    private void brinStandingPuppetHitbox(Pose pose, CallbackInfoReturnable<EntityDimensions> cir) {
        if (!((Object) this instanceof PlayerBodyEntity body)) return;
        if (!PuppeteerControlComponent.isPuppetModel(body)) return;
        cir.setReturnValue(PuppeteerControlComponent.PUPPET_MODEL_DIMENSIONS);
    }

    @Inject(method = "getPickRadius", at = @At("HEAD"), cancellable = true)
    private void brinInflatePuppetPickRadius(CallbackInfoReturnable<Float> cir) {
        if ((Object) this instanceof PlayerBodyEntity body
            && PuppeteerControlComponent.isPuppetModel(body)) {
            cir.setReturnValue(0.35F);
        }
    }

    @Inject(
        method = "onSyncedDataUpdated(Lnet/minecraft/network/syncher/EntityDataAccessor;)V",
        at = @At("TAIL")
    )
    private void brinRefreshPuppetDimensions(EntityDataAccessor<?> accessor, CallbackInfo ci) {
        if (!((Object) this instanceof PlayerBodyEntity body)) return;
        if (!PuppeteerControlComponent.isPuppetModel(body)) return;
        if (body.getBbHeight() < 1.0F) {
            body.refreshDimensions();
        }
    }
}
