package cn.erindax.brinswathe;

import cn.erindax.brinswathe.component.AvengerComponent;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public final class AvengerWitness {
    private static final Map<UUID, Boolean> LAST_ALIVE_SURVIVAL = new HashMap<>();
    private static final double MAX_WITNESS_DISTANCE = 64.0D;
    private static final double MIN_LOOK_DOT = 0.25D;
    private AvengerWitness() {
    }
    public static void tick(MinecraftServer server) {

        if (CowboyDuel.isActive()) {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                LAST_ALIVE_SURVIVAL.put(player.getUUID(), GameFunctions.isPlayerAliveAndSurvival(player));
            }
            return;
        }
        List<ServerPlayer> players = server.getPlayerList().getPlayers();
        Set<UUID> online = new HashSet<>();
        List<ServerPlayer> victims = null;
        for (ServerPlayer player : players) {
            online.add(player.getUUID());
            boolean alive = GameFunctions.isPlayerAliveAndSurvival(player);
            Boolean wasAlive = LAST_ALIVE_SURVIVAL.put(player.getUUID(), alive);
            if (wasAlive == null || !wasAlive || alive) continue;
            if (!player.isSpectator() && !player.isDeadOrDying()) continue;
            if (GameWorldComponent.KEY.get(player.level()).getGameStatus()
                != GameWorldComponent.GameStatus.ACTIVE) {
                continue;
            }
            if (victims == null) victims = new ArrayList<>();
            victims.add(player);
        }
        LAST_ALIVE_SURVIVAL.keySet().retainAll(online);
        if (victims == null) return;
        for (ServerPlayer victim : victims) {
            notifyWitnesses(victim);
        }
    }
    private static void notifyWitnesses(ServerPlayer victim) {
        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(victim.level());
        for (var viewer : victim.level().players()) {
            if (viewer == victim || !(viewer instanceof ServerPlayer serverViewer)) continue;
            if (!gameWorld.isRole(serverViewer, BrinRoles.AVENGER)) continue;
            if (!GameFunctions.isPlayerAliveAndSurvival(serverViewer)) continue;
            if (serverViewer.isSleeping()) continue;
            if (!canSee(serverViewer, victim)) continue;
            AvengerComponent component = AvengerComponent.KEY.get(serverViewer);
            if (component != null) component.witness();
        }
    }

    private static boolean canSee(ServerPlayer viewer, ServerPlayer victim) {
        Vec3 eye = viewer.getEyePosition();
        Vec3 target = victim.position().add(0.0D, victim.getBbHeight() * 0.5D, 0.0D);
        Vec3 delta = target.subtract(eye);
        double distance = delta.length();
        if (distance > MAX_WITNESS_DISTANCE) return false;
        if (distance > 1.0E-4D && viewer.getLookAngle().dot(delta.scale(1.0D / distance)) < MIN_LOOK_DOT) {
            return false;
        }
        ClipContext context = new ClipContext(eye, target, ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, viewer);
        return viewer.level().clip(context).getType() == HitResult.Type.MISS;
    }
}
