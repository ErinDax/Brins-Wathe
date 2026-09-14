package cn.erindax.brinswathe.mixin;

import cn.erindax.brinswathe.CowboyDuel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = dev.doctor4t.wathe.game.GameFunctions.class, remap = false)
public abstract class AvengerGunDropMixin {
    @Inject(method = "shouldDropOnDeath", at = @At("HEAD"), cancellable = true)
    private static void brinKeepDuelGun(
        ItemStack stack,
        Player player,
        CallbackInfoReturnable<Boolean> cir
    ) {
        if (player == null || stack.isEmpty()) return;
        if (CowboyDuel.isDuelGun(stack)) {
            cir.setReturnValue(false);
        }
    }
}
