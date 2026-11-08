package cn.erindax.brinswathe.client.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.doctor4t.wathe.client.model.entity.PlayerSkeletonEntityModel;
import dev.doctor4t.wathe.client.render.entity.PlayerBodyEntityRenderer;
import dev.doctor4t.wathe.entity.PlayerBodyEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(PlayerBodyEntityRenderer.class)
public abstract class FuneralCarriedCorpseRenderMixin {
    @Shadow(remap = false)
    protected PlayerSkeletonEntityModel<PlayerBodyEntity> skeletonModel;

    @Inject(method = "render", at = @At("HEAD"), cancellable = true, remap = false)
    private void brinHideCorpseCarriedByCamera(
        PlayerBodyEntity body,
        float entityYaw,
        float tickDelta,
        PoseStack poseStack,
        MultiBufferSource buffer,
        int light,
        CallbackInfo ci
    ) {
        Minecraft client = Minecraft.getInstance();
        Entity vehicle = body.getVehicle();
        if (vehicle != null && vehicle == client.getCameraEntity() && client.options.getCameraType().isFirstPerson()) {
            ci.cancel();
        }
    }

    @Inject(method = "renderSkeleton", at = @At("HEAD"), remap = false)
    private void brinPoseCarriedSkeleton(
        PlayerBodyEntity body,
        float entityYaw,
        float tickDelta,
        PoseStack poseStack,
        MultiBufferSource buffer,
        int light,
        float alpha,
        CallbackInfo ci
    ) {
        this.skeletonModel.riding = body.isPassenger();
    }
}
