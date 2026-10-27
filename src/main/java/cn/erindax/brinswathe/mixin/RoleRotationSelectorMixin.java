package cn.erindax.brinswathe.mixin;

import cn.erindax.brinswathe.RoleRotation;
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
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = ScoreboardRoleSelectorComponent.class, remap = false)
public abstract class RoleRotationSelectorMixin {
    @WrapOperation(
        method = {"assignKillers", "assignVigilantes"},
        at = @At(value = "INVOKE", target = "Ldev/doctor4t/wathe/cca/GameWorldComponent;areWeightsEnabled()Z")
    )
    private boolean brinRotationWeightsEnabled(GameWorldComponent game, Operation<Boolean> original) {
        return original.call(game) || RoleRotation.active();
    }

    @WrapOperation(method = "assignKillers", at = @At(value = "INVOKE", target = "Ljava/lang/Math;exp(D)D"))
    private double brinRotationKillerWeight(
        double exponent,
        Operation<Double> original,
        @Local ServerPlayer player,
        @Local(argsOnly = true) List<ServerPlayer> players,
        @Local(argsOnly = true) int killerCount
    ) {
        if (!RoleRotation.active()) return original.call(exponent);
        return RoleRotation.weight(player, RoleRotation.KILLER, players, killerCount);
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
        if (!RoleRotation.active()) return original.call(exponent);
        List<ServerPlayer> pool = new ArrayList<>(players.size());
        for (ServerPlayer candidate : players) {
            if (!game.isRole(candidate, WatheRoles.KILLER) && !game.isRole(candidate, WatheRoles.VIGILANTE)) {
                pool.add(candidate);
            }
        }
        return RoleRotation.weight(player, RoleRotation.VIGILANTE, pool, vigilanteCount);
    }
}
