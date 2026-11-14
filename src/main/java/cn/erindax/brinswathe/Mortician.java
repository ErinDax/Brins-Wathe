package cn.erindax.brinswathe;

import cn.erindax.brinswathe.component.MorticianComponent;
import cn.erindax.brinswathe.network.AbilityC2SPacket;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import net.minecraft.server.level.ServerPlayer;
import org.BsXinQin.kinswathe.component.AbilityPlayerComponent;

public final class Mortician {
    private Mortician() {
    }

    public static void handleDisguise(ServerPlayer player, GameWorldComponent gameWorld,
                                      AbilityPlayerComponent ability,
                                      AbilityC2SPacket payload) {
        if (!gameWorld.isRole(player, ModRoles.MORTICIAN)) return;
        if (ability.cooldown > 0) return;
        if (payload.targetId() == null) return;

        MorticianComponent comp = MorticianComponent.KEY.get(player);
        if (comp == null || comp.isDisguised()) return;
        if (!player.onGround()) return;

        ServerPlayer target = player.server.getPlayerList().getPlayer(payload.targetId());
        if (target == null) return;
        if (!AbilityCost.spend(player, "mortician")) return;
        comp.startDisguise(player, target.getUUID());
    }
}
