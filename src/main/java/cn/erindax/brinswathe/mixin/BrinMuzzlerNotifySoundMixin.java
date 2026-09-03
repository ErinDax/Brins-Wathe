package cn.erindax.brinswathe.mixin;

import cn.erindax.brinswathe.component.MuzzlerAbilityComponent;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class BrinMuzzlerNotifySoundMixin {
    @Inject(method = "playNotifySound", at = @At("HEAD"), cancellable = true)
    private void brinCancelMutedNotifySound(
        SoundEvent sound,
        SoundSource source,
        float volume,
        float pitch,
        CallbackInfo ci
    ) {
        if (MuzzlerAbilityComponent.isActive((Player) (Object) this)) {
            ci.cancel();
        }
    }
}
