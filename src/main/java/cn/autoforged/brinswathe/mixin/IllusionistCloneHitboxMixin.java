package cn.autoforged.brinswathe.mixin;

import cn.autoforged.brinswathe.component.IllusionistComponent;
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
public abstract class IllusionistCloneHitboxMixin {
    @Inject(method = "getDimensions", at = @At("HEAD"), cancellable = true)
    private void brinStandingIllusionHitbox(Pose pose, CallbackInfoReturnable<EntityDimensions> cir) {
        if (!((Object) this instanceof PlayerBodyEntity body)) return;
        if (!IllusionistComponent.isIllusionModel(body)) return;
        cir.setReturnValue(IllusionistComponent.ILLUSION_MODEL_DIMENSIONS);
    }

    @Inject(method = "getPickRadius", at = @At("HEAD"), cancellable = true)
    private void brinInflateIllusionPickRadius(CallbackInfoReturnable<Float> cir) {
        if ((Object) this instanceof PlayerBodyEntity body
            && IllusionistComponent.isIllusionModel(body)) {
            cir.setReturnValue(0.35F);
        }
    }

    @Inject(
        method = "onSyncedDataUpdated(Lnet/minecraft/network/syncher/EntityDataAccessor;)V",
        at = @At("TAIL")
    )
    private void brinRefreshIllusionDimensions(EntityDataAccessor<?> accessor, CallbackInfo ci) {
        if (!((Object) this instanceof PlayerBodyEntity body)) return;
        if (!IllusionistComponent.isIllusionModel(body)) return;
        if (body.getBbHeight() < 1.0F) {
            body.refreshDimensions();
        }
    }
}
