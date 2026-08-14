package cn.autoforged.brinswathe.mixin;

import cn.autoforged.brinswathe.BrinShopAccess;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(GameFunctions.class)
public class BrinKillRewardMixin {
    @Redirect(
        method = "killPlayer(Lnet/minecraft/world/entity/player/Player;ZLnet/minecraft/world/entity/player/Player;Lnet/minecraft/resources/ResourceLocation;)V",
        at = @At(
            value = "INVOKE",
            target = "Ldev/doctor4t/wathe/cca/GameWorldComponent;canUseKillerFeatures(Lnet/minecraft/world/entity/player/Player;)Z"
        )
    )
    private static boolean brinAllowRoleKillReward(GameWorldComponent game, Player player) {
        return game.canUseKillerFeatures(player)
            || BrinShopAccess.canUseShopAndEconomy(game, player);
    }
}
