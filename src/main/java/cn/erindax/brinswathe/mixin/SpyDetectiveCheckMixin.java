package cn.erindax.brinswathe.mixin;

import cn.erindax.brinswathe.ModRoles;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.doctor4t.wathe.api.Role;
import org.BsXinQin.kinswathe.roles.detective.DetectiveAbility;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = DetectiveAbility.class, remap = false)
public abstract class SpyDetectiveCheckMixin {
    @WrapOperation(
        method = "register",
        at = @At(value = "INVOKE", target = "Ldev/doctor4t/wathe/api/Role;isInnocent()Z")
    )
    private static boolean brinSpyReadsAsCivilian(Role role, Operation<Boolean> original) {
        return original.call(role) || ModRoles.SPY.equals(role);
    }
}
