package cn.erindax.brinswathe.client.mixin;

import cn.erindax.brinswathe.client.BrinSecretMurderClient;
import dev.doctor4t.wathe.block_entity.BeveragePlateBlockEntity;
import dev.doctor4t.wathe.block_entity.TrimmedBedBlockEntity;
import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public final class BrinSecretPoisonMixin {
    private BrinSecretPoisonMixin() {
    }

    @Environment(EnvType.CLIENT)
    @Mixin(value = BeveragePlateBlockEntity.class, priority = 100)
    public abstract static class Plate {
        @Inject(method = "clientTick", at = @At("HEAD"), cancellable = true)
        private static void brinHideOthersPoison(Level world, BlockPos pos, BlockState state, BlockEntity blockEntity, CallbackInfo ci) {
            if (blockEntity instanceof BeveragePlateBlockEntity plate
                && BrinSecretMurderClient.hidesPoisonOf(world, plate.getPoisoner())) {
                ci.cancel();
            }
        }
    }

    @Environment(EnvType.CLIENT)
    @Mixin(value = TrimmedBedBlockEntity.class, priority = 100)
    public abstract static class Bed {
        @Inject(method = "clientTick", at = @At("HEAD"), cancellable = true)
        private static void brinHideOthersScorpion(Level world, BlockPos pos, BlockState state, BlockEntity blockEntity, CallbackInfo ci) {
            if (!(blockEntity instanceof TrimmedBedBlockEntity bed)) return;
            UUID poisoner = bed.getPoisoner();
            if (poisoner != null && BrinSecretMurderClient.hidesPoisonOf(world, poisoner.toString())) ci.cancel();
        }
    }
}
