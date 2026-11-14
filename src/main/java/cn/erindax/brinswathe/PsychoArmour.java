package cn.erindax.brinswathe;

import cn.erindax.brinswathe.config.Tunable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public final class PsychoArmour {
    private PsychoArmour() {
    }

    public static int calculate(Player player) {
        int nearby = nearbyPlayers(player);
        int minPlayers = IcFlags.psychoMinPlayersForExtraArmour;
        int perShield = Math.max(1, IcFlags.psychoPlayersPerExtraArmour);
        if (nearby < minPlayers) return 1;
        return 1 + (nearby - minPlayers) / perShield;
    }

    private static int nearbyPlayers(Player player) {
        if (!(player.level() instanceof ServerLevel level)) return 1;
        int count = 0;
        double range = Tunable.PSYCHO_ARMOUR_RANGE.doubleValue();
        for (ServerPlayer other : level.players()) {
            if (other == player || player.position().distanceTo(other.position()) <= range) {
                count++;
            }
        }
        return count;
    }
}
