package cn.autoforged.brinswathe.client.mixin;

import cn.autoforged.brinswathe.entity.BoneharvestedCorpse;
import dev.doctor4t.wathe.client.render.entity.PlayerBodyEntityRenderer;
import dev.doctor4t.wathe.entity.PlayerBodyEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerBodyEntityRenderer.class)
public abstract class BoneharvestedCorpseRendererMixin {
    @Inject(method = "renderSkeleton", at = @At("HEAD"), cancellable = true)
    private void brinHideRemovedBones(
        PlayerBodyEntity body,
        float entityYaw,
        float tickDelta,
        PoseStack poseStack,
        MultiBufferSource buffer,
        int packedLight,
        float alpha,
        CallbackInfo ci
    ) {
        if (((BoneharvestedCorpse) body).brin$isBoneharvested()) ci.cancel();
    }
}
