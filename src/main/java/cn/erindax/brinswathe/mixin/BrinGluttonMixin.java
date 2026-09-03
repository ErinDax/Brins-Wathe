package cn.erindax.brinswathe.mixin;

import cn.erindax.brinswathe.BrinModifiers;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Item.class)
public abstract class BrinGluttonMixin {
    @Inject(method = "getUseDuration", at = @At("RETURN"), cancellable = true)
    private void brinGluttonEatsFaster(ItemStack stack, LivingEntity entity, CallbackInfoReturnable<Integer> cir) {
        if (!(entity instanceof Player player)) return;
        if (!BrinModifiers.hasModifier(player, BrinModifiers.GLUTTON)) return;
        UseAnim anim = stack.getUseAnimation();
        if (anim != UseAnim.EAT && anim != UseAnim.DRINK) return;
        if (cir.getReturnValueI() >= 1) cir.setReturnValue(1);
    }
}
