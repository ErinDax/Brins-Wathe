package cn.erindax.brinswathe.mixin;

import cn.erindax.brinswathe.component.ResurrectedComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = GameFunctions.class, remap = false)
public abstract class BrinResurrectedDropMixin {
    @Inject(method = "shouldDropOnDeath", at = @At("HEAD"), cancellable = true)
    private static void brinKeepResurrectedInventory(
        ItemStack stack,
        Player victim,
        CallbackInfoReturnable<Boolean> cir
    ) {
        if (victim == null) return;
        ResurrectedComponent component = ResurrectedComponent.KEY.get(victim);
        if (component != null && (component.noDropOnDeath || component.isResurrected)) {
            cir.setReturnValue(false);
        }
    }
}
