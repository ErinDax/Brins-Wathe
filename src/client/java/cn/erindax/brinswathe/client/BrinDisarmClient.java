package cn.erindax.brinswathe.client;

import cn.erindax.brinswathe.BrinDisarm;
import cn.erindax.brinswathe.component.DisarmComponent;
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
public final class BrinDisarmClient {
    private BrinDisarmClient() {
    }

    public static void init() {
        HudRenderCallback.EVENT.register(BrinDisarmClient::render);
    }

    private static void render(GuiGraphics context, DeltaTracker tickCounter) {
        Minecraft client = Minecraft.getInstance();
        LocalPlayer player = client.player;
        if (player == null || client.options.hideGui || !BrinDisarm.isDisarmed(player)) return;
        int seconds = (DisarmComponent.KEY.get(player.level()).ticks() + 19) / 20;
        Component line = Component.translatable("tip.brinswathe.inspector.disarmed", seconds);
        Font font = client.font;
        int x = (context.guiWidth() - font.width(line)) / 2;
        int y = context.guiHeight() / 2 + 36;
        context.drawString(font, line, x, y, BrinDisarm.COLOR);
    }
}
