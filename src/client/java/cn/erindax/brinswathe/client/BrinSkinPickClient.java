package cn.erindax.brinswathe.client;

import cn.erindax.brinswathe.BrinKnifeSkins;
import cn.erindax.brinswathe.BrinSkinPicks;
import cn.erindax.brinswathe.client.gui.BrinSkinPickScreen;
import cn.erindax.brinswathe.network.BrinSkinPickOpenS2CPacket;
import cn.erindax.brinswathe.network.BrinSkinPickResultS2CPacket;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public final class BrinSkinPickClient {
    private BrinSkinPickClient() {
    }

    public static void init() {
        ClientPlayNetworking.registerGlobalReceiver(BrinSkinPickOpenS2CPacket.TYPE, (payload, context) ->
            context.client().execute(() -> open(context.client(), payload)));
        ClientPlayNetworking.registerGlobalReceiver(BrinSkinPickResultS2CPacket.TYPE, (payload, context) ->
            context.client().execute(() -> result(context.client(), payload)));
    }

    private static void open(Minecraft client, BrinSkinPickOpenS2CPacket payload) {
        if (client.player == null) return;
        client.setScreen(new BrinSkinPickScreen(payload));
    }

    private static void result(Minecraft client, BrinSkinPickResultS2CPacket payload) {
        if (client.screen instanceof BrinSkinPickScreen screen && screen.token() == payload.token()) {
            screen.onResult(payload.status(), payload.skin(), payload.applied());
            return;
        }
        if (client.player == null) return;
        if (payload.status() == BrinSkinPicks.RESULT_DRAWN) {
            client.player.displayClientMessage(BrinSkinPicks.appliedMessage(
                payload.kind(),
                BrinKnifeSkins.displayName(payload.kind(), payload.skin()),
                payload.applied(),
                true
            ), false);
        } else if (payload.status() == BrinSkinPicks.RESULT_UNAVAILABLE) {
            client.player.displayClientMessage(
                Component.translatable("message.brinswathe.skin_pick.unavailable").withStyle(ChatFormatting.RED),
                false
            );
        }
    }
}
