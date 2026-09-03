package cn.erindax.brinswathe.mixin;

import cn.erindax.brinswathe.BrinHarpyRoles;
import cn.erindax.brinswathe.BrinRoleOverwriteCleanup;
import cn.erindax.brinswathe.BrinRoles;
import com.llamalad7.mixinextras.sugar.Local;
import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.api.WatheRoles;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.agmas.harpymodloader.Harpymodloader;
import org.agmas.harpymodloader.events.ModdedRoleAssigned;
import org.agmas.harpymodloader.events.ModdedRoleRemoved;
import org.agmas.harpymodloader.modded_murder.ModdedMurderGameMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ModdedMurderGameMode.class, priority = 900)
public abstract class TrapperSurvivalExpertPairMixin {
    @Inject(
        method = "assignCivilianReplacingRoles",
        at = @At(
            value = "INVOKE",
            target = "Ljava/util/Collections;shuffle(Ljava/util/List;)V",
            ordinal = 0,
            shift = At.Shift.AFTER
        )
    )
    private void brinKeepWatchmanOutOfCivilianPool(
        int desiredRoleCount,
        ServerLevel serverLevel,
        GameWorldComponent gameWorld,
        List<ServerPlayer> players,
        CallbackInfo ci,
        @Local(index = 5) ArrayList<Role> civilianRoles
    ) {
        if (BrinHarpyRoles.isForced(BrinRoles.WATCHMAN)) return;
        civilianRoles.remove(BrinRoles.WATCHMAN);
    }
    @Inject(method = "assignKillerReplacingRoles", at = @At("RETURN"))
    private void brinPairTrapperWithSurvivalExpert(
        int desiredRoleCount,
        ServerLevel serverLevel,
        GameWorldComponent gameWorld,
        List<ServerPlayer> players,
        CallbackInfo ci
    ) {
        boolean hasTrapper = brinHasRole(players, gameWorld, BrinRoles.TRAPPER);
        boolean hasSurvivalExpert = brinHasRole(players, gameWorld, BrinRoles.WATCHMAN);
        if (hasTrapper && !hasSurvivalExpert && !BrinHarpyRoles.isDisabled(BrinRoles.WATCHMAN)) {
            brinAssignRole(gameWorld, BrinRoles.WATCHMAN, brinWatchmanCandidates(players, gameWorld));
            return;
        }
        if (hasSurvivalExpert && !hasTrapper) {
            if (BrinHarpyRoles.isForced(BrinRoles.WATCHMAN) && !BrinHarpyRoles.isDisabled(BrinRoles.TRAPPER)) {
                brinAssignRole(gameWorld, BrinRoles.TRAPPER, brinTrapperCandidates(players, gameWorld));
                return;
            }
            if (!BrinHarpyRoles.isForced(BrinRoles.WATCHMAN)) {
                brinRevertWatchmen(players, gameWorld);
            }
        }
    }
    @Unique
    private static boolean brinHasRole(List<ServerPlayer> players, GameWorldComponent gameWorld, Role role) {
        for (ServerPlayer player : players) {
            if (gameWorld.isRole(player, role)) return true;
        }
        return false;
    }

    @Unique
    private static ArrayList<ServerPlayer> brinWatchmanCandidates(
        List<ServerPlayer> players,
        GameWorldComponent gameWorld
    ) {
        ArrayList<ServerPlayer> candidates = new ArrayList<>();
        for (ServerPlayer player : players) {
            Role role = gameWorld.getRole(player);
            if (role == null || !role.isInnocent()) continue;
            if (Harpymodloader.FORCED_MODDED_ROLE_FLIP.containsKey(player.getUUID())) continue;
            candidates.add(player);
        }
        Collections.shuffle(candidates);
        candidates.sort((left, right) -> Integer.compare(
            brinReplacementPriority(gameWorld.getRole(left)),
            brinReplacementPriority(gameWorld.getRole(right))
        ));
        return candidates;
    }
    @Unique
    private static ArrayList<ServerPlayer> brinTrapperCandidates(
        List<ServerPlayer> players,
        GameWorldComponent gameWorld
    ) {
        ArrayList<ServerPlayer> candidates = new ArrayList<>();
        for (ServerPlayer player : players) {
            if (!gameWorld.isRole(player, WatheRoles.KILLER)) continue;
            if (Harpymodloader.FORCED_MODDED_ROLE_FLIP.containsKey(player.getUUID())) continue;
            candidates.add(player);
        }
        Collections.shuffle(candidates);
        return candidates;
    }
    @Unique
    private static void brinAssignRole(
        GameWorldComponent gameWorld,
        Role newRole,
        ArrayList<ServerPlayer> candidates
    ) {
        if (candidates.isEmpty()) return;
        ServerPlayer selected = candidates.getFirst();
        Role oldRole = gameWorld.getRole(selected);
        Integer startingGold = null;
        if (newRole == BrinRoles.WATCHMAN) {
            startingGold = BrinRoleOverwriteCleanup.clearReplacedInnocent(selected, oldRole);
        } else if (oldRole != null && !Harpymodloader.VANNILA_ROLES.contains(oldRole)) {
            ModdedRoleRemoved.EVENT.invoker().removeModdedRole(selected, oldRole);
        }
        gameWorld.addRole(selected, newRole);
        ModdedRoleAssigned.EVENT.invoker().assignModdedRole(selected, newRole);
        if (startingGold != null) {
            BrinRoleOverwriteCleanup.restoreStartingGold(selected, startingGold);
        }
    }
    @Unique
    private static void brinRevertWatchmen(List<ServerPlayer> players, GameWorldComponent gameWorld) {
        for (ServerPlayer player : players) {
            if (!gameWorld.isRole(player, BrinRoles.WATCHMAN)) continue;
            if (Harpymodloader.FORCED_MODDED_ROLE_FLIP.containsKey(player.getUUID())) continue;
            int startingGold = BrinRoleOverwriteCleanup.clearReplacedInnocent(player, BrinRoles.WATCHMAN);
            gameWorld.addRole(player, WatheRoles.CIVILIAN);
            BrinRoleOverwriteCleanup.restoreStartingGold(player, startingGold);
        }
    }
    @Unique
    private static int brinReplacementPriority(Role role) {
        if (role == WatheRoles.CIVILIAN) return 0;
        if (role == WatheRoles.VIGILANTE) return 2;
        return 1;
    }
}
