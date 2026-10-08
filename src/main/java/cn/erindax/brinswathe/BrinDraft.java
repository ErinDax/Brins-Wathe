package cn.erindax.brinswathe;

import cn.erindax.brinswathe.mixin.BrinGameFunctionsInvoker;
import cn.erindax.brinswathe.network.BrinDraftChoiceC2SPacket;
import cn.erindax.brinswathe.network.BrinDraftCloseS2CPacket;
import cn.erindax.brinswathe.network.BrinDraftOpenS2CPacket;
import cn.erindax.brinswathe.network.BrinDraftProgressS2CPacket;
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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import org.agmas.harpymodloader.Harpymodloader;

public final class BrinDraft {
    public static final int SECONDS = 20;
    private static final Map<UUID, List<Role>> OFFERS = new HashMap<>();
    private static final Map<UUID, Role> PICKS = new HashMap<>();
    private static final Set<UUID> DECIDED = new HashSet<>();
    private static PendingStart pending;
    private static int ticksLeft;
    private static boolean bypass;

    private BrinDraft() {
    }

    public static void init() {
        ServerLifecycleEvents.SERVER_STARTING.register(server -> reset());
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> reset());
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            UUID id = handler.player.getUUID();
            PICKS.remove(id);
            DECIDED.remove(id);
            if (OFFERS.remove(id) != null && pending != null) broadcastProgress(server);
        });
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
        List<Role> innocents = pool(role -> role.isInnocent() && !role.canUseKiller(), players, game);
        List<Role> killers = pool(Role::canUseKiller, players, game);
        List<Role> neutrals = BrinHarpyRoles.resolveNeutralCount(players, game) > 0
            ? pool(role -> !role.isInnocent() && !role.canUseKiller(), players, game)
            : List.of();
        if (innocents.isEmpty() && killers.isEmpty() && neutrals.isEmpty()) return false;
        OFFERS.clear();
        PICKS.clear();
        DECIDED.clear();
        RandomSource random = world.getRandom();
        for (ServerPlayer player : players) {
            if (!ServerPlayNetworking.canSend(player, BrinDraftOpenS2CPacket.TYPE)) continue;
            List<Role> offer = new ArrayList<>(3);
            addRandom(offer, innocents, random);
            addRandom(offer, killers, random);
            addRandom(offer, neutrals, random);
            OFFERS.put(player.getUUID(), offer);
            List<String> ids = new ArrayList<>(offer.size());
            for (Role role : offer) ids.add(role.identifier().toString());
            ServerPlayNetworking.send(player, new BrinDraftOpenS2CPacket(ids, SECONDS));
        }
        if (OFFERS.isEmpty()) return false;
        pending = new PendingStart(world, mode, mapEffect, time);
        ticksLeft = SECONDS * 20;
        broadcastProgress(world.getServer());
        return true;
    }

    public static boolean boosting() {
        return !PICKS.isEmpty();
    }

    public static double killerBoost(ServerPlayer player) {
        Role pick = PICKS.get(player.getUUID());
        return pick != null && pick.canUseKiller() ? BrinIcFlags.draftBoost : 1.0;
    }

    public static double vigilanteBoost(ServerPlayer player) {
        return WatheRoles.VIGILANTE.equals(PICKS.get(player.getUUID())) ? BrinIcFlags.draftBoost : 1.0;
    }

    public static double roleBoost(ServerPlayer player, Role role) {
        return role != null && role.equals(PICKS.get(player.getUUID())) ? BrinIcFlags.draftBoost : 1.0;
    }

    public static void roundInitialized() {
        PICKS.clear();
    }

    private static void reset() {
        OFFERS.clear();
        PICKS.clear();
        DECIDED.clear();
        pending = null;
        ticksLeft = 0;
        bypass = false;
    }

    private static void choose(ServerPlayer player, String roleId) {
        if (pending == null) return;
        UUID id = player.getUUID();
        List<Role> offer = OFFERS.get(id);
        if (offer == null) return;
        if (roleId.isEmpty()) {
            PICKS.remove(id);
        } else {
            Role picked = null;
            for (Role role : offer) {
                if (role.identifier().toString().equals(roleId)) picked = role;
            }
            if (picked == null) return;
            PICKS.put(id, picked);
        }
        if (DECIDED.add(id)) broadcastProgress(player.getServer());
    }

    private static void tick(MinecraftServer server) {
        if (pending == null) return;
        ticksLeft--;
        boolean everyoneDecided = !OFFERS.isEmpty() && DECIDED.containsAll(OFFERS.keySet());
        if (ticksLeft > 0 && !everyoneDecided) return;
        finish(server);
    }

    private static void broadcastProgress(MinecraftServer server) {
        if (server == null) return;
        int decided = 0;
        for (UUID id : OFFERS.keySet()) {
            if (DECIDED.contains(id)) decided++;
        }
        BrinDraftProgressS2CPacket packet = new BrinDraftProgressS2CPacket(decided, OFFERS.size());
        for (UUID id : OFFERS.keySet()) {
            ServerPlayer player = server.getPlayerList().getPlayer(id);
            if (player == null || !ServerPlayNetworking.canSend(player, BrinDraftProgressS2CPacket.TYPE)) continue;
            ServerPlayNetworking.send(player, packet);
        }
    }

    private static void finish(MinecraftServer server) {
        PendingStart start = pending;
        pending = null;
        for (UUID id : OFFERS.keySet()) {
            ServerPlayer player = server.getPlayerList().getPlayer(id);
            if (player == null || !ServerPlayNetworking.canSend(player, BrinDraftCloseS2CPacket.TYPE)) continue;
            ServerPlayNetworking.send(player, BrinDraftCloseS2CPacket.INSTANCE);
        }
        OFFERS.clear();
        DECIDED.clear();
        bypass = true;
        try {
            GameFunctions.startGame(start.world(), start.mode(), start.mapEffect(), start.time());
        } finally {
            bypass = false;
        }
        if (GameWorldComponent.KEY.get(start.world()).getGameStatus() != GameWorldComponent.GameStatus.STARTING) {
            PICKS.clear();
        }
    }

    private static boolean isMurder(GameMode mode) {
        return mode == WatheGameModes.MURDER || mode == Harpymodloader.MODDED_GAMEMODE;
    }

    private static List<Role> pool(Predicate<Role> camp, List<ServerPlayer> players, GameWorldComponent game) {
        List<Role> roles = new ArrayList<>();
        for (Role role : WatheRoles.ROLES) {
            if (!camp.test(role)) continue;
            if (Harpymodloader.NON_MURDER_ROLES.contains(role) || !BrinHarpyRoles.isEnabled(role)) continue;
            if (BrinRoles.WATCHMAN.equals(role)) continue;
            if (Harpymodloader.VANNILA_ROLES.contains(role)
                && (!WatheRoles.VIGILANTE.equals(role) || BrinHarpyRoles.resolveVigilanteCount(players, game) <= 0)) {
                continue;
            }
            roles.add(role);
        }
        return roles;
    }

    private static void addRandom(List<Role> offer, List<Role> pool, RandomSource random) {
        if (!pool.isEmpty()) offer.add(pool.get(random.nextInt(pool.size())));
    }

    private record PendingStart(ServerLevel world, GameMode mode, MapEffect mapEffect, int time) {
    }
}
