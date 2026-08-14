package cn.autoforged.brinswathe.mixin;

import cn.autoforged.brinswathe.component.IllusionistComponent;
import dev.doctor4t.wathe.entity.PlayerBodyEntity;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerBodyEntity.class)
public abstract class IllusionistClonePushMixin {
    @Inject(method = "push(Lnet/minecraft/world/entity/Entity;)V", at = @At("HEAD"), cancellable = true)
    private void brinCloneTakePush(Entity other, CallbackInfo ci) {
        PlayerBodyEntity self = (PlayerBodyEntity) (Object) this;
        if (!brinCloneShouldPush(self, other)) return;

        double dx = other.getX() - self.getX();
        double dz = other.getZ() - self.getZ();
        double max = Mth.absMax(dx, dz);
        if (max >= 0.01) {
            max = Math.sqrt(max);
            dx /= max;
            dz /= max;
            double scale = Math.min(1.0, 1.0 / max) * 0.05;
            dx *= scale;
            dz *= scale;
            if (!other.isVehicle() && other.isPushable()) {
                other.push(dx, 0.0, dz);
            }
        }
        ci.cancel();
    }

    @Inject(method = "doPush(Lnet/minecraft/world/entity/Entity;)V", at = @At("HEAD"), cancellable = true)
    private void brinCloneGivePush(Entity other, CallbackInfo ci) {
        PlayerBodyEntity self = (PlayerBodyEntity) (Object) this;
        if (!brinCloneShouldPush(self, other)) return;
        other.push(self);
        ci.cancel();
    }

    private static boolean brinCloneShouldPush(PlayerBodyEntity self, Entity other) {
        if (!IllusionistComponent.isClone(self)) return false;
        if (self.noPhysics || other.noPhysics) return false;
        if (self.isPassengerOfSameVehicle(other)) return false;
        return !other.getUUID().equals(self.getPlayerUuid());
    }
}
