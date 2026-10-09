package cn.erindax.brinswathe.mixin;

import cn.erindax.brinswathe.BrinBuySlots;
import cn.erindax.brinswathe.BrinShopAccess;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.util.ShopEntry;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ShopEntry.class)
public class BrinShopEntryMixin {
    @Inject(method = "insertStackInFreeSlot", at = @At("HEAD"), cancellable = true)
    private static void brinPreferBuySlot(Player player, ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (BrinBuySlots.insert(player, stack)) cir.setReturnValue(true);
    }

    @Redirect(
        method = "onBuy",
        at = @At(
            value = "INVOKE",
            target = "Ldev/doctor4t/wathe/cca/GameWorldComponent;canUseKillerFeatures(Lnet/minecraft/world/entity/player/Player;)Z"
        )
    )
    private boolean brinAllowRolePurchase(GameWorldComponent game, Player player) {
        return game.canUseKillerFeatures(player)
            || BrinShopAccess.canUseShopAndEconomy(game, player);
    }
}
