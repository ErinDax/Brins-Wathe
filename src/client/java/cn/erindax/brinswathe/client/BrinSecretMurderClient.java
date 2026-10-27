package cn.erindax.brinswathe.client;

import cn.erindax.brinswathe.BrinSecretMurder;
import cn.erindax.brinswathe.network.BrinSecretCohortS2CPacket;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

@Environment(EnvType.CLIENT)
public final class BrinSecretMurderClient {
    public static final int COHORT_COLOR = 0xDD0000;
    public static final int OTHER_COLOR = 0x4EDD35;
    private static final Set<UUID> COHORTS = new HashSet<>();

    private BrinSecretMurderClient() {
    }

    public static void init() {
        ClientPlayNetworking.registerGlobalReceiver(BrinSecretCohortS2CPacket.TYPE, (payload, context) ->
            context.client().execute(() -> {
                COHORTS.clear();
                COHORTS.addAll(payload.cohorts());
            }));
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> client.execute(COHORTS::clear));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (COHORTS.isEmpty() || client.level == null) return;
            if (!isRound(GameWorldComponent.KEY.get(client.level))) COHORTS.clear();
        });
    }

    public static boolean isRound(GameWorldComponent game) {
        return game != null && game.isRunning() && BrinSecretMurder.isActive(game);
    }

    public static boolean isLocalSecretKiller() {
        Player player = Minecraft.getInstance().player;
        if (player == null) return false;
        GameWorldComponent game = GameWorldComponent.KEY.get(player.level());
        return isRound(game)
            && game.isRole(player, BrinSecretMurder.ROLE)
            && GameFunctions.isPlayerAliveAndSurvival(player);
    }

    public static boolean hidesPoisonOf(Level world, String poisoner) {
        Player player = Minecraft.getInstance().player;
        if (poisoner == null || player == null || world == null) return false;
        if (!isRound(GameWorldComponent.KEY.get(world))) return false;
        return !poisoner.equalsIgnoreCase(player.getStringUUID());
    }

    public static boolean isCohort(UUID playerId) {
        return playerId != null && COHORTS.contains(playerId);
    }

    public static int highlight(UUID playerId) {
        return isCohort(playerId) ? COHORT_COLOR : OTHER_COLOR;
    }
}
