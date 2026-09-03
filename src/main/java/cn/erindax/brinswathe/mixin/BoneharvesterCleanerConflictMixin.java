package cn.erindax.brinswathe.mixin;

import cn.erindax.brinswathe.BrinHarpyRoles;
import cn.erindax.brinswathe.BrinRoles;
import com.llamalad7.mixinextras.sugar.Local;
import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.BsXinQin.kinswathe.KinsWatheRoles;
import org.agmas.harpymodloader.modded_murder.ModdedMurderGameMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ModdedMurderGameMode.class, priority = 1100)
public abstract class BoneharvesterCleanerConflictMixin {
    @Inject(
        method = "assignKillerReplacingRoles",
        at = @At(
            value = "INVOKE",
            target = "Ljava/util/Collections;shuffle(Ljava/util/List;)V",
            shift = At.Shift.AFTER
        )
    )
    private void brinExcludeBoneharvesterAndCleaner(
        int desiredRoleCount,
        ServerLevel serverLevel,
        GameWorldComponent gameWorld,
        List<ServerPlayer> players,
        CallbackInfo ci,
        @Local(index = 5) ArrayList<Role> killerRoles
    ) {
        Role boneharvester = BrinRoles.BONEHARVESTER;
        Role cleaner = KinsWatheRoles.CLEANER;
        if (!killerRoles.contains(boneharvester) || !killerRoles.contains(cleaner)) return;

        Role roleToRemove;
        boolean boneharvesterEnabled = BrinHarpyRoles.isEnabled(boneharvester);
        boolean cleanerEnabled = BrinHarpyRoles.isEnabled(cleaner);
        if (boneharvesterEnabled != cleanerEnabled) {
            roleToRemove = boneharvesterEnabled ? cleaner : boneharvester;
        } else {
            boolean boneharvesterForced = BrinHarpyRoles.isForced(boneharvester);
            boolean cleanerForced = BrinHarpyRoles.isForced(cleaner);
            if (boneharvesterForced != cleanerForced) {
                roleToRemove = boneharvesterForced ? cleaner : boneharvester;
            } else {
                roleToRemove = killerRoles.indexOf(boneharvester) < killerRoles.indexOf(cleaner)
                    ? cleaner
                    : boneharvester;
            }
        }

        killerRoles.remove(roleToRemove);
    }
}
