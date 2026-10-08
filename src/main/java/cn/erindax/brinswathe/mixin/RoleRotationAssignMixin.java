package cn.erindax.brinswathe.mixin;

import cn.erindax.brinswathe.Draft;
import cn.erindax.brinswathe.RoleRotation;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.level.ServerPlayer;
import org.agmas.harpymodloader.Harpymodloader;
import org.agmas.harpymodloader.modded_murder.ModdedMurderGameMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = ModdedMurderGameMode.class, remap = false)
public abstract class RoleRotationAssignMixin {
    @WrapOperation(
        method = "findAndAssignPlayers",
        at = @At(value = "INVOKE", target = "Ldev/doctor4t/wathe/cca/GameWorldComponent;areWeightsEnabled()Z")
    )
    private static boolean brinRotationRoleWeightsEnabled(GameWorldComponent game, Operation<Boolean> original) {
        return original.call(game) || RoleRotation.active() || Draft.boosting();
    }

    @WrapOperation(method = "findAndAssignPlayers", at = @At(value = "INVOKE", target = "Ljava/lang/Math;exp(D)D"))
    private static double brinRotationRoleWeight(
        double exponent,
        Operation<Double> original,
        @Local ServerPlayer player,
        @Local(argsOnly = true) int remaining,
        @Local(argsOnly = true) Role role,
        @Local(argsOnly = true) List<ServerPlayer> players,
        @Local(argsOnly = true) GameWorldComponent game
    ) {
        double base;
        if (RoleRotation.active()) {
            List<ServerPlayer> pool = new ArrayList<>(players.size());
            for (ServerPlayer candidate : players) {
                if (Harpymodloader.FORCED_MODDED_ROLE_FLIP.containsKey(candidate.getUUID())) continue;
                if (Harpymodloader.OVERWRITE_ROLES.contains(game.getRole(candidate))) pool.add(candidate);
            }
            base = RoleRotation.weight(player, RoleRotation.categoryFor(role), pool, remaining);
        } else {
            base = game.areWeightsEnabled() ? original.call(exponent) : 1.0;
        }
        return base * Draft.roleBoost(player, role);
    }
}
