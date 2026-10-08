package cn.erindax.brinswathe.client;

import cn.erindax.brinswathe.client.gui.DraftScreen;
import cn.erindax.brinswathe.network.DraftCloseS2CPacket;
import cn.erindax.brinswathe.network.DraftOpenS2CPacket;
import cn.erindax.brinswathe.network.DraftProgressS2CPacket;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;

@Environment(EnvType.CLIENT)
public final class DraftClient {
    private DraftClient() {
    }

    public static void init() {
        ClientPlayNetworking.registerGlobalReceiver(DraftOpenS2CPacket.TYPE, (payload, context) ->
            context.client().execute(() -> open(context.client(), payload)));
        ClientPlayNetworking.registerGlobalReceiver(DraftCloseS2CPacket.TYPE, (payload, context) ->
            context.client().execute(() -> close(context.client())));
        ClientPlayNetworking.registerGlobalReceiver(DraftProgressS2CPacket.TYPE, (payload, context) ->
            context.client().execute(() -> progress(context.client(), payload)));
    }

    private static void progress(Minecraft client, DraftProgressS2CPacket payload) {
        if (client.screen instanceof DraftScreen screen) screen.updateProgress(payload.decided(), payload.total());
    }

    private static void open(Minecraft client, DraftOpenS2CPacket payload) {
        if (client.player == null || payload.roleIds().isEmpty()) return;
        client.setScreen(new DraftScreen(payload.roleIds(), payload.seconds()));
    }

    private static void close(Minecraft client) {
        if (client.screen instanceof DraftScreen screen) screen.finish();
    }
}
