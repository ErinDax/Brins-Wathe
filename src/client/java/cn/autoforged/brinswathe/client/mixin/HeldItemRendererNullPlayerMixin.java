package cn.autoforged.brinswathe.client.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemInHandRenderer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(value = ItemInHandRenderer.class, priority = 500)
public abstract class HeldItemRendererNullPlayerMixin {
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void brinSkipUpdateWithoutPlayer(CallbackInfo callbackInfo) {
        if (Minecraft.getInstance().player == null) callbackInfo.cancel();
    }
}
