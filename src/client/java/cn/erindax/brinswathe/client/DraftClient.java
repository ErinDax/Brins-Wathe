package cn.erindax.brinswathe.client;

import cn.erindax.brinswathe.client.gui.DraftScreen;
import cn.erindax.brinswathe.network.DraftCloseS2CPacket;
import cn.erindax.brinswathe.network.DraftStateS2CPacket;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;

@Environment(EnvType.CLIENT)
public final class DraftClient {
    private DraftClient() {
    }

    public static void init() {
        ClientPlayNetworking.registerGlobalReceiver(DraftStateS2CPacket.TYPE, (payload, context) ->
            context.client().execute(() -> state(context.client(), payload)));
        ClientPlayNetworking.registerGlobalReceiver(DraftCloseS2CPacket.TYPE, (payload, context) ->
            context.client().execute(() -> close(context.client())));
    }

    private static void state(Minecraft client, DraftStateS2CPacket payload) {
        if (client.screen instanceof DraftScreen screen) {
            screen.update(payload);
            return;
        }
        if (client.player == null) return;
        if (payload.open() || !payload.offers().isEmpty()) client.setScreen(new DraftScreen(payload));
    }

    private static void close(Minecraft client) {
        if (client.screen instanceof DraftScreen screen) screen.finish();
    }
}
