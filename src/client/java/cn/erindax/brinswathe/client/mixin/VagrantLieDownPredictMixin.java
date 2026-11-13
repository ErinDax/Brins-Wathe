package cn.erindax.brinswathe.client.mixin;

import cn.erindax.brinswathe.Vagrant;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(MultiPlayerGameMode.class)
public abstract class VagrantLieDownPredictMixin {
    @Inject(method = "performUseItemOn", at = @At("RETURN"), cancellable = true)
    private void brinPredictLieDown(LocalPlayer player, InteractionHand hand, BlockHitResult hitResult,
                                    CallbackInfoReturnable<InteractionResult> cir) {
        if (cir.getReturnValue() == InteractionResult.PASS && hand == InteractionHand.MAIN_HAND
            && Vagrant.canLieDown(player, hitResult)) {
            cir.setReturnValue(InteractionResult.SUCCESS);
        }
    }
}
