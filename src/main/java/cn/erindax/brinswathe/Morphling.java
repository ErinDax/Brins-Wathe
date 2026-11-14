package cn.erindax.brinswathe;

import cn.erindax.brinswathe.network.AbilityC2SPacket;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public final class Morphling {
    private Morphling() {
    }

    public static void handleMorph(ServerPlayer player, GameWorldComponent gameWorld, AbilityC2SPacket payload) {
        if (!NoelleAccess.isRole(gameWorld, player, NoelleAccess.MORPHLING_ID)) return;
        if (payload.targetId() == null) return;
        if (MorphlingAccess.morphTicks(player) != 0) return;
        ServerPlayer target = player.server.getPlayerList().getPlayer(payload.targetId());
        if (target != null && GameFunctions.isPlayerAliveAndSurvival(target)) {
            player.displayClientMessage(Component.literal("你要变身的对象还没死哦"), false);
            return;
        }
        var role = gameWorld.getRole(payload.targetId());
        if (role == null || !role.canUseKiller()) return;
        MorphlingAccess.startMorph(player, payload.targetId());
    }
}
