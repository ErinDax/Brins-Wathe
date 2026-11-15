package cn.erindax.brinswathe.client.mixin;

import cn.erindax.brinswathe.client.role.ZhuimuClient;
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
public abstract class ZhuimuStruggleKeyMixin {
    @Inject(method = "keyPress", at = @At("HEAD"))
    private void brinCountZhuimuStruggle(long window, int key, int scancode, int action, int modifiers, CallbackInfo ci) {
        if (window == Minecraft.getInstance().getWindow().getWindow()) ZhuimuClient.onKeyPress(key, scancode, action);
    }
}
