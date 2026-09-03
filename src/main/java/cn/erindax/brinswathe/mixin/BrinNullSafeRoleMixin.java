package cn.erindax.brinswathe.mixin;

import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import java.util.UUID;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameWorldComponent.class)
public abstract class BrinNullSafeRoleMixin {
    @Inject(
        method = "isRole(Lnet/minecraft/world/entity/player/Player;Ldev/doctor4t/wathe/api/Role;)Z",
        at = @At("HEAD"),
        cancellable = true
    )
    private void brinNullPlayerIsNotRole(Player player, Role role, CallbackInfoReturnable<Boolean> cir) {
        if (player == null) cir.setReturnValue(false);
    }

    @Inject(
        method = "isRole(Ljava/util/UUID;Ldev/doctor4t/wathe/api/Role;)Z",
        at = @At("HEAD"),
        cancellable = true
    )
    private void brinNullUuidIsNotRole(UUID uuid, Role role, CallbackInfoReturnable<Boolean> cir) {
        if (uuid == null) cir.setReturnValue(false);
    }
}
