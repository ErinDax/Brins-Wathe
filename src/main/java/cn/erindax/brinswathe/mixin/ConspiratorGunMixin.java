package cn.erindax.brinswathe.mixin;

import cn.erindax.brinswathe.BrinConspirator;
import cn.erindax.brinswathe.CowboyDuel;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.item.DerringerItem;
import dev.doctor4t.wathe.item.RevolverItem;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({RevolverItem.class, DerringerItem.class})
public abstract class ConspiratorGunMixin {
    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void brinBlockConspiratorShot(
        Level level,
        Player player,
        InteractionHand hand,
        CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir
    ) {
        if (!BrinConspirator.isConspirator(GameWorldComponent.KEY.get(level), player)) return;
        if (CowboyDuel.isDuelGun(player.getItemInHand(hand))) return;
        cir.setReturnValue(InteractionResultHolder.fail(player.getItemInHand(hand)));
    }
}
