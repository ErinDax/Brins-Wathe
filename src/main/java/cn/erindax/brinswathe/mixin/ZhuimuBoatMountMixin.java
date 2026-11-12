package cn.erindax.brinswathe.mixin;

import cn.erindax.brinswathe.entity.ZhuimuBoat;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class ZhuimuBoatMountMixin {
    @Inject(method = "startRiding(Lnet/minecraft/world/entity/Entity;Z)Z", at = @At("HEAD"), cancellable = true)
    private void brinRefuseOtherVehicles(Entity vehicle, boolean force, CallbackInfoReturnable<Boolean> cir) {
        if (((Entity) (Object) this).getVehicle() instanceof ZhuimuBoat) cir.setReturnValue(false);
    }
}
