package cn.autoforged.brinswathe.client.mixin;

import cn.autoforged.brinswathe.client.BrinsWatheClient;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Harpymodloader prints the looked-at player's role and modifier tags from its own mixin handler.
 * Cancelling that handler leaves the vanilla nametag (player name only) in place.
 */
@Environment(EnvType.CLIENT)
@Mixin(targets = "org.agmas.harpymodloader.client.mixin.CustomRolesRoleNameRendererMixin")
public abstract class CowboyDuelHideRoleNameMixin {
    @Inject(
        method = "b(Lnet/minecraft/client/gui/Font;Lnet/minecraft/client/player/LocalPlayer;Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfo;)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private static void brinHideRolesDuringCowboyDuel(CallbackInfo ci) {
        if (BrinsWatheClient.shouldHideSpectatorIdentities()) ci.cancel();
    }
}
