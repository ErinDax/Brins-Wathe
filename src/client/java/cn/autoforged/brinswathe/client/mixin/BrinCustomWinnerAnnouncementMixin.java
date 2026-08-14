package cn.autoforged.brinswathe.client.mixin;

import cn.autoforged.brinswathe.component.BrinCustomWinnerComponent;
import dev.doctor4t.wathe.client.gui.RoleAnnouncementTexts;
import dev.doctor4t.wathe.game.GameFunctions;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
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
        if (!winnerComponent.hasCustomWinner()) return;

        cir.setReturnValue(Component.translatable(
            "announcement.win.brinswathe." + winnerComponent.getWinningTextId()
        ).withColor(winnerComponent.getColor()));
        cir.cancel();
    }
}
