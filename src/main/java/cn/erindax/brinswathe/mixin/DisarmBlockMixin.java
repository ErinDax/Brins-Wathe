package cn.erindax.brinswathe.mixin;

import cn.erindax.brinswathe.Disarm;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class DisarmBlockMixin {
    @Inject(method = "useItemOn", at = @At("HEAD"), cancellable = true)
    private void brinTreatDisarmedHandAsEmpty(ItemStack stack, Level level, Player player, InteractionHand hand,
                                              BlockHitResult hitResult,
                                              CallbackInfoReturnable<ItemInteractionResult> cir) {
        if (!stack.isEmpty() && Disarm.isDisarmed(player)) {
            cir.setReturnValue(ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION);
        }
    }
}
