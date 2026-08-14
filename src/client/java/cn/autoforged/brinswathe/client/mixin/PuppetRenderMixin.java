package cn.autoforged.brinswathe.client.mixin;

import cn.autoforged.brinswathe.client.BrinsWatheClient;
import cn.autoforged.brinswathe.component.PuppeteerControlComponent;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.doctor4t.wathe.client.render.entity.PlayerBodyEntityRenderer;
import dev.doctor4t.wathe.entity.PlayerBodyEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.world.entity.WalkAnimationState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(PlayerBodyEntityRenderer.class)
public abstract class PuppetRenderMixin {
    @Inject(method = "render", at = @At("HEAD"), cancellable = true, remap = false)
    private void brinRenderPuppetAsPlayer(PlayerBodyEntity entity, float entityYaw, float tickDelta,
                                          PoseStack matrices, MultiBufferSource vertexConsumers,
                                          int light, CallbackInfo ci) {
        if (!PuppeteerControlComponent.isPuppetModel(entity)) return;

        EntityRendererAccessor bodyRenderer = (EntityRendererAccessor) this;
        if (BrinsWatheClient.isControllingPuppet(entity.getUUID())) {
            bodyRenderer.brinSetShadowRadius(0.0F);
            ci.cancel();
            return;
        }

        AbstractClientPlayer carrier = BrinsWatheClient.puppetRenderCarrier(entity);
        PlayerSkin skin = BrinsWatheClient.puppetRenderSkin(entity);
        if (carrier == null || skin == null) {
            bodyRenderer.brinSetShadowRadius(0.0F);
            return;
        }
        bodyRenderer.brinSetShadowRadius(0.5F);

        WalkAnimationStateAccessor carrierAnimation = (WalkAnimationStateAccessor) carrier.walkAnimation;
        WalkAnimationState puppetAnimation = entity.walkAnimation;
        WalkAnimationStateAccessor puppetAnimationAccessor = (WalkAnimationStateAccessor) puppetAnimation;

        float speedOld = carrierAnimation.brinGetSpeedOld();
        float speed = carrierAnimation.brinGetSpeed();
        float position = carrierAnimation.brinGetPosition();
        float carrierYRot = carrier.getYRot();
        float carrierYRotOld = carrier.yRotO;
        float carrierBodyRot = carrier.yBodyRot;
        float carrierBodyRotOld = carrier.yBodyRotO;
        float carrierHeadRot = carrier.getYHeadRot();
        float carrierHeadRotOld = carrier.yHeadRotO;
        float carrierXRot = carrier.getXRot();
        float carrierXRotOld = carrier.xRotO;

        try {
            float puppetYaw = entity.getYRot();
            carrierAnimation.brinSetSpeedOld(puppetAnimationAccessor.brinGetSpeedOld());
            carrierAnimation.brinSetSpeed(puppetAnimationAccessor.brinGetSpeed());
            carrierAnimation.brinSetPosition(puppetAnimationAccessor.brinGetPosition());
            carrier.setYRot(puppetYaw);
            carrier.yRotO = puppetYaw;
            carrier.setYBodyRot(puppetYaw);
            carrier.yBodyRotO = puppetYaw;
            carrier.setYHeadRot(puppetYaw);
            carrier.yHeadRotO = puppetYaw;
            carrier.setXRot(entity.getXRot());
            carrier.xRotO = entity.xRotO;
            BrinsWatheClient.beginPuppetModelRender(carrier, skin);
            EntityRenderer<? super AbstractClientPlayer> renderer = Minecraft.getInstance()
                .getEntityRenderDispatcher().getRenderer(carrier);
            renderer.render(carrier, puppetYaw, tickDelta, matrices, vertexConsumers, light);
        } finally {
            BrinsWatheClient.endPuppetModelRender();
            carrierAnimation.brinSetSpeedOld(speedOld);
            carrierAnimation.brinSetSpeed(speed);
            carrierAnimation.brinSetPosition(position);
            carrier.setYRot(carrierYRot);
            carrier.yRotO = carrierYRotOld;
            carrier.setYBodyRot(carrierBodyRot);
            carrier.yBodyRotO = carrierBodyRotOld;
            carrier.setYHeadRot(carrierHeadRot);
            carrier.yHeadRotO = carrierHeadRotOld;
            carrier.setXRot(carrierXRot);
            carrier.xRotO = carrierXRotOld;
        }
        ci.cancel();
    }
}
