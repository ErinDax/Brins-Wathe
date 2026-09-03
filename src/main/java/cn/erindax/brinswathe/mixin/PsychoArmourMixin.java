package cn.erindax.brinswathe.mixin;

import cn.erindax.brinswathe.PsychoArmour;
import dev.doctor4t.wathe.cca.PlayerPsychoComponent;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerPsychoComponent.class)
public abstract class PsychoArmourMixin {
    @Shadow
    @Final
    private Player player;

    @Inject(method = "startPsycho", at = @At("RETURN"))
    private void brinSetNearbyArmour(CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue() == null || !cir.getReturnValue()) return;
        ((PlayerPsychoComponent) (Object) this).setArmour(PsychoArmour.calculate(this.player));
    }
}
