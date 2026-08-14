package cn.autoforged.brinswathe.mixin;

import cn.autoforged.brinswathe.BrinRoles;
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
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ModdedMurderGameMode.class, priority = 900)
public abstract class TrapperSurvivalExpertPairMixin {
    @Inject(method = "assignKillerReplacingRoles", at = @At("RETURN"))
    private void brinPairTrapperWithSurvivalExpert(
        int desiredRoleCount,
        ServerLevel serverLevel,
        GameWorldComponent gameWorld,
        List<ServerPlayer> players,
        CallbackInfo ci
    ) {
        boolean hasTrapper = players.stream().anyMatch(player -> gameWorld.isRole(player, BrinRoles.TRAPPER));
        boolean hasSurvivalExpert = players.stream().anyMatch(player -> gameWorld.isRole(player, BrinRoles.WATCHMAN));
        if (!hasTrapper || hasSurvivalExpert) return;

        ArrayList<ServerPlayer> candidates = new ArrayList<>();
        for (ServerPlayer player : players) {
            Role role = gameWorld.getRole(player);
            if (role == null || !role.isInnocent()) continue;
            if (Harpymodloader.FORCED_MODDED_ROLE_FLIP.containsKey(player.getUUID())) continue;
            candidates.add(player);
        }
        if (candidates.isEmpty()) return;

        Collections.shuffle(candidates);
        candidates.sort((left, right) -> Integer.compare(
            brinReplacementPriority(gameWorld.getRole(left)),
            brinReplacementPriority(gameWorld.getRole(right))
        ));
        ServerPlayer selected = candidates.getFirst();
        Role oldRole = gameWorld.getRole(selected);
        if (oldRole != null && !Harpymodloader.VANNILA_ROLES.contains(oldRole)) {
            ModdedRoleRemoved.EVENT.invoker().removeModdedRole(selected, oldRole);
        }
        gameWorld.addRole(selected, BrinRoles.WATCHMAN);
        ModdedRoleAssigned.EVENT.invoker().assignModdedRole(selected, BrinRoles.WATCHMAN);
    }

    private static int brinReplacementPriority(Role role) {
        if (role == WatheRoles.CIVILIAN) return 0;
        if (role == WatheRoles.VIGILANTE) return 2;
        return 1;
    }
}
