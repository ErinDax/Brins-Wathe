package cn.erindax.brinswathe.client;

import cn.erindax.brinswathe.Vagrant;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

@Environment(EnvType.CLIENT)
public final class VagrantClient {
    private VagrantClient() {
    }

    public static void init() {
        HudRenderCallback.EVENT.register(VagrantClient::render);
    }

    private static void render(GuiGraphics context, DeltaTracker tickCounter) {
        Minecraft client = Minecraft.getInstance();
        LocalPlayer player = client.player;
        if (player == null || client.options.hideGui) return;
        if (!(client.hitResult instanceof BlockHitResult hit) || hit.getType() != HitResult.Type.BLOCK) return;
        if (!Vagrant.canLieDown(player, hit)) return;
        Component line = Component.translatable("tip.brinswathe.vagrant.sleep");
        Font font = client.font;
        int x = (context.guiWidth() - font.width(line)) / 2;
        int y = context.guiHeight() / 2 + 24;
        context.drawString(font, line, x, y, Vagrant.COLOR);
    }
}
