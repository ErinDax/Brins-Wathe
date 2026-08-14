package cn.autoforged.brinswathe.client.mixin;

import cn.autoforged.brinswathe.client.BrinsWatheClient;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(GameRenderer.class)
public abstract class SniperFovMixin {
    @Inject(method = "getFov", at = @At("RETURN"), cancellable = true)
    private void brinApplySniperZoom(Camera camera, float tickDelta, boolean changingFov,
                                     CallbackInfoReturnable<Double> cir) {
        if (BrinsWatheClient.isSniperAiming()) {
            cir.setReturnValue(cir.getReturnValue() * 0.35D);
        }
    }
}
