package cn.erindax.brinswathe.mixin;

import cn.erindax.brinswathe.BrinGuesserSuspension;
import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import java.util.UUID;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameWorldComponent.class)
public abstract class BrinGuesserRoleChangeMixin {
    @Shadow
    @Final
    private Level world;

    @Inject(method = "addRole(Ljava/util/UUID;Ldev/doctor4t/wathe/api/Role;)V", at = @At("RETURN"))
    private void brinSuspendGuesserForRole(UUID playerId, Role role, CallbackInfo ci) {
        BrinGuesserSuspension.onRoleChanged(this.world, (GameWorldComponent) (Object) this, playerId, role);
    }
}
