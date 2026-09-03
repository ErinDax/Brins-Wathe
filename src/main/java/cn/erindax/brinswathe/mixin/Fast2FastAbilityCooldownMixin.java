package cn.erindax.brinswathe.mixin;

import cn.erindax.brinswathe.BrinModifiers;
import net.minecraft.world.entity.player.Player;
import org.BsXinQin.kinswathe.component.AbilityPlayerComponent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = AbilityPlayerComponent.class, remap = false)
public abstract class Fast2FastAbilityCooldownMixin {
    @Shadow
    public int cooldown;
    @Shadow
    @Final
    private Player player;

    @Shadow
    public abstract void sync();
    @Inject(method = "setAbilityCooldown", at = @At("HEAD"), cancellable = true)
    private void brinSkipCooldownForFast2Fast(int seconds, CallbackInfo ci) {
        this.brinZeroInsteadOfCooldown(ci);
    }
    @Inject(method = "setNoellesRolesAbilityCooldown", at = @At("HEAD"), cancellable = true)
    private void brinSkipNoellCooldownForFast2Fast(int seconds, CallbackInfo ci) {
        this.brinZeroInsteadOfCooldown(ci);
    }
    @Inject(method = "serverTick", at = @At("HEAD"))
    private void brinClearRunningCooldownForFast2Fast(CallbackInfo ci) {
        if (this.cooldown <= 0) return;
        if (!BrinModifiers.hasModifier(this.player, BrinModifiers.FAST2FAST)) return;
        this.cooldown = 0;
        this.sync();
    }
    @Unique
    private void brinZeroInsteadOfCooldown(CallbackInfo ci) {
        if (this.player.level().isClientSide) return;
        if (!BrinModifiers.hasModifier(this.player, BrinModifiers.FAST2FAST)) return;
        if (this.cooldown != 0) {
            this.cooldown = 0;
        }
        this.sync();
        ci.cancel();
    }
}
