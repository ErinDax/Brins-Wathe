package cn.autoforged.brinswathe.mixin;

import cn.autoforged.brinswathe.entity.ArchivistSealedCorpse;
import cn.autoforged.brinswathe.entity.BoneharvestedCorpse;
import dev.doctor4t.wathe.entity.PlayerBodyEntity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class ArchivistSealMixin {

    @Inject(method = "hurt", at = @At("HEAD"), cancellable = true)
    private void brinPreventSealedDissolve(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (self instanceof PlayerBodyEntity body
            && (((ArchivistSealedCorpse) body).brin$isArchivistSealed()
                || ((BoneharvestedCorpse) body).brin$isBoneharvested())) {
            cir.setReturnValue(false);
        }
    }
}
