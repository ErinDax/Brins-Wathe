package cn.autoforged.brinswathe.client.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MultiPlayerGameMode.class)
public abstract class ClientPlayerInteractionManagerTickMixin {
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void brinswathe$skipTickWithoutPlayer(CallbackInfo callbackInfo) {
        if (Minecraft.getInstance().player == null) callbackInfo.cancel();
    }
}
