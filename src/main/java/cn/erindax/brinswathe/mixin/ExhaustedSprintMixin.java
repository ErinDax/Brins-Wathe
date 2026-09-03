package cn.erindax.brinswathe.mixin;

import cn.erindax.brinswathe.component.StaminaComponent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class ExhaustedSprintMixin {
    @Inject(method = "setSprinting", at = @At("HEAD"), cancellable = true)
    private void brinBlockExhaustedSprint(boolean sprinting, CallbackInfo ci) {
        if (!sprinting) return;
        if (!((Object) this instanceof Player player)) return;
        if (!StaminaComponent.usesGameStamina(player)) return;
        StaminaComponent stamina = StaminaComponent.KEY.get(player);
        if (stamina != null && !stamina.canSprint()) {
            ci.cancel();
        }
    }
}
