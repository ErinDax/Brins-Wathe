package cn.erindax.brinswathe.client.mixin;

import cn.erindax.brinswathe.client.RpsHud;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(KeyboardHandler.class)
public abstract class RpsKeyMixin {
    @Inject(method = "keyPress", at = @At("HEAD"), cancellable = true)
    private void brinHandleRpsKeys(long window, int key, int scancode, int action, int modifiers, CallbackInfo ci) {
        Minecraft client = Minecraft.getInstance();
        if (window != client.getWindow().getWindow()) return;
        if (RpsHud.handleKey(key, action)) ci.cancel();
    }
}
