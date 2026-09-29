package cn.erindax.brinswathe.musicbox;

import cn.erindax.brinswathe.BrinsWathe;
import cn.erindax.brinswathe.component.BrinCustomWinnerComponent;
import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.cca.GameRoundEndComponent;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import org.BsXinQin.kinswathe.component.CustomWinnerComponent;
import org.jetbrains.annotations.Nullable;
import org.ladysnake.cca.api.v3.component.ComponentKey;

public final class BrinMvp {
    private static final String STUPID_EXPRESS_ID = "stupid_express";
    private static final String STUPID_EXPRESS_WINNER_CLASS = "pro.fazeclan.river.stupid_express.cca.CustomWinnerComponent";
    private static boolean stupidExpressResolved;
    private static ComponentKey<?> stupidExpressKey;
    private static Method stupidExpressHasWinner;
    private static Method stupidExpressWinners;

    private BrinMvp() {
    }

    @Nullable
    public static Result resolve(
        ServerLevel level,
        List<ServerPlayer> players,
        @Nullable GameFunctions.WinStatus status,
        @Nullable Set<UUID> soloWinners
    ) {
        GameWorldComponent game = GameWorldComponent.KEY.get(level);
        Map<UUID, ServerPlayer> participants = new LinkedHashMap<>();
        for (ServerPlayer player : players) {
            if (player != null && game.getRole(player) != null) participants.put(player.getUUID(), player);
        }
        if (participants.isEmpty()) return null;
        RandomSource random = level.getRandom();
        if (soloWinners != null) {
            return best(participants, soloWinners::contains, BrinRoundStats.Tally::kills, game, random);
        }
        if (status == null) return null;
        GameRoundEndComponent roundEnd = GameRoundEndComponent.KEY.get(level);
        return switch (status) {
            case LOOSE_END -> {
                UUID winnerId = game.getLooseEndWinner();
                ServerPlayer winner = winnerId == null ? null : participants.get(winnerId);
                yield winner == null ? null : result(winner, game);
            }
            case KILLERS -> bestWinner(participants, id -> {
                Role role = game.getRole(id);
                return role != null && role.canUseKiller();
            }, BrinRoundStats.Tally::nonKillerKills, game, roundEnd, random);
            case PASSENGERS, TIME -> bestWinner(participants, id -> {
                Role role = game.getRole(id);
                return role != null && role.isInnocent();
            }, BrinRoundStats.Tally::nonInnocentKills, game, roundEnd, random);
            default -> null;
        };
    }

    @Nullable
    private static Result bestWinner(
        Map<UUID, ServerPlayer> participants,
        Predicate<UUID> side,
        ToIntFunction<BrinRoundStats.Tally> score,
        GameWorldComponent game,
        GameRoundEndComponent roundEnd,
        RandomSource random
    ) {
        Result winner = best(participants, id -> side.test(id) && roundEnd.didWin(id), score, game, random);
        return winner != null ? winner : best(participants, side, score, game, random);
    }

    @Nullable
    private static Result best(
        Map<UUID, ServerPlayer> participants,
        Predicate<UUID> filter,
        ToIntFunction<BrinRoundStats.Tally> score,
        GameWorldComponent game,
        RandomSource random
    ) {
        List<ServerPlayer> top = new ArrayList<>();
        int bestScore = Integer.MIN_VALUE;
        int bestTasks = Integer.MIN_VALUE;
        for (ServerPlayer player : participants.values()) {
            if (!filter.test(player.getUUID())) continue;
            BrinRoundStats.Tally tally = BrinRoundStats.get(player.getUUID());
            int value = score.applyAsInt(tally);
            int tasks = tally.tasks();
            if (value > bestScore || (value == bestScore && tasks > bestTasks)) {
                top.clear();
                top.add(player);
                bestScore = value;
                bestTasks = tasks;
            } else if (value == bestScore && tasks == bestTasks) {
                top.add(player);
            }
        }
        if (top.isEmpty()) return null;
        return result(top.get(random.nextInt(top.size())), game);
    }

    private static Result result(ServerPlayer player, GameWorldComponent game) {
        Role role = game.getRole(player);
        return new Result(
            player.getUUID(),
            player.getGameProfile().getName(),
            role == null ? "" : role.identifier().toString()
        );
    }

    @Nullable
    public static Set<UUID> customWinners(ServerLevel level) {
        BrinCustomWinnerComponent brin = BrinCustomWinnerComponent.KEY.getNullable(level);
        if (brin != null && brin.hasCustomWinner()) return new HashSet<>(brin.winnerIds());
        CustomWinnerComponent kins = CustomWinnerComponent.KEY.getNullable(level);
        if (kins != null && kins.hasCustomWinner()) {
            Set<UUID> ids = new HashSet<>();
            List<ServerPlayer> winners = kins.getWinners();
            if (winners != null) {
                for (ServerPlayer winner : winners) {
                    if (winner != null) ids.add(winner.getUUID());
                }
            }
            return ids;
        }
        return stupidExpressWinners(level);
    }

    @Nullable
    private static Set<UUID> stupidExpressWinners(ServerLevel level) {
        if (!FabricLoader.getInstance().isModLoaded(STUPID_EXPRESS_ID)) return null;
        try {
            if (!stupidExpressResolved) {
                stupidExpressResolved = true;
                Class<?> type = Class.forName(STUPID_EXPRESS_WINNER_CLASS);
                stupidExpressKey = (ComponentKey<?>) type.getField("KEY").get(null);
                stupidExpressHasWinner = type.getMethod("hasCustomWinner");
                stupidExpressWinners = type.getMethod("getWinners");
            }
            if (stupidExpressKey == null) return null;
            Object component = stupidExpressKey.getNullable(level);
            if (component == null) return null;
            if (!Boolean.TRUE.equals(stupidExpressHasWinner.invoke(component))) return null;
            Set<UUID> ids = new HashSet<>();
            if (stupidExpressWinners.invoke(component) instanceof List<?> winners) {
                for (Object winner : winners) {
                    if (winner instanceof Player player) ids.add(player.getUUID());
                }
            }
            return ids;
        } catch (ReflectiveOperationException | ClassCastException | LinkageError exception) {
            if (stupidExpressKey != null) BrinsWathe.LOGGER.warn("Stupid Express winner lookup failed", exception);
            stupidExpressKey = null;
            return null;
        }
    }

    public record Result(UUID id, String name, String roleId) {
    }
}
