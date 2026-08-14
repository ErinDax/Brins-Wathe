package cn.autoforged.brinswathe.mixin;

import cn.autoforged.brinswathe.BrinShopAccess;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.util.ShopEntry;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ShopEntry.class)
public class BrinShopEntryMixin {
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
