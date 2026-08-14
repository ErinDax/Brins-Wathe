package cn.autoforged.brinswathe.client.mixin;

import cn.autoforged.brinswathe.client.BrinsWatheClient;
import cn.autoforged.brinswathe.component.IllusionistComponent;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(PlayerRenderer.class)
public abstract class IllusionistOwnerRenderMixin {
    @Inject(
        method = "render(Lnet/minecraft/client/player/AbstractClientPlayer;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void brinHideOwnerWhileControlling(AbstractClientPlayer player, float entityYaw, float tickDelta,
                                                PoseStack matrices, MultiBufferSource vertexConsumers,
                                                int light, CallbackInfo ci) {
        if (BrinsWatheClient.isRenderingIllusionModel()) return;
        IllusionistComponent component = IllusionistComponent.KEY.get(player);
        if (component.controlledCloneId != null) {
            ci.cancel();
        }
    }
}
