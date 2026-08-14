package cn.autoforged.brinswathe.client.mixin;

import cn.autoforged.brinswathe.client.BrinsWatheClient;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(Entity.class)
public abstract class CowboyDuelLookLockMixin {
    @Inject(method = "turn", at = @At("HEAD"), cancellable = true)
    private void brinLockLookDuringDuel(double yaw, double pitch, CallbackInfo ci) {
        if ((Object) this != Minecraft.getInstance().player) return;
        if (BrinsWatheClient.isDuelLookLocked()) ci.cancel();
    }
}
