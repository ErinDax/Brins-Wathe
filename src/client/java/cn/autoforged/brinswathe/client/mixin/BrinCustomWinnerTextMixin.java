package cn.autoforged.brinswathe.client.mixin;

import cn.autoforged.brinswathe.component.BrinCustomWinnerComponent;
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
public abstract class BrinCustomWinnerTextMixin {
    @ModifyVariable(method = "renderHud", at = @At("STORE"), name = "winMessage")
    private static MutableComponent brinRenderCustomWinText(MutableComponent winMessage) {
        if (Minecraft.getInstance().level == null) return winMessage;
        BrinCustomWinnerComponent winnerComponent = BrinCustomWinnerComponent.KEY.get(Minecraft.getInstance().level);
        if (!winnerComponent.hasCustomWinner()) return winMessage;
        return Component.translatable(
            "game.win.brinswathe." + winnerComponent.getWinningTextId()
        );
    }
}
