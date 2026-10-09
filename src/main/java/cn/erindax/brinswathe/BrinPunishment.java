package cn.erindax.brinswathe;

import dev.doctor4t.wathe.api.GameMode;
import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.api.WatheGameModes;
import dev.doctor4t.wathe.api.event.GameEvents;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameConstants;
import dev.doctor4t.wathe.game.GameFunctions;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.BsXinQin.kinswathe.KinsWatheRoles;
import org.agmas.harpymodloader.Harpymodloader;
import org.agmas.harpymodloader.component.WorldModifierComponent;
import org.agmas.harpymodloader.modifiers.Modifier;
import org.jetbrains.annotations.Nullable;

public final class BrinPunishment {
    public static final int EARLY_FALL_SECONDS = 45;
    private static final int SWAP_GRACE_SECONDS = 10;
    private static final String SWAPPER_HANDLER = "org.agmas.noellesroles.Noellesroles";
    private static final int GLOW_TICKS = 60;
    private static final Map<UUID, String> QUEUED = new LinkedHashMap<>();
    private static final Map<UUID, Integer> SWAPPED = new HashMap<>();
    private static final Set<UUID> GLOWING = new HashSet<>();
    private static final Set<UUID> ENDED = new HashSet<>();
    private static int roundStartTick = -1;

    private BrinPunishment() {
    }

    public static void init() {
        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            QUEUED.clear();
            SWAPPED.clear();
            GLOWING.clear();
            ENDED.clear();
            roundStartTick = -1;
        });
        GameEvents.ON_FINISH_INITIALIZE.register(BrinPunishment::roundInitialized);
        ServerTickEvents.END_SERVER_TICK.register(BrinPunishment::tick);
    }

    public static boolean queue(ServerPlayer player) {
        return queue(player.getUUID(), player.getGameProfile().getName());
    }

    public static boolean queue(UUID playerId, String name) {
        return QUEUED.putIfAbsent(playerId, name) == null;
    }

    public static boolean cancel(UUID playerId) {
        return QUEUED.remove(playerId) != null;
    }

    public static boolean isQueued(UUID playerId) {
        return QUEUED.containsKey(playerId);
    }

    public static List<String> queuedNames() {
        return new ArrayList<>(QUEUED.values());
    }

    public static void applyQueued(List<ServerPlayer> players) {
        ENDED.clear();
        Modifier punishment = BrinIcModifiers.PUNISHMENT;
        Role drugmaker = KinsWatheRoles.DRUGMAKER;
        if (punishment == null || drugmaker == null) return;
        List<UUID> forcedByCommand = Harpymodloader.FORCED_MODDED_MODIFIER.get(punishment);
        for (ServerPlayer player : players) {
            UUID id = player.getUUID();
            boolean queued = QUEUED.remove(id) != null;
            if (!queued && (forcedByCommand == null || !forcedByCommand.contains(id))) continue;
            forceRole(id, drugmaker);
            List<UUID> forced = Harpymodloader.FORCED_MODDED_MODIFIER.computeIfAbsent(punishment, key -> new ArrayList<>());
            if (!forced.contains(id)) forced.add(id);
        }
    }

    public static void onKilled(Player victim, @Nullable Player killer, ResourceLocation deathReason) {
        if (!BrinIcFlags.punishEarlyFall || !(victim instanceof ServerPlayer player)) return;
        if (!GameConstants.DeathReasons.FELL_OUT_OF_TRAIN.equals(deathReason)) return;
        if (killer != null && !killer.getUUID().equals(player.getUUID())) return;
        int now = player.server.getTickCount();
        if (roundStartTick < 0 || now - roundStartTick > EARLY_FALL_SECONDS * 20) return;
        Integer swappedAt = SWAPPED.get(player.getUUID());
        if (swappedAt != null && now - swappedAt <= SWAP_GRACE_SECONDS * 20) return;
        if (GameFunctions.isPlayerAliveAndSurvival(player)) return;
        if (!GameWorldComponent.KEY.get(player.level()).isRunning()) return;
        if (!queue(player)) return;
        BrinsWathe.LOGGER.info("{} fell off the train early and will be punished next round", player.getGameProfile().getName());
        player.sendSystemMessage(Component.translatableWithFallback(
            "tip.brinswathe.punishment_queued",
            "你在开局 %s 秒内掉出了列车，下一局将受到惩罚",
            EARLY_FALL_SECONDS
        ).withStyle(ChatFormatting.DARK_RED));
    }

    public static void onMoved(ServerPlayer player) {
        if (!GameWorldComponent.KEY.get(player.level()).isRunning()) return;
        boolean swapped = StackWalker.getInstance().walk(frames ->
            frames.limit(8).anyMatch(frame -> SWAPPER_HANDLER.equals(frame.getClassName())));
        if (swapped) SWAPPED.put(player.getUUID(), player.server.getTickCount());
    }

    private static void forceRole(UUID playerId, Role role) {
        Role previous = Harpymodloader.FORCED_MODDED_ROLE_FLIP.put(playerId, role);
        if (previous != null && !previous.equals(role)) {
            List<UUID> previousIds = Harpymodloader.FORCED_MODDED_ROLE.get(previous);
            if (previousIds != null) {
                previousIds.removeIf(playerId::equals);
                if (previousIds.isEmpty()) Harpymodloader.FORCED_MODDED_ROLE.remove(previous);
            }
        }
        List<UUID> ids = Harpymodloader.FORCED_MODDED_ROLE.computeIfAbsent(role, key -> new ArrayList<>());
        if (!ids.contains(playerId)) ids.add(playerId);
    }

    private static void roundInitialized(Level world, GameWorldComponent game) {
        if (world.isClientSide() || world.getServer() == null) return;
        roundStartTick = isMurder(game.getGameMode()) ? world.getServer().getTickCount() : -1;
        SWAPPED.clear();
        Modifier punishment = BrinIcModifiers.PUNISHMENT;
        if (punishment == null) return;
        for (UUID id : WorldModifierComponent.KEY.get(world).getAllWithModifier(punishment)) {
            Player player = world.getPlayerByUUID(id);
            if (player == null) continue;
            if (!game.isRole(player, KinsWatheRoles.DRUGMAKER)) {
                BrinsWathe.LOGGER.warn("Punished player {} did not get the drugmaker role", player.getGameProfile().getName());
            }
            player.sendSystemMessage(Component.translatableWithFallback(
                "tip.brinswathe.punishment",
                "你受到了惩罚：本局全场都能看到你的高亮，直至死亡"
            ).withStyle(ChatFormatting.DARK_RED));
        }
    }

    private static boolean isMurder(GameMode mode) {
        return mode == WatheGameModes.MURDER || mode == Harpymodloader.MODDED_GAMEMODE;
    }

    private static void tick(MinecraftServer server) {
        if (server.getTickCount() % 20 != 0) return;
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            UUID id = player.getUUID();
            GameWorldComponent game = GameWorldComponent.KEY.get(player.level());
            boolean punished = game.getGameStatus() == GameWorldComponent.GameStatus.ACTIVE
                && BrinModifiers.hasModifier(player, BrinModifiers.PUNISHMENT);
            if (punished && !GameFunctions.isPlayerAliveAndSurvival(player)) ENDED.add(id);
            if (punished && !ENDED.contains(id)) {
                player.addEffect(new MobEffectInstance(MobEffects.GLOWING, GLOW_TICKS, 0, false, false));
                GLOWING.add(id);
            } else if (GLOWING.remove(id)) {
                player.removeEffect(MobEffects.GLOWING);
            }
        }
    }
}
