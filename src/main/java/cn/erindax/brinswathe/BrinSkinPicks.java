package cn.erindax.brinswathe;

import cn.erindax.brinswathe.network.BrinSkinPickChoiceC2SPacket;
import cn.erindax.brinswathe.network.BrinSkinPickOpenS2CPacket;
import cn.erindax.brinswathe.network.BrinSkinPickResultS2CPacket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;

public final class BrinSkinPicks {
    public static final int ACTION_ACCEPT = 0;
    public static final int ACTION_DRAW = 1;
    public static final int ACTION_CANCEL = 2;
    public static final int RESULT_ACCEPTED = 0;
    public static final int RESULT_DRAWN = 1;
    public static final int RESULT_UNAVAILABLE = 2;
    private static final Map<UUID, Pick> PICKS = new HashMap<>();
    private static int nextToken = 1;

    private BrinSkinPicks() {
    }

    public static void init() {
        ServerLifecycleEvents.SERVER_STARTING.register(server -> PICKS.clear());
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> PICKS.remove(handler.player.getUUID()));
        ServerPlayNetworking.registerGlobalReceiver(
            BrinSkinPickChoiceC2SPacket.TYPE,
            (payload, context) -> choose(context.player(), payload.token(), payload.action())
        );
    }

    public static void send(ServerPlayer sender, ServerPlayer target, String type, String skin) {
        Pick pick = new Pick(
            nextToken++,
            BrinSkinEditors.normalizeType(type),
            skin,
            sender.getUUID(),
            sender.getGameProfile().getName()
        );
        PICKS.put(target.getUUID(), pick);
        ServerPlayNetworking.send(target, new BrinSkinPickOpenS2CPacket(
            pick.token(),
            pick.type(),
            pick.skin(),
            pick.senderName(),
            BrinKnifeSkins.drawPool(pick.type()).size()
        ));
    }

    public static MutableComponent typeLabel(String type) {
        return Component.translatable("gui.brinswathe.skin_pick.type." + BrinSkinEditors.normalizeType(type));
    }

    private static void choose(ServerPlayer player, int token, int action) {
        Pick pick = PICKS.get(player.getUUID());
        boolean valid = pick != null && pick.token() == token;
        if (action == ACTION_CANCEL) {
            if (!valid) return;
            PICKS.remove(player.getUUID());
            notifySender(player, pick, Component.translatable(
                "message.brinswathe.skin_pick.sender_cancelled",
                player.getGameProfile().getName()
            ));
            return;
        }
        if (!valid || (action != ACTION_ACCEPT && action != ACTION_DRAW)) {
            ServerPlayNetworking.send(player, new BrinSkinPickResultS2CPacket(token, RESULT_UNAVAILABLE, "", "", 0));
            return;
        }
        PICKS.remove(player.getUUID());
        boolean drawn = action == ACTION_DRAW;
        String skin;
        if (drawn) {
            List<String> pool = BrinKnifeSkins.drawPool(pick.type());
            skin = pool.isEmpty() ? null : pool.get(player.getRandom().nextInt(pool.size()));
        } else {
            skin = BrinKnifeSkins.resolveSkin(pick.type(), pick.skin());
        }
        if (skin == null) {
            ServerPlayNetworking.send(player, new BrinSkinPickResultS2CPacket(token, RESULT_UNAVAILABLE, pick.type(), "", 0));
            return;
        }
        int applied = BrinKnifeSkins.applyHeldSkin(player, pick.type(), skin);
        ServerPlayNetworking.send(player, new BrinSkinPickResultS2CPacket(
            token,
            drawn ? RESULT_DRAWN : RESULT_ACCEPTED,
            pick.type(),
            skin,
            applied
        ));
        String name = BrinKnifeSkins.displayName(pick.type(), skin);
        if (!drawn) player.sendSystemMessage(appliedMessage(pick.type(), name, applied, false));
        notifySender(player, pick, Component.translatable(
            drawn ? "message.brinswathe.skin_pick.sender_drawn" : "message.brinswathe.skin_pick.sender_accepted",
            player.getGameProfile().getName(),
            name,
            applied
        ));
    }

    public static MutableComponent appliedMessage(String type, String name, int applied, boolean drawn) {
        if (applied <= 0) return nothingStatus(type, name);
        String kind = "gun".equalsIgnoreCase(type) ? "gun" : "knife";
        return Component.translatable(
            (drawn ? "message.brinswathe.skin_pick.drawn." : "message.brinswathe.skin_pick.accepted.") + kind,
            name
        ).withStyle(ChatFormatting.GREEN);
    }

    public static MutableComponent nothingStatus(String type, String name) {
        String kind = "gun".equalsIgnoreCase(type) ? "gun" : "knife";
        return Component.translatable("message.brinswathe.skin_pick.nothing." + kind, name)
            .withStyle("gun".equals(kind) ? ChatFormatting.YELLOW : ChatFormatting.GREEN);
    }

    private static void notifySender(ServerPlayer player, Pick pick, MutableComponent message) {
        ServerPlayer sender = player.server.getPlayerList().getPlayer(pick.senderId());
        if (sender != null) sender.sendSystemMessage(message.withStyle(ChatFormatting.GOLD));
    }

    private record Pick(int token, String type, String skin, UUID senderId, String senderName) {
    }
}
