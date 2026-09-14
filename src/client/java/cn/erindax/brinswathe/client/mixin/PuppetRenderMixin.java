package cn.erindax.brinswathe.client.mixin;

import cn.erindax.brinswathe.client.BrinsWatheClient;
import cn.erindax.brinswathe.component.PuppeteerControlComponent;
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
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
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
        AbstractClientPlayer controller = BrinsWatheClient.puppetController(entity);

        matrices.pushPose();
        try {
            float renderYaw;
            if (controller != null && PuppeteerControlComponent.isPuppet(entity)) {
                Vec3 from = entity.getPosition(tickDelta);
                Vec3 to = controller.getPosition(tickDelta);
                matrices.translate(to.x - from.x, to.y - from.y, to.z - from.z);
                if (carrier != controller) {
                    WalkAnimationStateAccessor controllerAnimation =
                        (WalkAnimationStateAccessor) controller.walkAnimation;
                    carrierAnimation.brinSetSpeedOld(controllerAnimation.brinGetSpeedOld());
                    carrierAnimation.brinSetSpeed(controllerAnimation.brinGetSpeed());
                    carrierAnimation.brinSetPosition(controllerAnimation.brinGetPosition());
                    carrier.setYRot(controller.getYRot());
                    carrier.yRotO = controller.yRotO;
                    carrier.setYBodyRot(controller.yBodyRot);
                    carrier.yBodyRotO = controller.yBodyRotO;
                    carrier.setYHeadRot(controller.getYHeadRot());
                    carrier.yHeadRotO = controller.yHeadRotO;
                    carrier.setXRot(controller.getXRot());
                    carrier.xRotO = controller.xRotO;
                }
                renderYaw = Mth.rotLerp(tickDelta, carrier.yRotO, carrier.getYRot());
            } else {
                WalkAnimationStateAccessor puppetAnimation =
                    (WalkAnimationStateAccessor) entity.walkAnimation;
                carrierAnimation.brinSetSpeedOld(puppetAnimation.brinGetSpeedOld());
                carrierAnimation.brinSetSpeed(puppetAnimation.brinGetSpeed());
                carrierAnimation.brinSetPosition(puppetAnimation.brinGetPosition());
                carrier.setYRot(entity.getYRot());
                carrier.yRotO = entity.yRotO;
                carrier.setYBodyRot(entity.yBodyRot);
                carrier.yBodyRotO = entity.yBodyRotO;
                carrier.setYHeadRot(entity.getYHeadRot());
                carrier.yHeadRotO = entity.yHeadRotO;
                carrier.setXRot(entity.getXRot());
                carrier.xRotO = entity.xRotO;
                renderYaw = Mth.rotLerp(tickDelta, entity.yRotO, entity.getYRot());
            }
            BrinsWatheClient.beginPuppetModelRender(carrier, skin);
            EntityRenderer<? super AbstractClientPlayer> renderer = Minecraft.getInstance()
                .getEntityRenderDispatcher().getRenderer(carrier);
            renderer.render(carrier, renderYaw, tickDelta, matrices, vertexConsumers, light);
        } finally {
            matrices.popPose();
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
