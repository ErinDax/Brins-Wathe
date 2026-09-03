package cn.erindax.brinswathe;

import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.PlayerPoisonComponent;
import dev.doctor4t.wathe.cca.PlayerShopComponent;
import dev.doctor4t.wathe.client.gui.RoleAnnouncementTexts;
import dev.doctor4t.wathe.game.GameFunctions;
import dev.doctor4t.wathe.util.AnnounceWelcomePayload;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.Util;
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
    private static final Set<UUID> CONVERTED_THIS_ROUND = ConcurrentHashMap.newKeySet();

    private BrinExecutioner() {
    }

    public static void resetRound() {
        CONVERTED_THIS_ROUND.clear();
    }

    public static boolean isInRound(GameWorldComponent game) {
        if (!CONVERTED_THIS_ROUND.isEmpty()) return true;
        Role role = BrinNoelleAccess.findRole(BrinNoelleAccess.EXECUTIONER_ID);
        return role != null && !game.getAllWithRole(role).isEmpty();
    }

    public record KillFrame(Map<UUID, UUID> hiddenTargets, Vec3 deathPos) {
    }

    public static KillFrame beginKill(Player victim) {
        Map<UUID, UUID> hidden = new HashMap<>();
        KillFrame frame = new KillFrame(hidden, victim.position());
        if (victim.level().isClientSide || !GameFunctions.isPlayerAliveAndSurvival(victim)) return frame;

        GameWorldComponent game = GameWorldComponent.KEY.get(victim.level());
        Role role = BrinNoelleAccess.findRole(BrinNoelleAccess.EXECUTIONER_ID);
        if (role == null) return frame;
        for (UUID executionerId : game.getAllWithRole(role)) {
            Player executioner = victim.level().getPlayerByUUID(executionerId);
            if (executioner == null) continue;
            UUID target = BrinNoelleAccess.executionerTarget(executioner);
            if (target == null || !target.equals(victim.getUUID())) continue;
            hidden.put(executionerId, target);
            BrinNoelleAccess.setExecutionerTarget(executioner, Util.NIL_UUID);
        }
        return frame;
    }

    public static void endKill(Player victim, Player killer, KillFrame frame) {
        if (frame == null || frame.hiddenTargets().isEmpty()) return;
        GameWorldComponent game = GameWorldComponent.KEY.get(victim.level());
        boolean died = !GameFunctions.isPlayerAliveAndSurvival(victim);

        for (Map.Entry<UUID, UUID> entry : frame.hiddenTargets().entrySet()) {
            Player executioner = victim.level().getPlayerByUUID(entry.getKey());
            if (executioner == null) continue;
            BrinNoelleAccess.setExecutionerTarget(executioner, entry.getValue());
            if (!died || !(executioner instanceof ServerPlayer serverExecutioner)) continue;
            if (!isValidKill(game, serverExecutioner, killer)) continue;
            convert(serverExecutioner, frame.deathPos(), game);
        }
    }

    private static boolean isValidKill(GameWorldComponent game, ServerPlayer executioner, Player killer) {
        if (killer == null) return false;
        if (killer == executioner) return true;
        return game.isInnocent(killer);
    }

    private static void convert(ServerPlayer executioner, Vec3 deathPos, GameWorldComponent game) {
        BrinNoelleAccess.setExecutionerWon(executioner, true);
        if (executioner.isSpectator() || executioner.isCreative()) {
            revive(executioner, deathPos);
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
        CONVERTED_THIS_ROUND.add(executioner.getUUID());
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
