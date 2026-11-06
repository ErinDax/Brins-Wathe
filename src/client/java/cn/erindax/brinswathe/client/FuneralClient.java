package cn.erindax.brinswathe.client;

import cn.erindax.brinswathe.Funeral;
import cn.erindax.brinswathe.ModModifiers;
import cn.erindax.brinswathe.network.AbilityC2SPacket;
import dev.doctor4t.wathe.entity.PlayerBodyEntity;
import dev.doctor4t.wathe.game.GameFunctions;
import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.EntityHitResult;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public final class FuneralClient {
    private static boolean useHandled;

    private FuneralClient() {
    }

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!client.options.keyUse.isDown()) useHandled = false;
        });
        HudRenderCallback.EVENT.register(FuneralClient::render);
    }

    public static boolean isCarrying() {
        LocalPlayer player = Minecraft.getInstance().player;
        return player != null && Funeral.isCarrying(player);
    }

    public static boolean handleUse() {
        Minecraft client = Minecraft.getInstance();
        LocalPlayer player = client.player;
        if (player == null) return false;
        if (Funeral.isCarrying(player)) {
            if (player.isShiftKeyDown()) send(AbilityC2SPacket.ABILITY_FUNERAL_PUT_DOWN, null);
            return true;
        }
        if (!player.isShiftKeyDown() || !canPickUp(player)) return false;
        PlayerBodyEntity body = targetedBody(client);
        if (body == null) return false;
        send(AbilityC2SPacket.ABILITY_FUNERAL_PICK_UP, body.getUUID());
        return true;
    }

    private static void send(int action, @Nullable UUID target) {
        if (useHandled) return;
        useHandled = true;
        ClientPlayNetworking.send(new AbilityC2SPacket(action, target));
    }

    private static boolean canPickUp(LocalPlayer player) {
        return GameFunctions.isPlayerAliveAndSurvival(player)
            && ModModifiers.hasModifier(player, ModModifiers.FUNERAL);
    }

    @Nullable
    private static PlayerBodyEntity targetedBody(Minecraft client) {
        if (!(client.hitResult instanceof EntityHitResult hit)) return null;
        if (!(hit.getEntity() instanceof PlayerBodyEntity body) || !Funeral.isCarryable(body)) return null;
        return body;
    }

    private static void render(GuiGraphics context, DeltaTracker tickCounter) {
        Minecraft client = Minecraft.getInstance();
        LocalPlayer player = client.player;
        if (player == null || client.options.hideGui) return;
        Component line;
        if (Funeral.isCarrying(player)) {
            line = Component.translatable("tip.brinswathe.funeral.carrying");
        } else if (canPickUp(player) && targetedBody(client) != null) {
            line = Component.translatable("tip.brinswathe.funeral.pick_up");
        } else {
            return;
        }
        Font font = client.font;
        int x = (context.guiWidth() - font.width(line)) / 2;
        int y = context.guiHeight() / 2 + 24;
        context.drawString(font, line, x, y, Funeral.COLOR);
    }
}
