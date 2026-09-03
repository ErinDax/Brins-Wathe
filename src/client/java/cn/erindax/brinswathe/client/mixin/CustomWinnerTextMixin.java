package cn.erindax.brinswathe.client.mixin;

import cn.erindax.brinswathe.component.ModCustomWinnerComponent;
import dev.doctor4t.wathe.client.gui.RoundTextRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Environment(EnvType.CLIENT)
@Mixin(RoundTextRenderer.class)
public abstract class CustomWinnerTextMixin {
    @ModifyVariable(method = "renderHud", at = @At("STORE"), name = "winMessage")
    private static MutableComponent brinRenderCustomWinText(MutableComponent winMessage) {
        if (Minecraft.getInstance().level == null) return winMessage;
        ModCustomWinnerComponent winnerComponent = ModCustomWinnerComponent.KEY.get(Minecraft.getInstance().level);
        if (!winnerComponent.hasCustomWinner()) return winMessage;
        return Component.translatable(
            "game.win.brinswathe." + winnerComponent.getWinningTextId()
        );
    }
}
