package cn.autoforged.brinswathe.client.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.doctor4t.wathe.client.render.entity.PlayerBodyEntityRenderer;
import dev.doctor4t.wathe.entity.PlayerBodyEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(value = PlayerBodyEntityRenderer.class, priority = 2000)
public abstract class BuriedCorpseRenderMixin {
    @Inject(method = "render", at = @At("HEAD"), cancellable = true, remap = false)
    private void brinHideBuriedCorpse(
        PlayerBodyEntity entity,
        float entityYaw,
        float tickDelta,
        PoseStack matrices,
        MultiBufferSource vertexConsumers,
        int light,
        CallbackInfo ci
    ) {
        if (!entity.isInvisible()) return;
        ((EntityRendererAccessor) this).brinSetShadowRadius(0.0F);
        ci.cancel();
    }
}
