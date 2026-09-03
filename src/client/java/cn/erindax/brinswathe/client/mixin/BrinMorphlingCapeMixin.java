package cn.erindax.brinswathe.client.mixin;

import cn.erindax.brinswathe.client.BrinMorphlingClient;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.layers.CapeLayer;
import net.minecraft.client.resources.PlayerSkin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Environment(EnvType.CLIENT)
@Mixin(value = CapeLayer.class, priority = 2000)
public abstract class BrinMorphlingCapeMixin {
    @WrapOperation(
        method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/player/AbstractClientPlayer;FFFFFF)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/player/AbstractClientPlayer;getSkin()Lnet/minecraft/client/resources/PlayerSkin;"
        )
    )
    private PlayerSkin brinDisguiseCape(AbstractClientPlayer player, Operation<PlayerSkin> original) {
        PlayerSkin disguise = BrinMorphlingClient.disguiseSkin(player);
        return disguise != null ? disguise : original.call(player);
    }
}
