package cn.erindax.brinswathe.mixin;

import cn.erindax.brinswathe.component.MuzzlerAbilityComponent;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class BrinMuzzlerSilentMixin {
    @ModifyReturnValue(method = "isSilent", at = @At("RETURN"))
    private boolean brinMuzzlerAbilitySilent(boolean original) {
        return original || MuzzlerAbilityComponent.isActive((Entity) (Object) this);
    }

    @Inject(method = "playSound(Lnet/minecraft/sounds/SoundEvent;FF)V", at = @At("HEAD"), cancellable = true)
    private void brinCancelMutedPlaySound(SoundEvent sound, float volume, float pitch, CallbackInfo ci) {
        if (MuzzlerAbilityComponent.isActive((Entity) (Object) this)) {
            ci.cancel();
        }
    }
}
