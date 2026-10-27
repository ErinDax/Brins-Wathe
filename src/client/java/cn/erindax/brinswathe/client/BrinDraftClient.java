package cn.erindax.brinswathe.client;

import cn.erindax.brinswathe.client.gui.BrinDraftScreen;
import cn.erindax.brinswathe.network.BrinDraftCloseS2CPacket;
import cn.erindax.brinswathe.network.BrinDraftStateS2CPacket;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;

@Environment(EnvType.CLIENT)
public final class BrinDraftClient {
    private BrinDraftClient() {
    }

    public static void init() {
        ClientPlayNetworking.registerGlobalReceiver(BrinDraftStateS2CPacket.TYPE, (payload, context) ->
            context.client().execute(() -> state(context.client(), payload)));
        ClientPlayNetworking.registerGlobalReceiver(BrinDraftCloseS2CPacket.TYPE, (payload, context) ->
            context.client().execute(() -> close(context.client())));
    }

    private static void state(Minecraft client, BrinDraftStateS2CPacket payload) {
        if (client.screen instanceof BrinDraftScreen screen) {
            screen.update(payload);
            return;
        }
        if (client.player == null) return;
        if (payload.open() || !payload.offers().isEmpty()) client.setScreen(new BrinDraftScreen(payload));
    }

    private static void close(Minecraft client) {
        if (client.screen instanceof BrinDraftScreen screen) screen.finish();
    }
}
