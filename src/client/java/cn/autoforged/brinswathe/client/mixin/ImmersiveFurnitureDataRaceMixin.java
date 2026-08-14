package cn.autoforged.brinswathe.client.mixin;

import net.conczin.immersive_furniture.data.FurnitureData;
import net.conczin.immersive_furniture.data.FurnitureDataManager;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;

@Pseudo
@Mixin(targets = "net.conczin.immersive_furniture.block.entity.FurnitureBlockEntity", remap = false)
public abstract class ImmersiveFurnitureDataRaceMixin {
    @Shadow
    private String hash;

    @Shadow
    private FurnitureData data;

    @Overwrite
    public synchronized FurnitureData getData() {
        String pendingHash = this.hash;
        if (pendingHash != null) {
            BlockEntity blockEntity = (BlockEntity) (Object) this;
            FurnitureData loaded = blockEntity.getLevel() != null && blockEntity.getLevel().isClientSide
                ? FurnitureDataManager.getCachedData(pendingHash)
                : FurnitureDataManager.getData(pendingHash);
            if (loaded != null) {
                this.data = loaded;
                if (pendingHash.equals(this.hash)) {
                    this.hash = null;
                }
            }
        }
        return this.data;
    }
}
