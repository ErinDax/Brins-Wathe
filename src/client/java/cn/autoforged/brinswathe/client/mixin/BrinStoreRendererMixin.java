package cn.autoforged.brinswathe.client.mixin;

import cn.autoforged.brinswathe.BrinShopAccess;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.client.gui.StoreRenderer;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(StoreRenderer.class)
public class BrinStoreRendererMixin {
    @ModifyExpressionValue(
        method = "renderHud",
        at = @At(
            value = "INVOKE",
            target = "Ldev/doctor4t/wathe/cca/GameWorldComponent;canUseKillerFeatures(Lnet/minecraft/world/entity/player/Player;)Z"
        )
    )
    private static boolean brinShowRoleBalance(boolean original, @Local(argsOnly = true) LocalPlayer player) {
        GameWorldComponent game = GameWorldComponent.KEY.get(player.level());
        if (BrinShopAccess.hasNoShop(game, player)) return false;
        return original || BrinShopAccess.canUseShopAndEconomy(game, player);
    }
}
