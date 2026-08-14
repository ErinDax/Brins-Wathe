package cn.autoforged.brinswathe.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.api.WatheRoles;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.agmas.harpymodloader.Harpymodloader;
import org.agmas.harpymodloader.modded_murder.ModdedMurderGameMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ModdedMurderGameMode.class, priority = 1100)
public abstract class BrinForcedRoleQuotaMixin {
    @Inject(
        method = "assignVannilaRoles",
        at = @At(
            value = "INVOKE",
            target = "Lorg/agmas/harpymodloader/modded_murder/ModdedMurderGameMode;addForcedRoles(Ljava/util/List;Ldev/doctor4t/wathe/cca/ScoreboardRoleSelectorComponent;Ljava/util/List;Ljava/util/List;[I)V",
            shift = At.Shift.AFTER
        )
    )
    private void brinRestoreForcedRoleSlots(
        ServerLevel serverLevel,
        GameWorldComponent gameWorld,
        List<ServerPlayer> players,
        CallbackInfoReturnable<Integer> cir,
        @Local(index = 12) int[] requestedRoleCounts
    ) {
        for (ServerPlayer player : players) {
            Role forcedRole = Harpymodloader.FORCED_MODDED_ROLE_FLIP.get(player.getUUID());
            if (forcedRole == null) continue;
            if (forcedRole.canUseKiller()) {
                requestedRoleCounts[0]++;
            } else if (forcedRole.equals(WatheRoles.VIGILANTE)) {
                requestedRoleCounts[1]++;
            }
        }
    }
}
