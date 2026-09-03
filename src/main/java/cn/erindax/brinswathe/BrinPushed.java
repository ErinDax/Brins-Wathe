package cn.erindax.brinswathe;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.world.entity.player.Player;

public final class BrinPushed {
    private static final Set<UUID> THIS_TICK = new HashSet<>();
    private static final Set<UUID> LAST_TICK = new HashSet<>();
    private BrinPushed() {
    }
    public static void beginTick() {
        LAST_TICK.clear();
        LAST_TICK.addAll(THIS_TICK);
        THIS_TICK.clear();
    }
    public static void mark(Player player) {
        THIS_TICK.add(player.getUUID());
    }

    public static boolean wasPushed(Player player) {
        UUID id = player.getUUID();
        return THIS_TICK.contains(id) || LAST_TICK.contains(id);
    }
}
