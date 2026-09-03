package cn.erindax.brinswathe.client.mixin;

import cn.erindax.brinswathe.client.BrinMorphlingClient;
import cn.erindax.brinswathe.client.BrinsWatheClient;
import cn.erindax.brinswathe.component.StuntDoubleComponent;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.resources.PlayerSkin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(AbstractClientPlayer.class)
public abstract class PlayerSkinMixin {

    @Inject(method = "getSkin", at = @At("HEAD"), cancellable = true)
    private void brinOverrideSkin(CallbackInfoReturnable<PlayerSkin> cir) {
        AbstractClientPlayer self = (AbstractClientPlayer)(Object)this;
        PlayerSkin puppetSkin = BrinsWatheClient.puppetSkinOverride(self);
        if (puppetSkin != null) {
            cir.setReturnValue(puppetSkin);
            return;
        }
        PlayerSkin morphSkin = BrinMorphlingClient.disguiseSkin(self);
        if (morphSkin != null) {
            cir.setReturnValue(morphSkin);
            return;
        }
        if (self.level().isClientSide) {
            StuntDoubleComponent sdComp = StuntDoubleComponent.KEY.get(self);
            if (sdComp != null && sdComp.isMimicking()) {
                PlayerInfo targetInfo = Minecraft.getInstance().getConnection().getPlayerInfo(sdComp.mimicTarget);
                if (targetInfo != null) {
                    cir.setReturnValue(targetInfo.getSkin());
                }
            }
        }
    }
}
