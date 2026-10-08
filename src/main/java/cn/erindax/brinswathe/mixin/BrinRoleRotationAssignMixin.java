package cn.erindax.brinswathe.mixin;

import cn.erindax.brinswathe.BrinDraft;
import cn.erindax.brinswathe.BrinRoleRotation;
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
public abstract class BrinRoleRotationAssignMixin {
    @WrapOperation(
        method = "findAndAssignPlayers",
        at = @At(value = "INVOKE", target = "Ldev/doctor4t/wathe/cca/GameWorldComponent;areWeightsEnabled()Z")
    )
    private static boolean brinRotationRoleWeightsEnabled(GameWorldComponent game, Operation<Boolean> original) {
        return original.call(game) || BrinRoleRotation.active() || BrinDraft.boosting();
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
        if (BrinRoleRotation.active()) {
            List<ServerPlayer> pool = new ArrayList<>(players.size());
            for (ServerPlayer candidate : players) {
                if (Harpymodloader.FORCED_MODDED_ROLE_FLIP.containsKey(candidate.getUUID())) continue;
                if (Harpymodloader.OVERWRITE_ROLES.contains(game.getRole(candidate))) pool.add(candidate);
            }
            base = BrinRoleRotation.weight(player, BrinRoleRotation.categoryFor(role), pool, remaining);
        } else {
            base = game.areWeightsEnabled() ? original.call(exponent) : 1.0;
        }
        return base * BrinDraft.roleBoost(player, role);
    }
}
