package cn.autoforged.brinswathe.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.api.WatheRoles;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.agmas.harpymodloader.Harpymodloader;
import org.agmas.harpymodloader.config.HarpyModLoaderConfig;
import org.agmas.harpymodloader.modded_murder.ModdedMurderGameMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ModdedMurderGameMode.class, priority = 1100)
public abstract class BrinForcedNeutralRoleMixin {
    @Inject(
        method = "assignCivilianReplacingRoles",
        at = @At(
            value = "INVOKE",
            target = "Ljava/util/Collections;shuffle(Ljava/util/List;)V",
            ordinal = 1,
            shift = At.Shift.AFTER
        )
    )
    private void brinAssignForcedNeutralRoles(
        int desiredRoleCount,
        ServerLevel serverLevel,
        GameWorldComponent gameWorld,
        List<ServerPlayer> players,
        CallbackInfo ci,
        @Local(index = 7) ArrayList<ServerPlayer> civilianCandidates
    ) {
        int remainingSlots = Math.max(
            0,
            brinResolveNeutralLimit(players, gameWorld)
                - brinCountAssignedNeutralPlayers(players, gameWorld)
        );
        remainingSlots = brinLimitForcedPlayers(civilianCandidates, gameWorld, remainingSlots);
        brinLimitForcedRefreshRoles(remainingSlots);
    }

    @Inject(
        method = "assignCivilianReplacingRoles",
        at = @At(
            value = "INVOKE",
            target = "Ljava/util/ArrayList;iterator()Ljava/util/Iterator;",
            ordinal = 1
        )
    )
    private void brinCountForcedNeutralRoles(
        int desiredRoleCount,
        ServerLevel serverLevel,
        GameWorldComponent gameWorld,
        List<ServerPlayer> players,
        CallbackInfo ci,
        @Local(index = 8) LocalIntRef neutralLimit,
        @Local(index = 10) LocalIntRef assignedNeutralCount
    ) {
        int configuredLimit = brinResolveNeutralLimit(players, gameWorld);
        neutralLimit.set(configuredLimit);
        assignedNeutralCount.set(Math.min(
            brinCountAssignedNeutralPlayers(players, gameWorld),
            configuredLimit
        ));
    }

    @Inject(method = "assignCivilianReplacingRoles", at = @At("RETURN"))
    private void brinNormalizeNeutralQuota(
        int desiredRoleCount,
        ServerLevel serverLevel,
        GameWorldComponent gameWorld,
        List<ServerPlayer> players,
        CallbackInfo ci
    ) {
        int neutralLimit = Math.min(players.size(), brinResolveNeutralLimit(players, gameWorld));
        List<ServerPlayer> neutralPlayers = new ArrayList<>();
        for (ServerPlayer player : players) {
            Role role = gameWorld.getRole(player);
            if (role != null && !role.isInnocent() && !role.canUseKiller()) {
                neutralPlayers.add(player);
            }
        }

        Collections.shuffle(neutralPlayers);
        neutralPlayers.sort((left, right) -> Boolean.compare(
            brinIsExplicitlyForced(right),
            brinIsExplicitlyForced(left)
        ));
        for (int index = neutralLimit; index < neutralPlayers.size(); index++) {
            ServerPlayer player = neutralPlayers.get(index);
            Role oldRole = gameWorld.getRole(player);
            org.agmas.harpymodloader.events.ModdedRoleRemoved.EVENT.invoker()
                .removeModdedRole(player, oldRole);
            gameWorld.addRole(player, WatheRoles.CIVILIAN);
        }

        int assignedNeutralCount = Math.min(neutralPlayers.size(), neutralLimit);
        if (assignedNeutralCount < neutralLimit) {
            List<ServerPlayer> civilianCandidates = new ArrayList<>();
            for (ServerPlayer player : players) {
                if (gameWorld.isRole(player, WatheRoles.CIVILIAN)) {
                    civilianCandidates.add(player);
                }
            }
            Collections.shuffle(civilianCandidates);

            List<Role> neutralRoles = new ArrayList<>();
            for (Role role : WatheRoles.ROLES) {
                if (isEnabledNeutral(role) && !Harpymodloader.VANNILA_ROLES.contains(role)) {
                    neutralRoles.add(role);
                }
            }

            for (ServerPlayer player : civilianCandidates) {
                if (assignedNeutralCount >= neutralLimit) break;
                Collections.shuffle(neutralRoles);
                Role role = brinFindAvailableNeutralRole(neutralRoles, players, gameWorld);
                if (role == null) break;

                gameWorld.addRole(player, role);
                org.agmas.harpymodloader.events.ModdedRoleAssigned.EVENT.invoker()
                    .assignModdedRole(player, role);
                assignedNeutralCount++;
            }
        }

    }

    private static boolean brinIsExplicitlyForced(ServerPlayer player) {
        return Harpymodloader.FORCED_MODDED_ROLE_FLIP.containsKey(player.getUUID());
    }

    private static Role brinFindAvailableNeutralRole(
        List<Role> roles,
        List<ServerPlayer> players,
        GameWorldComponent gameWorld
    ) {
        for (Role role : roles) {
            Integer maximum = Harpymodloader.ROLE_MAX.get(role.identifier());
            if (maximum != null && brinCountRole(players, gameWorld, role) >= maximum) continue;
            return role;
        }
        return null;
    }

    private static int brinCountRole(
        List<ServerPlayer> players,
        GameWorldComponent gameWorld,
        Role expectedRole
    ) {
        int count = 0;
        for (ServerPlayer player : players) {
            if (gameWorld.getRole(player) == expectedRole) count++;
        }
        return count;
    }

    private static int brinLimitForcedPlayers(
        List<ServerPlayer> civilianCandidates,
        GameWorldComponent gameWorld,
        int remainingSlots
    ) {
        List<Role> forcedRoles = new ArrayList<>(Harpymodloader.FORCED_MODDED_ROLE.keySet());
        for (Role role : forcedRoles) {
            if (!isEnabledNeutral(role)) continue;

            List<UUID> forcedIds = Harpymodloader.FORCED_MODDED_ROLE.get(role);
            List<UUID> retainedIds = new ArrayList<>(forcedIds.size());
            for (UUID forcedId : forcedIds) {
                ServerPlayer candidate = brinFindPlayer(civilianCandidates, forcedId);
                boolean eligible = candidate != null
                    && Harpymodloader.OVERWRITE_ROLES.contains(gameWorld.getRole(candidate));
                if (!eligible || remainingSlots > 0) {
                    retainedIds.add(forcedId);
                    if (eligible) remainingSlots--;
                    continue;
                }

                if (role.equals(Harpymodloader.FORCED_MODDED_ROLE_FLIP.get(forcedId))) {
                    Harpymodloader.FORCED_MODDED_ROLE_FLIP.remove(forcedId);
                }
            }

            if (retainedIds.isEmpty()) {
                Harpymodloader.FORCED_MODDED_ROLE.remove(role);
            } else {
                Harpymodloader.FORCED_MODDED_ROLE.put(role, retainedIds);
            }
        }
        return remainingSlots;
    }

    private static void brinLimitForcedRefreshRoles(int remainingSlots) {
        List<Role> refreshRoles = new ArrayList<>(Harpymodloader.FORCED_REFRESH_ROLES);
        for (Role role : refreshRoles) {
            if (!isEnabledNeutral(role)) continue;
            if (remainingSlots > 0) {
                remainingSlots--;
            } else {
                Harpymodloader.FORCED_REFRESH_ROLES.remove(role);
            }
        }
    }

    private static ServerPlayer brinFindPlayer(List<ServerPlayer> players, UUID playerId) {
        for (ServerPlayer player : players) {
            if (player.getUUID().equals(playerId)) return player;
        }
        return null;
    }

    private static int brinResolveNeutralLimit(
        List<ServerPlayer> players,
        GameWorldComponent gameWorld
    ) {
        int configuredCount = HarpyModLoaderConfig.HANDLER.instance().neutralRoleCount;
        if (configuredCount > 0) return configuredCount;
        if (configuredCount < 0) {
            return (int) (players.size() / Math.abs((long) configuredCount));
        }
        return players.size() / Math.max(1, gameWorld.getKillerDividend());
    }

    private static int brinCountAssignedNeutralPlayers(
        List<ServerPlayer> players,
        GameWorldComponent gameWorld
    ) {
        int count = 0;
        for (ServerPlayer player : players) {
            Role role = gameWorld.getRole(player);
            if (role != null && !role.isInnocent() && !role.canUseKiller()) {
                count++;
            }
        }
        return count;
    }

    private static boolean isEnabledNeutral(Role role) {
        return role != null
            && !role.isInnocent()
            && !role.canUseKiller()
            && !HarpyModLoaderConfig.HANDLER.instance().disabled.contains(role.identifier().toString());
    }
}

