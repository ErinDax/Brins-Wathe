package cn.erindax.brinswathe.role;

import cn.erindax.brinswathe.NoelleAccess;
import cn.erindax.brinswathe.config.Tunable;
import cn.erindax.brinswathe.network.AbilityC2SPacket;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.server.level.ServerPlayer;

public final class Noisemaker {
    private Noisemaker() {
    }

    public static void handleMark(ServerPlayer player, GameWorldComponent gameWorld, AbilityC2SPacket payload) {
        if (!NoelleAccess.isRole(gameWorld, player, NoelleAccess.NOISEMAKER_ID)) return;
        if (payload.targetId() == null) return;
        if (NoelleAccess.noelleAbilityCooldown(player) > 0) return;
        ServerPlayer target = player.server.getPlayerList().getPlayer(payload.targetId());
        if (target == null || target.equals(player) || target.level() != player.level()) return;
        if (!GameFunctions.isPlayerAliveAndSurvival(target)) return;
        if (!NoelleAccess.setVoodooTarget(player, target.getUUID())) return;
        NoelleAccess.setNoelleAbilityCooldown(player, Tunable.NOISEMAKER_MARK_COOLDOWN_SECONDS.ticks());
    }
}
