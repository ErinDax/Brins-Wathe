package cn.erindax.brinswathe;

import cn.erindax.brinswathe.mixin.BrinGameFunctionsInvoker;
import cn.erindax.brinswathe.network.BrinDraftChoiceC2SPacket;
import cn.erindax.brinswathe.network.BrinDraftCloseS2CPacket;
import cn.erindax.brinswathe.network.BrinDraftStateS2CPacket;
import dev.doctor4t.wathe.api.GameMode;
import dev.doctor4t.wathe.api.MapEffect;
import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.api.WatheGameModes;
import dev.doctor4t.wathe.api.WatheRoles;
import dev.doctor4t.wathe.cca.GameWorldComponent;
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
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import org.BsXinQin.kinswathe.KinsWatheRoles;
import org.agmas.harpymodloader.Harpymodloader;

public final class BrinDraft {
    public static final int TURN_SECONDS = 15;
    private static final int END_TICKS = 60;
    private static final int OFFER_SIZE = 3;
    private static final int INNOCENT = 0;
    private static final int KILLER = 1;
    private static final int NEUTRAL = 2;
    private static final List<UUID> ORDER = new ArrayList<>();
    private static final Map<UUID, Integer> NUMBERS = new HashMap<>();
    private static final Map<UUID, Role> PICKS = new HashMap<>();
    private static final Set<UUID> RANDOM_PICKS = new HashSet<>();
    private static final Map<Role, Integer> CAPACITY = new LinkedHashMap<>();
    private static final Map<Role, Integer> USED = new HashMap<>();
    private static final Map<UUID, Role> APPLIED = new HashMap<>();
    private static final int[] SLOTS = new int[3];
    private static List<Role> offer = List.of();
    private static PendingStart pending;
    private static ServerLevel appliedWorld;
    private static int turn;
    private static int ticksLeft;
    private static boolean bypass;

    private BrinDraft() {
    }

    public static void init() {
        ServerLifecycleEvents.SERVER_STARTING.register(server -> reset());
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> reset());
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> leave(server, handler.player.getUUID()));
        ServerTickEvents.END_SERVER_TICK.register(BrinDraft::tick);
        ServerPlayNetworking.registerGlobalReceiver(
            BrinDraftChoiceC2SPacket.TYPE,
            (payload, context) -> choose(context.player(), payload.roleId())
        );
    }

    public static boolean interceptStart(ServerLevel world, GameMode mode, MapEffect mapEffect, int time) {
        if (bypass) return false;
        if (pending != null) return true;
        if (!BrinIcFlags.draft || !isMurder(mode)) return false;
        GameWorldComponent game = GameWorldComponent.KEY.get(world);
        if (game.getGameStatus() != GameWorldComponent.GameStatus.INACTIVE) return false;
        List<ServerPlayer> players = BrinGameFunctionsInvoker.brinGetReadyPlayerList(world);
        if (players.size() < mode.minPlayerCount) return false;
        clearDraft();
        buildPool(players, game);
        int[] reserved = new int[3];
        List<UUID> drafters = new ArrayList<>();
        for (ServerPlayer player : players) {
            UUID id = player.getUUID();
            Role forced = BrinPunishment.willPunish(id)
                ? KinsWatheRoles.DRUGMAKER
                : Harpymodloader.FORCED_MODDED_ROLE_FLIP.get(id);
            if (forced != null) {
                reserved[camp(forced)]++;
                USED.merge(forced, 1, Integer::sum);
                continue;
            }
            if (ServerPlayNetworking.canSend(player, BrinDraftStateS2CPacket.TYPE)) drafters.add(id);
        }
        if (drafters.isEmpty()) {
            clearDraft();
            return false;
        }
        int count = drafters.size();
        int killers = clamp(BrinHarpyRoles.resolveKillerCount(players, game) - reserved[KILLER], count);
        int neutrals = clamp(
            Math.min(BrinHarpyRoles.resolveNeutralCount(players, game) - reserved[NEUTRAL], neutralCapacity()),
            count - killers
        );
        SLOTS[INNOCENT] = count - killers - neutrals;
        SLOTS[KILLER] = killers;
        SLOTS[NEUTRAL] = neutrals;
        RandomSource random = world.getRandom();
        for (int i = drafters.size() - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            UUID swap = drafters.get(i);
            drafters.set(i, drafters.get(j));
            drafters.set(j, swap);
        }
        for (UUID id : drafters) {
            ORDER.add(id);
            NUMBERS.put(id, ORDER.size());
        }
        pending = new PendingStart(world, mode, mapEffect, time);
        turn = 0;
        startTurn(world.getServer(), true);
        return true;
    }

    public static boolean isBusy() {
        return pending != null || bypass;
    }

    public static void roundInitialized() {
        APPLIED.clear();
        appliedWorld = null;
    }

    private static void reset() {
        clearDraft();
        APPLIED.clear();
        appliedWorld = null;
        bypass = false;
    }

    private static void clearDraft() {
        ORDER.clear();
        NUMBERS.clear();
        PICKS.clear();
        RANDOM_PICKS.clear();
        CAPACITY.clear();
        USED.clear();
        SLOTS[INNOCENT] = 0;
        SLOTS[KILLER] = 0;
        SLOTS[NEUTRAL] = 0;
        offer = List.of();
        pending = null;
        turn = 0;
        ticksLeft = 0;
    }

    private static void tick(MinecraftServer server) {
        if (pending == null) {
            watchApplied();
            return;
        }
        if (--ticksLeft > 0) return;
        if (turn < ORDER.size()) {
            commit(ORDER.get(turn), null);
            turn++;
            startTurn(server, false);
        } else {
            finish(server);
        }
    }

    private static void choose(ServerPlayer player, String roleId) {
        if (pending == null || turn >= ORDER.size()) return;
        UUID id = player.getUUID();
        if (!id.equals(ORDER.get(turn))) return;
        Role chosen = null;
        if (!roleId.isEmpty()) {
            for (Role role : offer) {
                if (role.identifier().toString().equals(roleId)) chosen = role;
            }
            if (chosen == null) return;
        }
        commit(id, chosen);
        turn++;
        startTurn(player.getServer(), false);
    }

    private static void leave(MinecraftServer server, UUID id) {
        if (pending == null) return;
        int index = ORDER.indexOf(id);
        if (index < 0) return;
        Role picked = PICKS.remove(id);
        RANDOM_PICKS.remove(id);
        if (picked != null) {
            USED.merge(picked, -1, Integer::sum);
            SLOTS[camp(picked)]++;
        }
        ORDER.remove(index);
        NUMBERS.remove(id);
        boolean current = index == turn;
        if (index < turn) turn--;
        int unpicked = ORDER.size() - turn;
        while (SLOTS[INNOCENT] + SLOTS[KILLER] + SLOTS[NEUTRAL] > unpicked) {
            if (SLOTS[INNOCENT] > 0) SLOTS[INNOCENT]--;
            else if (SLOTS[NEUTRAL] > 0) SLOTS[NEUTRAL]--;
            else SLOTS[KILLER]--;
        }
        if (ORDER.isEmpty()) {
            finish(server);
        } else if (current) {
            startTurn(server, false);
        } else {
            broadcast(server, false);
        }
    }

    private static void startTurn(MinecraftServer server, boolean open) {
        if (turn >= ORDER.size()) {
            offer = List.of();
            ticksLeft = END_TICKS;
        } else {
            offer = buildOffer(pending.world().getRandom());
            ticksLeft = TURN_SECONDS * 20;
        }
        broadcast(server, open);
    }

    private static void commit(UUID id, Role chosen) {
        Role role = chosen;
        if (role == null) {
            role = draw(List.of(), true, pending.world().getRandom());
            if (role == null) role = WatheRoles.CIVILIAN;
            RANDOM_PICKS.add(id);
        }
        PICKS.put(id, role);
        USED.merge(role, 1, Integer::sum);
        int camp = camp(role);
        if (SLOTS[camp] > 0) {
            SLOTS[camp]--;
        } else if (SLOTS[INNOCENT] > 0) {
            SLOTS[INNOCENT]--;
        } else if (SLOTS[NEUTRAL] > 0) {
            SLOTS[NEUTRAL]--;
        } else if (SLOTS[KILLER] > 0) {
            SLOTS[KILLER]--;
        }
    }

    private static void broadcast(MinecraftServer server, boolean open) {
        if (server == null) return;
        int total = NUMBERS.values().stream().mapToInt(Integer::intValue).max().orElse(0);
        int current = turn < ORDER.size() ? NUMBERS.get(ORDER.get(turn)) : 0;
        int seconds = Math.max(0, (ticksLeft + 19) / 20);
        for (UUID id : ORDER) {
            ServerPlayer player = server.getPlayerList().getPlayer(id);
            if (player == null || !ServerPlayNetworking.canSend(player, BrinDraftStateS2CPacket.TYPE)) continue;
            List<String> offers = new ArrayList<>();
            if (turn < ORDER.size() && id.equals(ORDER.get(turn))) {
                for (Role role : offer) offers.add(role.identifier().toString());
            }
            boolean random = RANDOM_PICKS.contains(id);
            Role picked = PICKS.get(id);
            String pickedId = picked == null || random ? "" : picked.identifier().toString();
            ServerPlayNetworking.send(player, new BrinDraftStateS2CPacket(
                open,
                NUMBERS.get(id),
                total,
                current,
                seconds,
                offers,
                pickedId,
                random
            ));
        }
    }

    private static void finish(MinecraftServer server) {
        PendingStart start = pending;
        for (UUID id : ORDER) {
            ServerPlayer player = server.getPlayerList().getPlayer(id);
            if (player == null || !ServerPlayNetworking.canSend(player, BrinDraftCloseS2CPacket.TYPE)) continue;
            ServerPlayNetworking.send(player, BrinDraftCloseS2CPacket.INSTANCE);
        }
        APPLIED.clear();
        for (UUID id : ORDER) {
            Role role = PICKS.get(id);
            if (role == null) continue;
            BrinHarpyRoles.forceRole(id, role);
            APPLIED.put(id, role);
        }
        appliedWorld = start.world();
        clearDraft();
        bypass = true;
        try {
            GameFunctions.startGame(start.world(), start.mode(), start.mapEffect(), start.time());
        } finally {
            bypass = false;
        }
        if (GameWorldComponent.KEY.get(start.world()).getGameStatus() != GameWorldComponent.GameStatus.STARTING) {
            releaseApplied();
        }
    }

    private static void watchApplied() {
        if (APPLIED.isEmpty() || appliedWorld == null) return;
        if (GameWorldComponent.KEY.get(appliedWorld).getGameStatus() == GameWorldComponent.GameStatus.INACTIVE) {
            releaseApplied();
        }
    }

    private static void releaseApplied() {
        for (Map.Entry<UUID, Role> entry : APPLIED.entrySet()) {
            BrinHarpyRoles.unforceRole(entry.getKey(), entry.getValue());
        }
        APPLIED.clear();
        appliedWorld = null;
    }

    private static List<Role> buildOffer(RandomSource random) {
        List<Role> roles = new ArrayList<>(OFFER_SIZE);
        for (int i = 0; i < OFFER_SIZE; i++) {
            Role role = draw(roles, false, random);
            if (role == null) break;
            roles.add(role);
        }
        if (roles.isEmpty()) roles.add(WatheRoles.CIVILIAN);
        return roles;
    }

    private static Role draw(List<Role> exclude, boolean includeBase, RandomSource random) {
        List<Integer> camps = new ArrayList<>(3);
        for (int camp = 0; camp < SLOTS.length; camp++) {
            if (SLOTS[camp] > 0) camps.add(camp);
        }
        while (!camps.isEmpty()) {
            int weight = 0;
            for (int camp : camps) weight += SLOTS[camp];
            int roll = random.nextInt(weight);
            int chosen = camps.getLast();
            for (int camp : camps) {
                roll -= SLOTS[camp];
                if (roll < 0) {
                    chosen = camp;
                    break;
                }
            }
            List<Role> options = new ArrayList<>();
            for (Role role : CAPACITY.keySet()) {
                if (camp(role) == chosen && available(role) && !exclude.contains(role)) options.add(role);
            }
            Role base = baseRole(chosen);
            if (base != null && !exclude.contains(base) && (includeBase || options.isEmpty())) options.add(base);
            if (!options.isEmpty()) return options.get(random.nextInt(options.size()));
            camps.remove(Integer.valueOf(chosen));
        }
        return null;
    }

    private static void buildPool(List<ServerPlayer> players, GameWorldComponent game) {
        int vigilantes = BrinHarpyRoles.resolveVigilanteCount(players, game);
        for (Role role : WatheRoles.ROLES) {
            if (Harpymodloader.NON_MURDER_ROLES.contains(role) || !BrinHarpyRoles.isEnabled(role)) continue;
            int capacity;
            if (WatheRoles.VIGILANTE.equals(role)) {
                capacity = vigilantes;
            } else if (Harpymodloader.VANNILA_ROLES.contains(role)) {
                continue;
            } else {
                Integer maximum = Harpymodloader.ROLE_MAX.get(role.identifier());
                capacity = maximum == null ? 1 : maximum;
            }
            if (capacity > 0) CAPACITY.put(role, capacity);
        }
    }

    private static boolean available(Role role) {
        if (USED.getOrDefault(role, 0) >= CAPACITY.getOrDefault(role, 0)) return false;
        if (BrinRoles.WATCHMAN.equals(role)) return USED.getOrDefault(BrinRoles.TRAPPER, 0) > 0;
        if (BrinRoles.BONEHARVESTER.equals(role)) return USED.getOrDefault(KinsWatheRoles.CLEANER, 0) == 0;
        if (KinsWatheRoles.CLEANER != null && KinsWatheRoles.CLEANER.equals(role)) {
            return USED.getOrDefault(BrinRoles.BONEHARVESTER, 0) == 0;
        }
        return true;
    }

    private static int neutralCapacity() {
        int total = 0;
        for (Map.Entry<Role, Integer> entry : CAPACITY.entrySet()) {
            if (camp(entry.getKey()) != NEUTRAL) continue;
            total += Math.max(0, entry.getValue() - USED.getOrDefault(entry.getKey(), 0));
        }
        return total;
    }

    private static Role baseRole(int camp) {
        if (camp == INNOCENT) return WatheRoles.CIVILIAN;
        if (camp == KILLER) return WatheRoles.KILLER;
        return null;
    }

    private static int camp(Role role) {
        if (role.canUseKiller()) return KILLER;
        if (role.isInnocent()) return INNOCENT;
        return NEUTRAL;
    }

    private static int clamp(int value, int max) {
        return Math.max(0, Math.min(max, value));
    }

    private static boolean isMurder(GameMode mode) {
        return mode == WatheGameModes.MURDER || mode == Harpymodloader.MODDED_GAMEMODE;
    }

    private record PendingStart(ServerLevel world, GameMode mode, MapEffect mapEffect, int time) {
    }
}
