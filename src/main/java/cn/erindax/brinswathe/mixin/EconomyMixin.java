package cn.erindax.brinswathe.mixin;

import cn.erindax.brinswathe.ShopAccess;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.gamemode.MurderGameMode;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MurderGameMode.class)
public class EconomyMixin {
    @Redirect(
        method = "tickServerGameLoop",
        at = @At(
            value = "INVOKE",
            target = "Ldev/doctor4t/wathe/cca/GameWorldComponent;canUseKillerFeatures(Lnet/minecraft/world/entity/player/Player;)Z"
        )
    )
    private boolean brinAllowRolePassiveIncome(GameWorldComponent game, Player player) {
        return game.canUseKillerFeatures(player)
            || ShopAccess.canUseShopAndEconomy(game, player);
    }
}
