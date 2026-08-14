package cn.autoforged.brinswathe.mixin;

import cn.autoforged.brinswathe.CompensatorPassive;
import cn.autoforged.brinswathe.CowboyDuel;
import dev.doctor4t.wathe.game.GameConstants;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class CompensatorReflectMixin {

    @Inject(method = "hurt", at = @At("HEAD"), cancellable = true)
    private void brinCompensatorReflect(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (!(self instanceof Player player)) return;
        if (self.level().isClientSide || CompensatorPassive.isResolvingRetaliation()) return;
        // Duel deaths are final: the compensator's life-for-a-life does not fire in the arena.
        if (CowboyDuel.isActive()) return;
        if (player.getHealth() - amount > 0) return;
        if (!(source.getEntity() instanceof Player attacker)) return;
        if (CompensatorPassive.tryHandle(
            player,
            true,
            attacker,
            GameConstants.DeathReasons.GENERIC
        )) {
            cir.setReturnValue(false);
        }
    }
}
