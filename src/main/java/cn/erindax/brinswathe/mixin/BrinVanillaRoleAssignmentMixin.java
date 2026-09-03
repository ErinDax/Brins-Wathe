package cn.erindax.brinswathe.mixin;

import cn.erindax.brinswathe.BrinHarpyRoles;
import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.api.WatheRoles;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.ScoreboardRoleSelectorComponent;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.agmas.harpymodloader.Harpymodloader;
import org.agmas.harpymodloader.modded_murder.ModdedMurderGameMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ModdedMurderGameMode.class, priority = 1100)
public abstract class BrinVanillaRoleAssignmentMixin {
    @Inject(method = "assignVannilaRoles", at = @At("HEAD"), cancellable = true)
    private void brinAssignVanillaRoles(
        ServerLevel serverLevel,
        GameWorldComponent gameWorld,
        List<ServerPlayer> players,
        CallbackInfoReturnable<Integer> cir
    ) {
        ScoreboardRoleSelectorComponent roleSelector = ScoreboardRoleSelectorComponent.KEY.get(serverLevel.getScoreboard());
        int killerCount = BrinHarpyRoles.resolveKillerCount(players, gameWorld);
        int vigilanteCount = BrinHarpyRoles.resolveVigilanteCount(players, gameWorld);
        List<ServerPlayer> basePlayers = new ArrayList<>(players);
        basePlayers.removeIf(player -> Harpymodloader.FORCED_MODDED_ROLE_FLIP.containsKey(player.getUUID()));
        List<ServerPlayer> playersForKiller = new ArrayList<>(basePlayers);
        List<ServerPlayer> playersForVigilante = new ArrayList<>(basePlayers);
        for (ServerPlayer player : players) {
            Role forcedRole = Harpymodloader.FORCED_MODDED_ROLE_FLIP.get(player.getUUID());
            if (forcedRole == null) continue;
            if (forcedRole.canUseKiller()) {
                roleSelector.forcedKillers.add(player.getUUID());
                if (!playersForKiller.contains(player)) playersForKiller.add(player);
            } else if (forcedRole.equals(WatheRoles.VIGILANTE)) {
                roleSelector.forcedVigilantes.add(player.getUUID());
                if (!playersForVigilante.contains(player)) playersForVigilante.add(player);
            } else {
                playersForKiller.remove(player);
                playersForVigilante.remove(player);
            }
        }
        int total = roleSelector.assignKillers(serverLevel, gameWorld, playersForKiller, killerCount);
        roleSelector.assignVigilantes(serverLevel, gameWorld, playersForVigilante, vigilanteCount);
        cir.setReturnValue(total);
    }
    @Inject(method = "initializeGame", at = @At("TAIL"))
    private void brinClearForcedRefreshRoles(
        ServerLevel serverLevel,
        GameWorldComponent gameWorld,
        List<ServerPlayer> players,
        CallbackInfo ci
    ) {
        BrinHarpyRoles.FORCED_REFRESH_ROLES.clear();
    }
}
