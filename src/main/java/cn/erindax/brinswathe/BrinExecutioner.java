package cn.erindax.brinswathe;

import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.PlayerPoisonComponent;
import dev.doctor4t.wathe.cca.PlayerShopComponent;
import dev.doctor4t.wathe.client.gui.RoleAnnouncementTexts;
import dev.doctor4t.wathe.game.GameFunctions;
import dev.doctor4t.wathe.util.AnnounceWelcomePayload;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import org.agmas.harpymodloader.Harpymodloader;
import org.agmas.harpymodloader.events.ModdedRoleAssigned;

public final class BrinExecutioner {
    private static final int CONVERSION_BALANCE = 200;
    private static final int REVIVE_INVISIBILITY_TICKS = 600;
    private static final ResourceLocation MODDED_BACKFIRE =
        ResourceLocation.fromNamespaceAndPath("noellesroles", "modded_backfire");
    private static final Set<UUID> CONVERTING = ConcurrentHashMap.newKeySet();

    private BrinExecutioner() {
    }

    public record KillFrame(Map<UUID, UUID> hiddenTargets, Vec3 deathPos, Set<UUID> aliveBeforeKill) {
    }

    public static KillFrame beginKill(Player victim) {
        Map<UUID, UUID> hidden = new HashMap<>();
        Set<UUID> aliveBeforeKill = new HashSet<>();
        KillFrame frame = new KillFrame(hidden, victim.position(), aliveBeforeKill);
        if (victim.level().isClientSide || !GameFunctions.isPlayerAliveAndSurvival(victim)) return frame;

        GameWorldComponent game = GameWorldComponent.KEY.get(victim.level());
        Role role = BrinNoelleAccess.findRole(BrinNoelleAccess.EXECUTIONER_ID);
        if (role == null) return frame;
        for (UUID executionerId : game.getAllWithRole(role)) {
            Player executioner = victim.level().getPlayerByUUID(executionerId);
            if (executioner == null) continue;
            if (GameFunctions.isPlayerAliveAndSurvival(executioner)) {
                aliveBeforeKill.add(executionerId);
            }
            UUID target = BrinNoelleAccess.executionerTarget(executioner);
            if (target == null || !target.equals(victim.getUUID())) continue;
            hidden.put(executionerId, target);
            CONVERTING.add(executionerId);
            BrinNoelleAccess.setExecutionerTarget(executioner, Util.NIL_UUID);
        }
        return frame;
    }

    public static boolean allowDeath(Player victim, ResourceLocation deathReason) {
        if (victim == null || deathReason == null) return true;
        if (!MODDED_BACKFIRE.equals(deathReason)) return true;
        return !CONVERTING.contains(victim.getUUID());
    }

    public static void endKill(Player victim, Player killer, KillFrame frame) {
        if (frame == null) return;
        try {
            endKillInner(victim, killer, frame);
        } finally {
            CONVERTING.removeAll(frame.hiddenTargets().keySet());
        }
    }

    private static void endKillInner(Player victim, Player killer, KillFrame frame) {
        if (frame.hiddenTargets().isEmpty()) return;
        GameWorldComponent game = GameWorldComponent.KEY.get(victim.level());
        boolean died = !GameFunctions.isPlayerAliveAndSurvival(victim);

        for (Map.Entry<UUID, UUID> entry : frame.hiddenTargets().entrySet()) {
            Player executioner = victim.level().getPlayerByUUID(entry.getKey());
            if (executioner == null) continue;
            BrinNoelleAccess.setExecutionerTarget(executioner, entry.getValue());
            if (!died || !(executioner instanceof ServerPlayer serverExecutioner)) continue;
            if (!isValidKill(game, serverExecutioner, killer)) continue;
            convert(serverExecutioner, frame.deathPos(), game, frame.aliveBeforeKill().contains(entry.getKey()));
        }
    }

    private static boolean isValidKill(GameWorldComponent game, ServerPlayer executioner, Player killer) {
        if (killer == null) return false;
        if (killer == executioner) return true;
        return game.isInnocent(killer);
    }

    private static void convert(ServerPlayer executioner, Vec3 deathPos, GameWorldComponent game, boolean wasAlive) {
        boolean shouldRevive = !wasAlive && (executioner.isSpectator() || executioner.isCreative());
        BrinNoelleAccess.setExecutionerWon(executioner, true);
        if (shouldRevive) {
            revive(executioner, deathPos);
        } else if (wasAlive && !GameFunctions.isPlayerAliveAndSurvival(executioner)) {
            executioner.setGameMode(GameType.ADVENTURE);
        }

        Role newRole = CompensatorPassive.drawRole(true);
        game.addRole(executioner, newRole);
        ModdedRoleAssigned.EVENT.invoker().assignModdedRole(executioner, newRole);
        PlayerShopComponent shop = PlayerShopComponent.KEY.get(executioner);
        if (shop != null) shop.setBalance(CONVERSION_BALANCE);
        PlayerPoisonComponent poison = PlayerPoisonComponent.KEY.get(executioner);
        if (poison != null) poison.reset();
        game.sync();
        announce(executioner, game, newRole);
    }

    private static void revive(ServerPlayer executioner, Vec3 deathPos) {
        executioner.setGameMode(GameType.ADVENTURE);
        executioner.connection.teleport(deathPos.x, deathPos.y, deathPos.z, executioner.getYRot(), executioner.getXRot());
        executioner.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, REVIVE_INVISIBILITY_TICKS, 0, false, false, false));
        for (int slot = 0; slot < 9; slot++) {
            if (executioner.getInventory().getItem(slot).isEmpty()) {
                executioner.getInventory().selected = slot;
                break;
            }
        }
    }

    private static void announce(ServerPlayer player, GameWorldComponent game, Role role) {
        RoleAnnouncementTexts.RoleAnnouncementText announcement = Harpymodloader.VANNILA_ROLES.contains(role)
            ? RoleAnnouncementTexts.KILLER
            : Harpymodloader.autogeneratedAnnouncements.get(role);
        if (announcement == null) return;
        int index = RoleAnnouncementTexts.ROLE_ANNOUNCEMENT_TEXTS.indexOf(announcement);
        if (index < 0) return;
        ServerPlayNetworking.send(
            player,
            new AnnounceWelcomePayload(index, game.getAllKillerTeamPlayers().size(), 0)
        );
    }
}
