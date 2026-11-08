package cn.erindax.brinswathe.mixin;

import cn.erindax.brinswathe.BrinDisarm;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class BrinDisarmItemMixin {
    @Inject(method = "useOn", at = @At("HEAD"), cancellable = true)
    private void brinRejectDisarmedUseOn(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
        Player player = context.getPlayer();
        if (player != null && BrinDisarm.isDisarmed(player)) cir.setReturnValue(InteractionResult.FAIL);
    }
}
