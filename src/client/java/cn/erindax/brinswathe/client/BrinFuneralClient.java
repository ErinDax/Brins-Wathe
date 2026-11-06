package cn.erindax.brinswathe.client;

import cn.erindax.brinswathe.BrinFuneral;
import cn.erindax.brinswathe.BrinModifiers;
import cn.erindax.brinswathe.network.BrinAbilityC2SPacket;
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
public final class BrinFuneralClient {
    private static boolean useHandled;

    private BrinFuneralClient() {
    }

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!client.options.keyUse.isDown()) useHandled = false;
        });
        HudRenderCallback.EVENT.register(BrinFuneralClient::render);
    }

    public static boolean isCarrying() {
        LocalPlayer player = Minecraft.getInstance().player;
        return player != null && BrinFuneral.isCarrying(player);
    }

    public static boolean handleUse() {
        Minecraft client = Minecraft.getInstance();
        LocalPlayer player = client.player;
        if (player == null) return false;
        if (BrinFuneral.isCarrying(player)) {
            if (player.isShiftKeyDown()) send(BrinAbilityC2SPacket.ABILITY_FUNERAL_PUT_DOWN, null);
            return true;
        }
        if (!player.isShiftKeyDown() || !canPickUp(player)) return false;
        PlayerBodyEntity body = targetedBody(client);
        if (body == null) return false;
        send(BrinAbilityC2SPacket.ABILITY_FUNERAL_PICK_UP, body.getUUID());
        return true;
    }

    private static void send(int action, @Nullable UUID target) {
        if (useHandled) return;
        useHandled = true;
        ClientPlayNetworking.send(new BrinAbilityC2SPacket(action, target));
    }

    private static boolean canPickUp(LocalPlayer player) {
        return GameFunctions.isPlayerAliveAndSurvival(player)
            && BrinModifiers.hasModifier(player, BrinModifiers.FUNERAL);
    }

    @Nullable
    private static PlayerBodyEntity targetedBody(Minecraft client) {
        if (!(client.hitResult instanceof EntityHitResult hit)) return null;
        if (!(hit.getEntity() instanceof PlayerBodyEntity body) || !BrinFuneral.isCarryable(body)) return null;
        return body;
    }

    private static void render(GuiGraphics context, DeltaTracker tickCounter) {
        Minecraft client = Minecraft.getInstance();
        LocalPlayer player = client.player;
        if (player == null || client.options.hideGui) return;
        Component line;
        if (BrinFuneral.isCarrying(player)) {
            line = Component.translatable("tip.brinswathe.funeral.carrying");
        } else if (canPickUp(player) && targetedBody(client) != null) {
            line = Component.translatable("tip.brinswathe.funeral.pick_up");
        } else {
            return;
        }
        Font font = client.font;
        int x = (context.guiWidth() - font.width(line)) / 2;
        int y = context.guiHeight() / 2 + 24;
        context.drawString(font, line, x, y, BrinFuneral.COLOR);
    }
}
