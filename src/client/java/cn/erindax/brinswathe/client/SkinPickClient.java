package cn.erindax.brinswathe.client;

import cn.erindax.brinswathe.KnifeSkins;
import cn.erindax.brinswathe.SkinPicks;
import cn.erindax.brinswathe.client.gui.SkinPickScreen;
import cn.erindax.brinswathe.network.SkinPickOpenS2CPacket;
import cn.erindax.brinswathe.network.SkinPickResultS2CPacket;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public final class SkinPickClient {
    private SkinPickClient() {
    }

    public static void init() {
        ClientPlayNetworking.registerGlobalReceiver(SkinPickOpenS2CPacket.TYPE, (payload, context) ->
            context.client().execute(() -> open(context.client(), payload)));
        ClientPlayNetworking.registerGlobalReceiver(SkinPickResultS2CPacket.TYPE, (payload, context) ->
            context.client().execute(() -> result(context.client(), payload)));
    }

    private static void open(Minecraft client, SkinPickOpenS2CPacket payload) {
        if (client.player == null) return;
        client.setScreen(new SkinPickScreen(payload));
    }

    private static void result(Minecraft client, SkinPickResultS2CPacket payload) {
        if (client.screen instanceof SkinPickScreen screen && screen.token() == payload.token()) {
            screen.onResult(payload.status(), payload.skin(), payload.applied());
            return;
        }
        if (client.player == null) return;
        if (payload.status() == SkinPicks.RESULT_DRAWN) {
            client.player.displayClientMessage(SkinPicks.appliedMessage(
                payload.kind(),
                KnifeSkins.displayName(payload.kind(), payload.skin()),
                payload.applied(),
                true
            ), false);
        } else if (payload.status() == SkinPicks.RESULT_UNAVAILABLE) {
            client.player.displayClientMessage(
                Component.translatable("message.brinswathe.skin_pick.unavailable").withStyle(ChatFormatting.RED),
                false
            );
        }
    }
}
