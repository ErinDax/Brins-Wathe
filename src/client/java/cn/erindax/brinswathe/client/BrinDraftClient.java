package cn.erindax.brinswathe.client;

import cn.erindax.brinswathe.client.gui.BrinDraftScreen;
import cn.erindax.brinswathe.network.BrinDraftCloseS2CPacket;
import cn.erindax.brinswathe.network.BrinDraftOpenS2CPacket;
import cn.erindax.brinswathe.network.BrinDraftProgressS2CPacket;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;

@Environment(EnvType.CLIENT)
public final class BrinDraftClient {
    private BrinDraftClient() {
    }

    public static void init() {
        ClientPlayNetworking.registerGlobalReceiver(BrinDraftOpenS2CPacket.TYPE, (payload, context) ->
            context.client().execute(() -> open(context.client(), payload)));
        ClientPlayNetworking.registerGlobalReceiver(BrinDraftCloseS2CPacket.TYPE, (payload, context) ->
            context.client().execute(() -> close(context.client())));
        ClientPlayNetworking.registerGlobalReceiver(BrinDraftProgressS2CPacket.TYPE, (payload, context) ->
            context.client().execute(() -> progress(context.client(), payload)));
    }

    private static void progress(Minecraft client, BrinDraftProgressS2CPacket payload) {
        if (client.screen instanceof BrinDraftScreen screen) screen.updateProgress(payload.decided(), payload.total());
    }

    private static void open(Minecraft client, BrinDraftOpenS2CPacket payload) {
        if (client.player == null || payload.roleIds().isEmpty()) return;
        client.setScreen(new BrinDraftScreen(payload.roleIds(), payload.seconds()));
    }

    private static void close(Minecraft client) {
        if (client.screen instanceof BrinDraftScreen screen) screen.finish();
    }
}
