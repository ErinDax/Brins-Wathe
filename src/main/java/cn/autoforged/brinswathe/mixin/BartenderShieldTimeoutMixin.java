package cn.autoforged.brinswathe.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets = "org.agmas.noellesroles.bartender.BartenderPlayerComponent", remap = false)
public abstract class BartenderShieldTimeoutMixin {
    /**
     * NoellesRoles documents {@code defenseMaximumTime = -1} as "infinite", but its expiration check only
     * skips when the value is exactly {@code 1}, so the shipped default expires the shield one tick after it
     * is granted. Mapping every non-positive value onto that sentinel restores the documented behaviour while
     * leaving real, positive timeouts untouched.
     *
     * <p>Older NoellesRoles builds have no shield timeout at all; the injection is optional so those versions
     * keep loading.
     */
    @ModifyExpressionValue(
        method = "serverTick",
        at = @At(
            value = "FIELD",
            target = "Lorg/agmas/noellesroles/config/NoellesRolesConfig;defenseMaximumTime:I",
            remap = false
        ),
        require = 0,
        remap = false
    )
    private int brinTreatNonPositiveTimeoutAsInfinite(int original) {
        return original <= 0 ? 1 : original;
    }
}
