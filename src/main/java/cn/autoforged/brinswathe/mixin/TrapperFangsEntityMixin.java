package cn.autoforged.brinswathe.mixin;

import cn.autoforged.brinswathe.entity.TrapperFangs;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.projectile.EvokerFangs;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EvokerFangs.class)
public abstract class TrapperFangsEntityMixin implements TrapperFangs {
    @Unique
    private static final EntityDataAccessor<Boolean> BRIN_TRAPPER_TRAP =
        SynchedEntityData.defineId(EvokerFangs.class, EntityDataSerializers.BOOLEAN);

    @Inject(method = "defineSynchedData", at = @At("TAIL"))
    private void brinDefineTrapData(SynchedEntityData.Builder builder, CallbackInfo ci) {
        builder.define(BRIN_TRAPPER_TRAP, false);
    }

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void brinKeepTrapFangsOpen(CallbackInfo ci) {
        if (!this.brin$isTrapperTrap()) return;
        ci.cancel();
    }

    @Inject(method = "getAnimationProgress", at = @At("HEAD"), cancellable = true)
    private void brinFreezeTrapFangsAnimation(float partialTick, CallbackInfoReturnable<Float> cir) {
        if (this.brin$isTrapperTrap()) cir.setReturnValue(0.1F);
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void brinWriteTrapData(CompoundTag tag, CallbackInfo ci) {
        tag.putBoolean("BrinTrapperTrap", this.brin$isTrapperTrap());
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void brinReadTrapData(CompoundTag tag, CallbackInfo ci) {
        this.brin$setTrapperTrap(tag.getBoolean("BrinTrapperTrap"));
    }

    @Override
    public boolean brin$isTrapperTrap() {
        return ((EvokerFangs) (Object) this).getEntityData().get(BRIN_TRAPPER_TRAP);
    }

    @Override
    public void brin$setTrapperTrap(boolean trap) {
        ((EvokerFangs) (Object) this).getEntityData().set(BRIN_TRAPPER_TRAP, trap);
    }
}
