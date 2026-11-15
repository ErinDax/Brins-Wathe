package cn.erindax.brinswathe.client.role;

import cn.erindax.brinswathe.ModRoles;
import cn.erindax.brinswathe.component.SurvivalMomentComponent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public final class SurvivalMomentClient {
    private SurvivalMomentClient() {
    }

    public static void init() {
        HudRenderCallback.EVENT.register(SurvivalMomentClient::render);
    }

    private static void render(GuiGraphics context, DeltaTracker tickCounter) {
        Minecraft client = Minecraft.getInstance();
        LocalPlayer player = client.player;
        if (player == null || client.options.hideGui) return;
        SurvivalMomentComponent moment = SurvivalMomentComponent.KEY.get(player.level());
        if (!moment.isActive()) return;
        Component line = Component.translatable("tip.brinswathe.watchman.survival_moment", moment.seconds());
        Font font = client.font;
        int x = (context.guiWidth() - font.width(line)) / 2;
        int y = context.guiHeight() / 2 + 60;
        context.drawString(font, line, x, y, ModRoles.WATCHMAN.color());
    }
}
