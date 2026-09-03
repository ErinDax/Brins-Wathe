package cn.erindax.brinswathe.mixin;

import cn.erindax.brinswathe.BrinRoles;
import cn.erindax.brinswathe.CowboyDuel;
import cn.erindax.brinswathe.component.BerserkerComponent;

import dev.doctor4t.wathe.cca.GameWorldComponent;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class BerserkerInvincibleMixin {

    @Inject(method = "hurt", at = @At("HEAD"), cancellable = true)
    private void brinBerserkerInvincible(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (!(self instanceof Player player)) return;
        if (self.level().isClientSide) return;
        if (CowboyDuel.isActive()) return;

        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(player.level());
        if (!gameWorld.isRole(player, BrinRoles.BERSERKER)) return;

        BerserkerComponent berserker = BerserkerComponent.KEY.get(player);
        if (berserker != null && berserker.psychoActive) {
            cir.setReturnValue(false);
        }
    }
}
