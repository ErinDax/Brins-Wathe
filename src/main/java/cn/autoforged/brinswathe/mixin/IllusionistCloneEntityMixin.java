package cn.autoforged.brinswathe.mixin;

import cn.autoforged.brinswathe.entity.ArchivistSealedCorpse;
import cn.autoforged.brinswathe.entity.BoneharvestedCorpse;
import cn.autoforged.brinswathe.entity.IllusionistCloneEntity;
import cn.autoforged.brinswathe.entity.MorticianDisguiseBody;
import cn.autoforged.brinswathe.entity.PuppetEntity;
import dev.doctor4t.wathe.entity.PlayerBodyEntity;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerBodyEntity.class)
public abstract class IllusionistCloneEntityMixin
    implements IllusionistCloneEntity, ArchivistSealedCorpse, BoneharvestedCorpse, PuppetEntity,
    MorticianDisguiseBody {
    @Unique
    private static final String BRIN_BODYMAKER_FORGED_NBT = "BrinBodymakerForged";

    @Unique
    private static final String BRIN_ARCHIVIST_SEALED_NBT = "BrinArchivistSealed";

    @Unique
    private static final String BRIN_BONEHARVESTED_NBT = "BrinBoneharvested";

    @Unique
    private static final EntityDataAccessor<Boolean> BRIN_BODYMAKER_FORGED =
        SynchedEntityData.defineId(PlayerBodyEntity.class, EntityDataSerializers.BOOLEAN);

    @Unique
    private static final EntityDataAccessor<Boolean> BRIN_ARCHIVIST_SEALED =
        SynchedEntityData.defineId(PlayerBodyEntity.class, EntityDataSerializers.BOOLEAN);

    @Unique
    private static final EntityDataAccessor<Boolean> BRIN_BONEHARVESTED =
        SynchedEntityData.defineId(PlayerBodyEntity.class, EntityDataSerializers.BOOLEAN);

    @Unique
    private static final String BRIN_ILLUSION_CLONE_NBT = "BrinIllusionClone";

    @Unique
    private static final String BRIN_ILLUSION_BODY_PROXY_NBT = "BrinIllusionBodyProxy";

    @Unique
    private static final EntityDataAccessor<Boolean> BRIN_ILLUSION_CLONE =
        SynchedEntityData.defineId(PlayerBodyEntity.class, EntityDataSerializers.BOOLEAN);

    @Unique
    private static final EntityDataAccessor<Boolean> BRIN_ILLUSION_BODY_PROXY =
        SynchedEntityData.defineId(PlayerBodyEntity.class, EntityDataSerializers.BOOLEAN);

    @Unique
    private static final String BRIN_PUPPET_NBT = "BrinPuppet";

    @Unique
    private static final String BRIN_PUPPET_BODY_PROXY_NBT = "BrinPuppetBodyProxy";

    @Unique
    private static final String BRIN_PUPPETEER_NBT = "BrinPuppeteer";

    @Unique
    private static final EntityDataAccessor<Boolean> BRIN_PUPPET =
        SynchedEntityData.defineId(PlayerBodyEntity.class, EntityDataSerializers.BOOLEAN);

    @Unique
    private static final EntityDataAccessor<Boolean> BRIN_PUPPET_BODY_PROXY =
        SynchedEntityData.defineId(PlayerBodyEntity.class, EntityDataSerializers.BOOLEAN);

    @Unique
    private static final EntityDataAccessor<Optional<UUID>> BRIN_PUPPETEER =
        SynchedEntityData.defineId(PlayerBodyEntity.class, EntityDataSerializers.OPTIONAL_UUID);

    @Unique
    private static final String BRIN_MORTICIAN_DISGUISE_NBT = "BrinMorticianDisguise";

    @Unique
    private static final String BRIN_MORTICIAN_NBT = "BrinMortician";

    @Unique
    private static final EntityDataAccessor<Boolean> BRIN_MORTICIAN_DISGUISE =
        SynchedEntityData.defineId(PlayerBodyEntity.class, EntityDataSerializers.BOOLEAN);

    @Unique
    private static final EntityDataAccessor<Optional<UUID>> BRIN_MORTICIAN =
        SynchedEntityData.defineId(PlayerBodyEntity.class, EntityDataSerializers.OPTIONAL_UUID);

    @Inject(method = "getArmorSlots", at = @At("HEAD"), cancellable = true)
    private void brinProvideEmptyArmorSlots(CallbackInfoReturnable<Iterable<ItemStack>> cir) {
        cir.setReturnValue(Collections.emptyList());
    }

    @Inject(method = "defineSynchedData", at = @At("TAIL"))
    private void brinDefineIllusionCloneData(SynchedEntityData.Builder builder, CallbackInfo ci) {
        builder.define(BRIN_BODYMAKER_FORGED, false);
        builder.define(BRIN_ARCHIVIST_SEALED, false);
        builder.define(BRIN_BONEHARVESTED, false);
        builder.define(BRIN_ILLUSION_CLONE, false);
        builder.define(BRIN_ILLUSION_BODY_PROXY, false);
        builder.define(BRIN_PUPPET, false);
        builder.define(BRIN_PUPPET_BODY_PROXY, false);
        builder.define(BRIN_PUPPETEER, Optional.empty());
        builder.define(BRIN_MORTICIAN_DISGUISE, false);
        builder.define(BRIN_MORTICIAN, Optional.empty());
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void brinSaveIllusionCloneData(CompoundTag tag, CallbackInfo ci) {
        tag.putBoolean(BRIN_BODYMAKER_FORGED_NBT, this.brin$isBodymakerForged());
        tag.putBoolean(BRIN_ARCHIVIST_SEALED_NBT, this.brin$isArchivistSealed());
        tag.putBoolean(BRIN_BONEHARVESTED_NBT, this.brin$isBoneharvested());
        tag.putBoolean(BRIN_ILLUSION_CLONE_NBT, this.brin$isIllusionClone());
        tag.putBoolean(BRIN_ILLUSION_BODY_PROXY_NBT, this.brin$isIllusionBodyProxy());
        tag.putBoolean(BRIN_PUPPET_NBT, this.brin$isPuppet());
        tag.putBoolean(BRIN_PUPPET_BODY_PROXY_NBT, this.brin$isPuppetBodyProxy());
        UUID puppeteer = this.brin$getPuppeteer();
        if (puppeteer != null) tag.putUUID(BRIN_PUPPETEER_NBT, puppeteer);
        tag.putBoolean(BRIN_MORTICIAN_DISGUISE_NBT, this.brin$isMorticianDisguise());
        UUID mortician = this.brin$getMortician();
        if (mortician != null) tag.putUUID(BRIN_MORTICIAN_NBT, mortician);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void brinReadIllusionCloneData(CompoundTag tag, CallbackInfo ci) {
        this.brin$setBodymakerForged(tag.getBoolean(BRIN_BODYMAKER_FORGED_NBT));
        this.brin$setArchivistSealed(tag.getBoolean(BRIN_ARCHIVIST_SEALED_NBT));
        this.brin$setBoneharvested(tag.getBoolean(BRIN_BONEHARVESTED_NBT));
        this.brin$setIllusionClone(tag.getBoolean(BRIN_ILLUSION_CLONE_NBT));
        this.brin$setIllusionBodyProxy(tag.getBoolean(BRIN_ILLUSION_BODY_PROXY_NBT));
        this.brin$setPuppet(tag.getBoolean(BRIN_PUPPET_NBT));
        this.brin$setPuppetBodyProxy(tag.getBoolean(BRIN_PUPPET_BODY_PROXY_NBT));
        this.brin$setPuppeteer(tag.hasUUID(BRIN_PUPPETEER_NBT) ? tag.getUUID(BRIN_PUPPETEER_NBT) : null);
        this.brin$setMorticianDisguise(tag.getBoolean(BRIN_MORTICIAN_DISGUISE_NBT));
        this.brin$setMortician(tag.hasUUID(BRIN_MORTICIAN_NBT) ? tag.getUUID(BRIN_MORTICIAN_NBT) : null);
    }

    @Override
    public boolean brin$isMorticianDisguise() {
        return ((PlayerBodyEntity) (Object) this).getEntityData().get(BRIN_MORTICIAN_DISGUISE);
    }

    @Override
    public void brin$setMorticianDisguise(boolean disguise) {
        ((PlayerBodyEntity) (Object) this).getEntityData().set(BRIN_MORTICIAN_DISGUISE, disguise);
    }

    @Override
    @Nullable
    public UUID brin$getMortician() {
        return ((PlayerBodyEntity) (Object) this).getEntityData().get(BRIN_MORTICIAN).orElse(null);
    }

    @Override
    public void brin$setMortician(@Nullable UUID mortician) {
        ((PlayerBodyEntity) (Object) this).getEntityData().set(BRIN_MORTICIAN, Optional.ofNullable(mortician));
    }

    @Override
    public boolean brin$isPuppet() {
        return ((PlayerBodyEntity) (Object) this).getEntityData().get(BRIN_PUPPET);
    }

    @Override
    public void brin$setPuppet(boolean puppet) {
        ((PlayerBodyEntity) (Object) this).getEntityData().set(BRIN_PUPPET, puppet);
    }

    @Override
    public boolean brin$isPuppetBodyProxy() {
        return ((PlayerBodyEntity) (Object) this).getEntityData().get(BRIN_PUPPET_BODY_PROXY);
    }

    @Override
    public void brin$setPuppetBodyProxy(boolean bodyProxy) {
        ((PlayerBodyEntity) (Object) this).getEntityData().set(BRIN_PUPPET_BODY_PROXY, bodyProxy);
    }

    @Override
    @Nullable
    public UUID brin$getPuppeteer() {
        return ((PlayerBodyEntity) (Object) this).getEntityData().get(BRIN_PUPPETEER).orElse(null);
    }

    @Override
    public void brin$setPuppeteer(@Nullable UUID puppeteer) {
        ((PlayerBodyEntity) (Object) this).getEntityData().set(BRIN_PUPPETEER, Optional.ofNullable(puppeteer));
    }

    @Override
    public boolean brin$isIllusionClone() {
        return ((PlayerBodyEntity) (Object) this).getEntityData().get(BRIN_ILLUSION_CLONE);
    }

    @Override
    public void brin$setIllusionClone(boolean clone) {
        ((PlayerBodyEntity) (Object) this).getEntityData().set(BRIN_ILLUSION_CLONE, clone);
    }

    @Override
    public boolean brin$isIllusionBodyProxy() {
        return ((PlayerBodyEntity) (Object) this).getEntityData().get(BRIN_ILLUSION_BODY_PROXY);
    }

    @Override
    public void brin$setIllusionBodyProxy(boolean bodyProxy) {
        ((PlayerBodyEntity) (Object) this).getEntityData().set(BRIN_ILLUSION_BODY_PROXY, bodyProxy);
    }

    @Override
    public boolean brin$isBodymakerForged() {
        return ((PlayerBodyEntity) (Object) this).getEntityData().get(BRIN_BODYMAKER_FORGED);
    }

    @Override
    public void brin$setBodymakerForged(boolean forged) {
        ((PlayerBodyEntity) (Object) this).getEntityData().set(BRIN_BODYMAKER_FORGED, forged);
    }

    @Override
    public boolean brin$isArchivistSealed() {
        return ((PlayerBodyEntity) (Object) this).getEntityData().get(BRIN_ARCHIVIST_SEALED);
    }

    @Override
    public void brin$setArchivistSealed(boolean sealed) {
        ((PlayerBodyEntity) (Object) this).getEntityData().set(BRIN_ARCHIVIST_SEALED, sealed);
    }

    @Override
    public boolean brin$isBoneharvested() {
        return ((PlayerBodyEntity) (Object) this).getEntityData().get(BRIN_BONEHARVESTED);
    }

    @Override
    public void brin$setBoneharvested(boolean boneharvested) {
        ((PlayerBodyEntity) (Object) this).getEntityData().set(BRIN_BONEHARVESTED, boneharvested);
    }
}
