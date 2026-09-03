package cn.erindax.brinswathe.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.doctor4t.wathe.cca.PlayerPoisonComponent;
import java.util.UUID;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = PlayerPoisonComponent.class, priority = 2000)
public abstract class BrinNullPoisonerGuardMixin {
    private static final UUID UNKNOWN_POISONER =
        UUID.fromString("00000000-0000-4000-a000-00000000b0b0");
    @WrapMethod(method = "setPoisonTicks")
    private void brinReplaceNullPoisoner(int ticks, UUID poisoner, Operation<Void> original) {
        original.call(ticks, poisoner != null ? poisoner : UNKNOWN_POISONER);
    }
}
