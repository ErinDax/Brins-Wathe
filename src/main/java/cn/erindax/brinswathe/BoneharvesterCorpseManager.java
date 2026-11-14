package cn.erindax.brinswathe;

import cn.erindax.brinswathe.config.Tunable;
import dev.doctor4t.wathe.entity.PlayerBodyEntity;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;

public final class BoneharvesterCorpseManager {
    public static final Map<UUID, Integer> delayedCorpseAppearances = new ConcurrentHashMap<>();

    public static void markCorpseDelayed(Player victim) {
        int delayTicks = Tunable.BONEHARVESTER_CORPSE_DELAY_SECONDS.ticks();
        if (delayTicks <= 0) return;
        UUID playerUuid = victim.getUUID();
        delayedCorpseAppearances.put(playerUuid, delayTicks);

        if (!(victim.level() instanceof ServerLevel serverLevel)) return;
        for (var entity : serverLevel.getAllEntities()) {
            if (entity instanceof PlayerBodyEntity body && playerUuid.equals(body.getPlayerUuid())) {
                body.setInvisible(true);
                return;
            }
        }
    }

    private BoneharvesterCorpseManager() {
    }
}
