package cn.erindax.brinswathe.mixin;

import cn.erindax.brinswathe.BrinHarpyRoles;
import com.llamalad7.mixinextras.sugar.Local;
import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.agmas.harpymodloader.modded_murder.ModdedMurderGameMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ModdedMurderGameMode.class, priority = 1200)
public abstract class BrinForcedRefreshRoleMixin {
    @Inject(
        method = "assignCivilianReplacingRoles",
        at = @At(
            value = "INVOKE",
            target = "Ljava/util/ArrayList;iterator()Ljava/util/Iterator;",
            ordinal = 1
        )
    )
    private void brinRefreshNeutralRoles(
        int desiredRoleCount,
        ServerLevel serverLevel,
        GameWorldComponent gameWorld,
        List<ServerPlayer> players,
        CallbackInfo ci,
        @Local(index = 6) ArrayList<Role> shuffledNeutralRoles,
        @Local(index = 7) ArrayList<ServerPlayer> civilianCandidates
    ) {
        BrinHarpyRoles.assignForcedRefresh(
            shuffledNeutralRoles,
            civilianCandidates,
            gameWorld,
            serverLevel,
            role -> !role.canUseKiller() && !role.isInnocent()
        );
    }

    @Inject(
        method = "assignCivilianReplacingRoles",
        at = @At(
            value = "INVOKE",
            target = "Ljava/util/ArrayList;iterator()Ljava/util/Iterator;",
            ordinal = 2
        )
    )
    private void brinRefreshCivilianRoles(
        int desiredRoleCount,
        ServerLevel serverLevel,
        GameWorldComponent gameWorld,
        List<ServerPlayer> players,
        CallbackInfo ci,
        @Local(index = 5) ArrayList<Role> shuffledCivilianRoles,
        @Local(index = 7) ArrayList<ServerPlayer> civilianCandidates
    ) {
        BrinHarpyRoles.assignForcedRefresh(
            shuffledCivilianRoles,
            civilianCandidates,
            gameWorld,
            serverLevel,
            role -> !role.canUseKiller() && role.isInnocent()
        );
    }

    @Inject(
        method = "assignKillerReplacingRoles",
        at = @At(
            value = "INVOKE",
            target = "Ljava/util/Collections;shuffle(Ljava/util/List;)V",
            shift = At.Shift.AFTER
        )
    )
    private void brinRefreshKillerRoles(
        int desiredRoleCount,
        ServerLevel serverLevel,
        GameWorldComponent gameWorld,
        List<ServerPlayer> players,
        CallbackInfo ci,
        @Local(index = 5) ArrayList<Role> shuffledKillerRoles,
        @Local(index = 6) ArrayList<ServerPlayer> killerCandidates
    ) {
        BrinHarpyRoles.assignForcedRefresh(
            shuffledKillerRoles,
            killerCandidates,
            gameWorld,
            serverLevel,
            Role::canUseKiller
        );
    }
}
