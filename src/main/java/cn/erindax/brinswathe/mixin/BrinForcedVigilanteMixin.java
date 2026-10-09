package cn.erindax.brinswathe.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.api.WatheRoles;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.ScoreboardRoleSelectorComponent;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ScoreboardRoleSelectorComponent.class)
public abstract class BrinForcedVigilanteMixin {
    @WrapOperation(
        method = "assignVigilantes",
        at = @At(
            value = "INVOKE",
            target = "Ldev/doctor4t/wathe/cca/GameWorldComponent;isRole(Lnet/minecraft/world/entity/player/Player;Ldev/doctor4t/wathe/api/Role;)Z"
        )
    )
    private boolean brinSkipAssignedVigilantes(
        GameWorldComponent game,
        Player player,
        Role role,
        Operation<Boolean> original
    ) {
        return original.call(game, player, role) || game.isRole(player, WatheRoles.VIGILANTE);
    }
}
