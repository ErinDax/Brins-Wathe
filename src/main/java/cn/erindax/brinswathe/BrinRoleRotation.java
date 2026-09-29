package cn.erindax.brinswathe;

import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.api.WatheRoles;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import org.agmas.harpymodloader.Harpymodloader;
import org.jetbrains.annotations.Nullable;

public final class BrinRoleRotation {
    public static final String KILLER = "camp:killer";
    public static final String VIGILANTE = "camp:vigilante";
    public static final String NEUTRAL = "camp:neutral";
    private static final BrinRoleHistory HISTORY = new BrinRoleHistory();

    private BrinRoleRotation() {
    }

    public static boolean active() {
        return BrinIcFlags.roleRepeatGuard && !BrinIcFlags.roleWeights;
    }

    @Nullable
    public static String categoryFor(@Nullable Role role) {
        if (role == null) return null;
        if (WatheRoles.VIGILANTE.equals(role)) return VIGILANTE;
        if (!role.isInnocent() && !role.canUseKiller()) return NEUTRAL;
        if (Harpymodloader.VANNILA_ROLES.contains(role)) return null;
        return role.identifier().toString();
    }

    public static double weight(ServerPlayer player, @Nullable String category, Collection<ServerPlayer> pool, int slots) {
        if (category == null) return 1.0;
        List<UUID> ids = new ArrayList<>(pool.size());
        for (ServerPlayer candidate : pool) ids.add(candidate.getUUID());
        return HISTORY.weight(
            player.getUUID(),
            category,
            ids,
            slots,
            BrinIcFlags.roleRepeatStrength,
            BrinIcFlags.roleRepeatMaxStreak
        );
    }

    public static void order(List<ServerPlayer> players, @Nullable String category, RandomSource random) {
        if (!active() || category == null) {
            Collections.shuffle(players);
            return;
        }
        List<UUID> ids = new ArrayList<>(players.size());
        for (ServerPlayer player : players) ids.add(player.getUUID());
        List<Integer> order = HISTORY.order(
            ids,
            category,
            BrinIcFlags.roleRepeatStrength,
            BrinIcFlags.roleRepeatMaxStreak,
            random::nextDouble
        );
        List<ServerPlayer> sorted = new ArrayList<>(players.size());
        for (int index : order) sorted.add(players.get(index));
        players.clear();
        players.addAll(sorted);
    }

    public static void recordRound(GameWorldComponent game) {
        Map<UUID, Set<String>> held = new HashMap<>();
        for (Map.Entry<UUID, Role> entry : game.getRoles().entrySet()) {
            Role role = entry.getValue();
            if (role == null) continue;
            Set<String> categories = new HashSet<>(2);
            if (role.canUseKiller()) categories.add(KILLER);
            String category = categoryFor(role);
            if (category != null) categories.add(category);
            if (!categories.isEmpty()) held.put(entry.getKey(), categories);
        }
        HISTORY.record(held, BrinIcFlags.roleRepeatDecay);
    }

    public static void clear() {
        HISTORY.clear();
    }
}
