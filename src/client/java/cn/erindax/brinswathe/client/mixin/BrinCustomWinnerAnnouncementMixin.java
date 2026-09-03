package cn.erindax.brinswathe.client.mixin;

import cn.erindax.brinswathe.component.BrinCustomWinnerComponent;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.client.gui.RoleAnnouncementTexts;
import dev.doctor4t.wathe.game.GameFunctions;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(value = RoleAnnouncementTexts.RoleAnnouncementText.class, priority = 500)
public abstract class BrinCustomWinnerAnnouncementMixin {
    @Inject(method = "getEndText", at = @At("HEAD"), cancellable = true)
    private void brinGetCustomEndText(GameFunctions.WinStatus status, Component winner,
                                      CallbackInfoReturnable<Component> cir) {
        if (Minecraft.getInstance().level == null) return;
        BrinCustomWinnerComponent winnerComponent = BrinCustomWinnerComponent.KEY.get(Minecraft.getInstance().level);
        if (!winnerComponent.hasCustomWinner()) {
            var player = Minecraft.getInstance().player;
            if (player != null
                && status == GameFunctions.WinStatus.KILLERS
                && brinIsThief(player)) {
                cir.setReturnValue(RoleAnnouncementTexts.KILLER.winText);
            }
            return;
        }

        cir.setReturnValue(Component.translatable(
            "announcement.win.brinswathe." + winnerComponent.getWinningTextId()
        ).withColor(winnerComponent.getColor()));
        cir.cancel();
    }

    @Unique
    private static boolean brinIsThief(Player player) {
        var role = GameWorldComponent.KEY.get(player.level()).getRole(player);
        return role != null
            && "stupid_express".equals(role.identifier().getNamespace())
            && "thief".equals(role.identifier().getPath());
    }
}
