package cn.autoforged.brinswathe.mixin;

import cn.autoforged.brinswathe.entity.BomberMine;
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

/**
 * Bomber mines are trapper traps as far as the fangs themselves are concerned - frozen, hidden from
 * innocents and wiped between rounds - so this flag only exists to tell the two apart on contact.
 */
@Mixin(EvokerFangs.class)
public abstract class BomberMineEntityMixin implements BomberMine {
    @Unique
    private static final EntityDataAccessor<Boolean> BRIN_BOMBER_MINE =
        SynchedEntityData.defineId(EvokerFangs.class, EntityDataSerializers.BOOLEAN);

    @Inject(method = "defineSynchedData", at = @At("TAIL"))
    private void brinDefineMineData(SynchedEntityData.Builder builder, CallbackInfo ci) {
        builder.define(BRIN_BOMBER_MINE, false);
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void brinWriteMineData(CompoundTag tag, CallbackInfo ci) {
        tag.putBoolean("BrinBomberMine", this.brin$isBomberMine());
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void brinReadMineData(CompoundTag tag, CallbackInfo ci) {
        this.brin$setBomberMine(tag.getBoolean("BrinBomberMine"));
    }

    @Override
    public boolean brin$isBomberMine() {
        return ((EvokerFangs) (Object) this).getEntityData().get(BRIN_BOMBER_MINE);
    }

    @Override
    public void brin$setBomberMine(boolean mine) {
        ((EvokerFangs) (Object) this).getEntityData().set(BRIN_BOMBER_MINE, mine);
    }
}
