package cn.erindax.brinswathe.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets = "org.agmas.noellesroles.bartender.BartenderPlayerComponent", remap = false)
public abstract class BartenderShieldTimeoutMixin {
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
