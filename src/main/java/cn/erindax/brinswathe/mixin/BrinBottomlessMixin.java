package cn.erindax.brinswathe.mixin;

import cn.erindax.brinswathe.BrinModifiers;
import dev.doctor4t.wathe.block.FoodPlatterBlock;
import dev.doctor4t.wathe.block_entity.BeveragePlateBlockEntity;
import dev.doctor4t.wathe.index.WatheDataComponentTypes;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FoodPlatterBlock.class)
public abstract class BrinBottomlessMixin {
    @Inject(method = {"useWithoutItem", "onUse"}, at = @At("HEAD"), cancellable = true, require = 0)
    private void brinBottomlessTakeAgain(
        BlockState state,
        Level level,
        BlockPos pos,
        Player player,
        BlockHitResult hit,
        CallbackInfoReturnable<InteractionResult> cir
    ) {
        if (player.isCreative()) return;
        if (!player.getMainHandItem().isEmpty()) return;
        if (!BrinModifiers.hasModifier(player, BrinModifiers.BOTTOMLESS)) return;
        if (level.isClientSide) {
            cir.setReturnValue(InteractionResult.SUCCESS);
            return;
        }
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof BeveragePlateBlockEntity plate)) {
            cir.setReturnValue(InteractionResult.PASS);
            return;
        }
        List<ItemStack> platter = plate.getStoredItems();
        if (platter.isEmpty()) {
            cir.setReturnValue(InteractionResult.SUCCESS);
            return;
        }
        ItemStack randomItem = platter.get(level.random.nextInt(platter.size())).copy();
        randomItem.setCount(1);
        randomItem.set(DataComponents.MAX_STACK_SIZE, 1);
        String poisoner = plate.getPoisoner();
        if (poisoner != null) {
            randomItem.set(WatheDataComponentTypes.POISONER, poisoner);
            plate.setPoisoner(null);
        }
        player.playNotifySound(SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 1.0F, 1.0F);
        player.setItemInHand(InteractionHand.MAIN_HAND, randomItem);
        cir.setReturnValue(InteractionResult.SUCCESS);
    }
}
