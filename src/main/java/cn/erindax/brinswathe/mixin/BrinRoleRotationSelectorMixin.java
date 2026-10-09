package cn.erindax.brinswathe.mixin;

import cn.erindax.brinswathe.BrinDraft;
import cn.erindax.brinswathe.BrinRoleRotation;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import dev.doctor4t.wathe.api.WatheRoles;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.ScoreboardRoleSelectorComponent;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = ScoreboardRoleSelectorComponent.class, remap = false)
public abstract class BrinRoleRotationSelectorMixin {
    @WrapOperation(
        method = {"assignKillers", "assignVigilantes"},
        at = @At(value = "INVOKE", target = "Ldev/doctor4t/wathe/cca/GameWorldComponent;areWeightsEnabled()Z")
    )
    private boolean brinRotationWeightsEnabled(GameWorldComponent game, Operation<Boolean> original) {
        return original.call(game) || BrinRoleRotation.active() || BrinDraft.boosting();
    }

    @WrapOperation(method = "assignKillers", at = @At(value = "INVOKE", target = "Ljava/lang/Math;exp(D)D"))
    private double brinRotationKillerWeight(
        double exponent,
        Operation<Double> original,
        @Local ServerPlayer player,
        @Local(argsOnly = true) GameWorldComponent game,
        @Local(argsOnly = true) List<ServerPlayer> players,
        @Local(argsOnly = true) int killerCount
    ) {
        double base = BrinRoleRotation.active()
            ? BrinRoleRotation.weight(player, BrinRoleRotation.KILLER, players, killerCount)
            : brinBaseWeight(game, exponent, original);
        return base * BrinDraft.killerBoost(player);
    }

    @WrapOperation(method = "assignVigilantes", at = @At(value = "INVOKE", target = "Ljava/lang/Math;exp(D)D"))
    private double brinRotationVigilanteWeight(
        double exponent,
        Operation<Double> original,
        @Local ServerPlayer player,
        @Local(argsOnly = true) GameWorldComponent game,
        @Local(argsOnly = true) List<ServerPlayer> players,
        @Local(argsOnly = true) int vigilanteCount
    ) {
        double base;
        if (BrinRoleRotation.active()) {
            List<ServerPlayer> pool = new ArrayList<>(players.size());
            for (ServerPlayer candidate : players) {
                if (!game.isRole(candidate, WatheRoles.KILLER) && !game.isRole(candidate, WatheRoles.VIGILANTE)) {
                    pool.add(candidate);
                }
            }
            base = BrinRoleRotation.weight(player, BrinRoleRotation.VIGILANTE, pool, vigilanteCount);
        } else {
            base = brinBaseWeight(game, exponent, original);
        }
        return base * BrinDraft.vigilanteBoost(player);
    }

    @Unique
    private static double brinBaseWeight(GameWorldComponent game, double exponent, Operation<Double> original) {
        return game.areWeightsEnabled() ? original.call(exponent) : 1.0;
    }
}
