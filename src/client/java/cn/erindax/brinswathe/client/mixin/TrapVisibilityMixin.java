package cn.erindax.brinswathe.client.mixin;

import cn.erindax.brinswathe.BrinRoles;
import cn.erindax.brinswathe.entity.BomberMine;
import cn.erindax.brinswathe.entity.TrapperFangs;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.SkullModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.client.renderer.entity.EvokerFangsRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.EvokerFangs;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(EvokerFangsRenderer.class)
public abstract class TrapVisibilityMixin {
    @Unique
    private static final RenderType BRIN_CREEPER_HEAD = RenderType.entityCutoutNoCullZOffset(
        ResourceLocation.withDefaultNamespace("textures/entity/creeper/creeper.png")
    );
    @Unique
    private static final double BRIN_MINE_SINK = 0.25D;
    @Unique
    private static EntityModelSet brinSkullModelSet;

    @Unique
    private static SkullModel brinCreeperHead;

    @Inject(
        method = "render(Lnet/minecraft/world/entity/projectile/EvokerFangs;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void brinHideTrapFromOthers(EvokerFangs entity, float f, float g, PoseStack poseStack,
                                        MultiBufferSource multiBufferSource, int i, CallbackInfo ci) {
        if (!((TrapperFangs) entity).brin$isTrapperTrap()) return;
        Player localPlayer = Minecraft.getInstance().player;
        if (localPlayer == null) {
            ci.cancel();
            return;
        }
        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(localPlayer.level());
        if (!gameWorld.canUseKillerFeatures(localPlayer)
            && !gameWorld.isRole(localPlayer, BrinRoles.WATCHMAN)
            && !GameFunctions.isPlayerSpectatingOrCreative(localPlayer)) {
            ci.cancel();
            return;
        }
        if (((BomberMine) entity).brin$isBomberMine()) {
            brinRenderMine(entity, poseStack, multiBufferSource, i);
            ci.cancel();
        }
    }
    @Unique
    private static void brinRenderMine(EvokerFangs entity, PoseStack poseStack,
                                       MultiBufferSource buffers, int light) {
        poseStack.pushPose();
        poseStack.translate(-0.5D, -BRIN_MINE_SINK, -0.5D);
        SkullBlockRenderer.renderSkull(
            null,
            entity.getYRot(),
            0.0F,
            poseStack,
            buffers,
            light,
            brinCreeperHead(),
            BRIN_CREEPER_HEAD
        );
        poseStack.popPose();
    }
    @Unique
    private static SkullModel brinCreeperHead() {
        EntityModelSet models = Minecraft.getInstance().getEntityModels();
        if (brinCreeperHead == null || brinSkullModelSet != models) {
            brinSkullModelSet = models;
            brinCreeperHead = new SkullModel(models.bakeLayer(ModelLayers.CREEPER_HEAD));
        }
        return brinCreeperHead;
    }
}
