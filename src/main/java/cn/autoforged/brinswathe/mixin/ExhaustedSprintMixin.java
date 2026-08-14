package cn.autoforged.brinswathe.mixin;

import cn.autoforged.brinswathe.component.StaminaComponent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * While a player is exhausted, starting a sprint is refused outright - on the client as well, so a held
 * sprint key stops spamming start-sprint packets that would otherwise interrupt regeneration every tick.
 * The stamina value itself keeps flowing to the hud, letting the bar visibly refill during the lockout.
 * LivingEntity rather than Entity because that override is the one player sprint calls dispatch to.
 */
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
