package cn.erindax.brinswathe.client.mixin;

import cn.erindax.brinswathe.client.BrinsWatheClient;
import cn.erindax.brinswathe.client.RpsHud;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(Entity.class)
public abstract class IllusionistBodyFreezeMixin {
    @Inject(method = "move", at = @At("HEAD"), cancellable = true)
    private void brinFreezeIllusionistBody(MoverType movementType, Vec3 movement, CallbackInfo ci) {
        if ((Object) this == Minecraft.getInstance().player
            && (BrinsWatheClient.isSniperAiming() || RpsHud.locksMovement())) {
            ci.cancel();
        }
    }
}
