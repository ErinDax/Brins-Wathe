package cn.erindax.brinswathe.mixin;

import cn.erindax.brinswathe.BrinNoelleAccess;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "org.agmas.noellesroles.recaller.RecallerPlayerComponent", remap = false)
public abstract class BrinRecallerInstantMixin {
    @Shadow
    @Final
    private Player player;

    @Inject(method = "setPosition", at = @At("TAIL"))
    private void brinAllowInstantRecall(CallbackInfo ci) {
        if (this.player instanceof ServerPlayer serverPlayer) {
            BrinNoelleAccess.setNoelleAbilityCooldown(serverPlayer, 0);
        }
    }
}
