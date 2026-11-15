package cn.erindax.brinswathe.role;

import cn.erindax.brinswathe.ModRoles;
import cn.erindax.brinswathe.component.StalkerComponent;
import cn.erindax.brinswathe.network.AbilityC2SPacket;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import net.minecraft.server.level.ServerPlayer;

public final class Stalker {
    private Stalker() {
    }

    public static void handleTrack(ServerPlayer player, GameWorldComponent gameWorld,
                                   AbilityC2SPacket payload) {
        if (!gameWorld.isRole(player, ModRoles.STALKER)) return;
        if (payload.targetId() == null) return;
        ServerPlayer target = player.server.getPlayerList().getPlayer(payload.targetId());
        if (target == null || target.level() != player.level()) return;
        StalkerComponent.tryTrack(player, target);
    }
}
