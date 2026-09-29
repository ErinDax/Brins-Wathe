package cn.erindax.brinswathe.musicbox;

import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public final class BrinRoundStats {
    private static final Tally EMPTY = new Tally();
    private static final Map<UUID, Tally> TALLIES = new HashMap<>();

    private BrinRoundStats() {
    }

    @Nullable
    public static KillContext beforeKill(Player victim, @Nullable Player killer) {
        if (victim == null || killer == null || killer == victim) return null;
        if (victim.level().isClientSide) return null;
        if (killer.getUUID().equals(victim.getUUID())) return null;
        GameWorldComponent game = GameWorldComponent.KEY.get(victim.level());
        if (game == null || game.getGameStatus() != GameWorldComponent.GameStatus.ACTIVE) return null;
        if (!GameFunctions.isPlayerAliveAndSurvival(victim)) return null;
        Role role = game.getRole(victim);
        if (role == null) return null;
        return new KillContext(killer.getUUID(), role.isInnocent(), role.canUseKiller());
    }

    public static void afterKill(Player victim, @Nullable KillContext context) {
        if (context == null) return;
        if (GameFunctions.isPlayerAliveAndSurvival(victim)) return;
        Tally tally = TALLIES.computeIfAbsent(context.killer(), id -> new Tally());
        tally.kills++;
        if (!context.victimInnocent()) tally.nonInnocentKills++;
        if (!context.victimKiller()) tally.nonKillerKills++;
    }

    public static void recordTask(Player player) {
        if (player == null || player.level().isClientSide) return;
        GameWorldComponent game = GameWorldComponent.KEY.get(player.level());
        if (game == null || game.getGameStatus() != GameWorldComponent.GameStatus.ACTIVE) return;
        TALLIES.computeIfAbsent(player.getUUID(), id -> new Tally()).tasks++;
    }

    public static Tally get(UUID id) {
        Tally tally = TALLIES.get(id);
        return tally == null ? EMPTY : tally;
    }

    public static void reset() {
        TALLIES.clear();
    }

    public record KillContext(UUID killer, boolean victimInnocent, boolean victimKiller) {
    }

    public static final class Tally {
        private int kills;
        private int nonInnocentKills;
        private int nonKillerKills;
        private int tasks;

        public int kills() {
            return this.kills;
        }

        public int nonInnocentKills() {
            return this.nonInnocentKills;
        }

        public int nonKillerKills() {
            return this.nonKillerKills;
        }

        public int tasks() {
            return this.tasks;
        }
    }
}
