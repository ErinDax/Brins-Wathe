package cn.erindax.brinswathe;

import cn.erindax.brinswathe.network.BrinInstinctSnapshotS2CPacket;
import cn.erindax.brinswathe.network.BrinInstinctSnapshotS2CPacket.Entry;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.PlayerMoodComponent;
import dev.doctor4t.wathe.cca.PlayerPsychoComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public final class BrinInstinctSnapshots {
    private static final Set<UUID> ENABLED = ConcurrentHashMap.newKeySet();

    private BrinInstinctSnapshots() {
    }

    public static void setEnabled(UUID playerId, boolean enabled) {
        if (enabled) ENABLED.add(playerId);
        else ENABLED.remove(playerId);
    }

    public static void remove(UUID playerId) {
        ENABLED.remove(playerId);
    }

    public static void tick(MinecraftServer server) {
        if (server.getTickCount() % 10 != 0) return;
        if (ENABLED.isEmpty()) return;

        List<Entry> entries = new ArrayList<>();
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (GameFunctions.isPlayerSpectatingOrCreative(player)) continue;
            if (player.isInvisible()) continue;
            GameWorldComponent game = GameWorldComponent.KEY.get(player.level());
            PlayerMoodComponent mood = PlayerMoodComponent.KEY.get(player);
            PlayerPsychoComponent psycho = PlayerPsychoComponent.KEY.get(player);
            entries.add(new Entry(
                player.getUUID(),
                player.getDisplayName().getString(),
                player.getX(),
                player.getY(),
                player.getZ(),
                player.yBodyRot,
                mood == null ? 0.0F : mood.getMood(),
                false,
                game.canUseKillerFeatures(player),
                game.isInnocent(player),
                psycho != null && psycho.getPsychoTicks() > 0
            ));
        }

        for (ServerPlayer recipient : server.getPlayerList().getPlayers()) {
            if (!ENABLED.contains(recipient.getUUID())) continue;
            List<Entry> filtered = new ArrayList<>(entries.size());
            for (Entry entry : entries) {
                if (!entry.uuid().equals(recipient.getUUID())) filtered.add(entry);
            }
            ServerPlayNetworking.send(recipient, new BrinInstinctSnapshotS2CPacket(filtered));
        }
    }
}
