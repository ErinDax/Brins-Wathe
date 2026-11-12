package cn.erindax.brinswathe.client;

import cn.erindax.brinswathe.ModEntities;
import cn.erindax.brinswathe.ModRoles;
import cn.erindax.brinswathe.config.ModConfig;
import cn.erindax.brinswathe.entity.ZhuimuBoat;
import cn.erindax.brinswathe.network.AbilityC2SPacket;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.entity.BoatRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public final class ZhuimuClient {
    private static boolean wasTrapped;

    private ZhuimuClient() {
    }

    public static void init() {
        EntityRendererRegistry.<Boat>register(ModEntities.ZHUIMU_BOAT, context -> new BoatRenderer(context, false));
        HudRenderCallback.EVENT.register(ZhuimuClient::render);
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            boolean trapped = trappingBoat(client.player) != null;
            if (trapped && !wasTrapped) client.gui.setOverlayMessage(Component.empty(), false);
            wasTrapped = trapped;
        });
    }

    public static void onKeyPress(int key, int scancode, int action) {
        Minecraft client = Minecraft.getInstance();
        if (action != GLFW.GLFW_PRESS || client.screen != null || trappingBoat(client.player) == null) return;
        if (client.options.keyLeft.matches(key, scancode)) {
            ClientPlayNetworking.send(new AbilityC2SPacket(AbilityC2SPacket.ABILITY_ZHUIMU_STRUGGLE_LEFT, null));
        } else if (client.options.keyRight.matches(key, scancode)) {
            ClientPlayNetworking.send(new AbilityC2SPacket(AbilityC2SPacket.ABILITY_ZHUIMU_STRUGGLE_RIGHT, null));
        }
    }

    private static void render(GuiGraphics context, DeltaTracker tickCounter) {
        Minecraft client = Minecraft.getInstance();
        ZhuimuBoat boat = trappingBoat(client.player);
        if (boat == null || client.options.hideGui) return;
        int required = ModConfig.zhuimuStrugglePresses();
        Component line = Component.translatable(
            "tip.brinswathe.zhuimu.trapped",
            client.options.keyLeft.getTranslatedKeyMessage(),
            boat.leftPresses(),
            required,
            client.options.keyRight.getTranslatedKeyMessage(),
            boat.rightPresses(),
            required
        );
        Font font = client.font;
        int x = (context.guiWidth() - font.width(line)) / 2;
        int y = context.guiHeight() / 2 + 48;
        context.drawString(font, line, x, y, ModRoles.ZHUIMU.color());
    }

    @Nullable
    private static ZhuimuBoat trappingBoat(@Nullable Player player) {
        return player != null && player.getVehicle() instanceof ZhuimuBoat boat ? boat : null;
    }
}
