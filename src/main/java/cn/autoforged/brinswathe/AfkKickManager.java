package cn.autoforged.brinswathe;

import cn.autoforged.brinswathe.component.BombComponent;
import cn.autoforged.brinswathe.component.TrapperComponent;
import cn.autoforged.brinswathe.config.BrinConfig;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public final class AfkKickManager {
    private static final double MOVEMENT_EPSILON = 0.0001D;
    private static final float ROTATION_EPSILON = 0.5F;
    private static final Map<UUID, AfkState> STATES = new HashMap<>();

    private AfkKickManager() {
    }

    public static void tick(MinecraftServer server) {
        if (!BrinConfig.afkKickEnabled()) {
            STATES.clear();
            return;
        }

        long now = Util.getMillis();
        Set<UUID> onlinePlayers = new HashSet<>();
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            UUID playerId = player.getUUID();
            onlinePlayers.add(playerId);
            if (!isEligible(player) || isTrapped(server, playerId) || isPinnedByMine(player)) {
                STATES.remove(playerId);
                continue;
            }

            AfkState state = STATES.computeIfAbsent(playerId, ignored -> new AfkState(player, now));
            if (state.hasActivity(player)) {
                if (state.warningStarted) {
                    player.displayClientMessage(
                        Component.translatable("message.brinswathe.afk.cancelled")
                            .withStyle(ChatFormatting.GREEN),
                        true
                    );
                }
                state.recordActivity(player, now);
                continue;
            }

            long idleMillis = now - state.lastActivityMillis;
            long idleThresholdMillis = BrinConfig.afkIdleSeconds() * 1000L;
            if (idleMillis < idleThresholdMillis) continue;

            int countdownSeconds = BrinConfig.afkCountdownSeconds();
            long countdownElapsedMillis = idleMillis - idleThresholdMillis;
            if (countdownElapsedMillis >= countdownSeconds * 1000L) {
                STATES.remove(playerId);
                player.connection.disconnect(Component.translatable("disconnect.brinswathe.afk"));
                continue;
            }

            int remainingSeconds = countdownSeconds - (int) (countdownElapsedMillis / 1000L);
            if (!state.warningStarted) {
                state.warningStarted = true;
                player.sendSystemMessage(Component.translatable(
                    "message.brinswathe.afk.started",
                    countdownSeconds
                ).withStyle(ChatFormatting.RED));
            }
            if (remainingSeconds != state.lastDisplayedSecond) {
                state.lastDisplayedSecond = remainingSeconds;
                player.displayClientMessage(Component.translatable(
                    "message.brinswathe.afk.countdown",
                    remainingSeconds
                ).withStyle(ChatFormatting.RED), true);
            }
        }
        STATES.keySet().retainAll(onlinePlayers);
    }

    public static void reset() {
        STATES.clear();
    }

    private static boolean isEligible(ServerPlayer player) {
        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(player.level());
        return gameWorld.getGameStatus() == GameWorldComponent.GameStatus.ACTIVE
            && GameFunctions.isPlayerAliveAndSurvival(player);
    }

    private static boolean isPinnedByMine(ServerPlayer player) {
        BombComponent bomb = BombComponent.KEY.get(player);
        return bomb != null && bomb.isMinePinned();
    }

    private static boolean isTrapped(MinecraftServer server, UUID playerId) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            TrapperComponent trapper = TrapperComponent.KEY.get(player);
            if (trapper != null && trapper.isTrapped(playerId)) return true;
        }
        return false;
    }

    private static final class AfkState {
        private long lastActivityMillis;
        private long lastVanillaActionMillis;
        private Vec3 position;
        private float yaw;
        private float pitch;
        private boolean warningStarted;
        private int lastDisplayedSecond = -1;

        private AfkState(ServerPlayer player, long now) {
            this.recordActivity(player, now);
        }

        private boolean hasActivity(ServerPlayer player) {
            return player.getLastActionTime() > this.lastVanillaActionMillis
                || player.position().distanceToSqr(this.position) > MOVEMENT_EPSILON
                || Math.abs(Mth.wrapDegrees(player.getYRot() - this.yaw)) > ROTATION_EPSILON
                || Math.abs(player.getXRot() - this.pitch) > ROTATION_EPSILON;
        }

        private void recordActivity(ServerPlayer player, long now) {
            this.lastActivityMillis = now;
            this.lastVanillaActionMillis = player.getLastActionTime();
            this.position = player.position();
            this.yaw = player.getYRot();
            this.pitch = player.getXRot();
            this.warningStarted = false;
            this.lastDisplayedSecond = -1;
        }
    }
}
