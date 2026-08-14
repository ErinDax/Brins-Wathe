package cn.autoforged.brinswathe.client.mixin;

import cn.autoforged.brinswathe.component.IllusionistComponent;
import cn.autoforged.brinswathe.client.BrinsWatheClient;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.doctor4t.wathe.entity.PlayerBodyEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import dev.doctor4t.wathe.client.render.entity.PlayerBodyEntityRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.WalkAnimationState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(PlayerBodyEntityRenderer.class)
public abstract class IllusionistCloneRenderMixin {
    @Inject(method = "render", at = @At("HEAD"), cancellable = true, remap = false)
    private void brinRenderCloneAsPlayer(PlayerBodyEntity entity, float entityYaw, float tickDelta,
                                         PoseStack matrices, MultiBufferSource vertexConsumers,
                                         int light, CallbackInfo ci) {
        EntityRendererAccessor bodyRenderer = (EntityRendererAccessor) this;
        if (!IllusionistComponent.isIllusionModel(entity)) {
            bodyRenderer.brinSetShadowRadius(0.0F);
            return;
        }
        if (BrinsWatheClient.isControllingClone(entity.getUUID())) {
            bodyRenderer.brinSetShadowRadius(0.0F);
            ci.cancel();
            return;
        }
        if (!(entity.level().getPlayerByUUID(entity.getPlayerUuid()) instanceof AbstractClientPlayer owner)) {
            bodyRenderer.brinSetShadowRadius(0.0F);
            ci.cancel();
            return;
        }
        bodyRenderer.brinSetShadowRadius(0.5F);

        EntityRenderer<? super AbstractClientPlayer> renderer = Minecraft.getInstance()
            .getEntityRenderDispatcher().getRenderer(owner);
        WalkAnimationState animation = owner.walkAnimation;
        WalkAnimationStateAccessor animationAccessor = (WalkAnimationStateAccessor) animation;
        WalkAnimationState cloneAnimation = entity.walkAnimation;
        WalkAnimationStateAccessor cloneAnimationAccessor = (WalkAnimationStateAccessor) cloneAnimation;

        float speedOld = animationAccessor.brinGetSpeedOld();
        float speed = animationAccessor.brinGetSpeed();
        float position = animationAccessor.brinGetPosition();
        float ownerYRot = owner.getYRot();
        float ownerYRotOld = owner.yRotO;
        float ownerBodyRot = owner.yBodyRot;
        float ownerBodyRotOld = owner.yBodyRotO;
        float ownerHeadRot = owner.getYHeadRot();
        float ownerHeadRotOld = owner.yHeadRotO;
        float ownerXRot = owner.getXRot();
        float ownerXRotOld = owner.xRotO;

        try {
            float cloneYaw = entity.getYRot();
            animationAccessor.brinSetSpeedOld(cloneAnimationAccessor.brinGetSpeedOld());
            animationAccessor.brinSetSpeed(cloneAnimationAccessor.brinGetSpeed());
            animationAccessor.brinSetPosition(cloneAnimationAccessor.brinGetPosition());
            owner.setYRot(cloneYaw);
            owner.yRotO = cloneYaw;
            owner.setYBodyRot(cloneYaw);
            owner.yBodyRotO = cloneYaw;
            owner.setYHeadRot(cloneYaw);
            owner.yHeadRotO = cloneYaw;
            owner.setXRot(entity.getXRot());
            owner.xRotO = entity.xRotO;
            BrinsWatheClient.beginIllusionModelRender();
            renderer.render(owner, cloneYaw, tickDelta, matrices, vertexConsumers, light);
        } finally {
            BrinsWatheClient.endIllusionModelRender();
            animationAccessor.brinSetSpeedOld(speedOld);
            animationAccessor.brinSetSpeed(speed);
            animationAccessor.brinSetPosition(position);
            owner.setYRot(ownerYRot);
            owner.yRotO = ownerYRotOld;
            owner.setYBodyRot(ownerBodyRot);
            owner.yBodyRotO = ownerBodyRotOld;
            owner.setYHeadRot(ownerHeadRot);
            owner.yHeadRotO = ownerHeadRotOld;
            owner.setXRot(ownerXRot);
            owner.xRotO = ownerXRotOld;
        }
        ci.cancel();
    }
}
