package cn.autoforged.brinswathe.client.mixin;

import cn.autoforged.brinswathe.client.BrinsWatheClient;
import cn.autoforged.brinswathe.component.MorticianComponent;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.doctor4t.wathe.game.GameFunctions;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(PlayerRenderer.class)
public abstract class MorticianOwnerRenderMixin {
    @Inject(
        method = "render(Lnet/minecraft/client/player/AbstractClientPlayer;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void brinHideDisguisedMortician(AbstractClientPlayer player, float entityYaw, float tickDelta,
                                            PoseStack matrices, MultiBufferSource vertexConsumers,
                                            int light, CallbackInfo ci) {
        // A puppet borrows a live player as its model carrier, and that carrier may happen to be the
        // mortician; cancelling here would erase the puppet instead of the mortician.
        if (BrinsWatheClient.isRenderingPuppetModel()) return;

        // Spectators watch the round from outside it, so they see the mortician crouching inside his own
        // fake corpse - the same way they see traps and mines.
        Player localPlayer = Minecraft.getInstance().player;
        if (localPlayer != null && GameFunctions.isPlayerSpectatingOrCreative(localPlayer)) return;

        MorticianComponent component = MorticianComponent.KEY.get(player);
        if (component == null || !component.isDisguised()) {
            // Wathe draws instinct sightings through throwaway player entities that carry a blank
            // component of their own, so the disguise has to be read off the real player.
            Player real = player.level().getPlayerByUUID(player.getUUID());
            component = real == null || real == player ? null : MorticianComponent.KEY.get(real);
        }
        if (component != null && component.isDisguised()) {
            ci.cancel();
        }
    }
}
