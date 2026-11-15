package cn.erindax.brinswathe.role;

import cn.erindax.brinswathe.AbilityCost;
import cn.erindax.brinswathe.ModRoles;
import cn.erindax.brinswathe.component.BombComponent;
import cn.erindax.brinswathe.component.TrapperComponent;
import cn.erindax.brinswathe.config.ModConfig;
import cn.erindax.brinswathe.network.AbilityC2SPacket;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import org.BsXinQin.kinswathe.component.AbilityPlayerComponent;

public final class Watchman {
    private Watchman() {
    }

    public static void handleRescue(ServerPlayer player, GameWorldComponent gameWorld,
                                    AbilityPlayerComponent ability,
                                    AbilityC2SPacket payload) {
        if (!gameWorld.isRole(player, ModRoles.WATCHMAN)) return;
        if (ability.cooldown > 0) return;
        ServerPlayer target = null;
        if (payload.targetId() != null) {
            ServerPlayer aimed = player.server.getPlayerList().getPlayer(payload.targetId());
            if (aimed != null && aimed.level() == player.level() && WatchmanRescue.canRescue(aimed)) target = aimed;
        }
        if (target == null && WatchmanRescue.canRescue(player)) target = player;
        if (target == null) return;
        TrapperComponent trappedBy = WatchmanRescue.trapperOf(target);
        BombComponent bomb = BombComponent.KEY.get(target);
        boolean hasBombToDefuse = WatchmanRescue.hasBombToDefuse(target);
        boolean leftoverTrapStun = trappedBy == null && WatchmanRescue.hasLeftoverStun(target);
        if (trappedBy == null && !hasBombToDefuse && !leftoverTrapStun) return;
        if (!AbilityCost.spend(player, "watchman")) return;
        boolean rescued = trappedBy != null && trappedBy.rescuePlayer(target.getUUID());
        if (hasBombToDefuse) bomb.reset();
        if (leftoverTrapStun) BomberBombs.releaseStun(target);
        if (!rescued && !hasBombToDefuse && !leftoverTrapStun) return;
        ability.setAbilityCooldown(ModConfig.skillCooldownSeconds("watchman"));
        player.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 1.0F, 1.25F);
    }
}
