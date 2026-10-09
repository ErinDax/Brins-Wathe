package cn.erindax.brinswathe.mixin;

import cn.erindax.brinswathe.CowboyDuel;
import cn.erindax.brinswathe.component.TrapperComponent;
import cn.erindax.brinswathe.entity.TrapperFangs;
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

    @Unique
    private int brin$trapAge;

    @Inject(method = "defineSynchedData", at = @At("TAIL"))
    private void brinDefineTrapData(SynchedEntityData.Builder builder, CallbackInfo ci) {
        builder.define(BRIN_TRAPPER_TRAP, false);
    }

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void brinKeepTrapFangsOpen(CallbackInfo ci) {
        if (!this.brin$isTrapperTrap()) return;
        ci.cancel();
        EvokerFangs self = (EvokerFangs) (Object) this;
        if (self.level().isClientSide() || CowboyDuel.isActive()) return;
        if (++this.brin$trapAge >= TrapperComponent.TRAP_LIFETIME_TICKS) self.discard();
    }

    @Inject(method = "getAnimationProgress", at = @At("HEAD"), cancellable = true)
    private void brinFreezeTrapFangsAnimation(float partialTick, CallbackInfoReturnable<Float> cir) {
        if (this.brin$isTrapperTrap()) cir.setReturnValue(0.1F);
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void brinWriteTrapData(CompoundTag tag, CallbackInfo ci) {
        tag.putBoolean("BrinTrapperTrap", this.brin$isTrapperTrap());
        tag.putInt("BrinTrapAge", this.brin$trapAge);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void brinReadTrapData(CompoundTag tag, CallbackInfo ci) {
        this.brin$setTrapperTrap(tag.getBoolean("BrinTrapperTrap"));
        this.brin$trapAge = tag.getInt("BrinTrapAge");
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
