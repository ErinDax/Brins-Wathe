package cn.erindax.brinswathe.mixin;

import cn.erindax.brinswathe.Vagrant;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerGameMode.class)
public abstract class VagrantLieDownMixin {
    @Inject(method = "useItemOn", at = @At("RETURN"), cancellable = true)
    private void brinLieDownOnGround(ServerPlayer player, Level level, ItemStack stack, InteractionHand hand,
                                     BlockHitResult hitResult, CallbackInfoReturnable<InteractionResult> cir) {
        if (cir.getReturnValue() == InteractionResult.PASS && hand == InteractionHand.MAIN_HAND
            && Vagrant.lieDown(player, hitResult)) {
            cir.setReturnValue(InteractionResult.CONSUME);
        }
    }
}
