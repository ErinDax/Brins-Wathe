package cn.erindax.brinswathe.mixin;

import cn.erindax.brinswathe.BrinRoles;
import cn.erindax.brinswathe.BrinShopAccess;
import cn.erindax.brinswathe.CowboyDuel;
import dev.doctor4t.wathe.api.WatheRoles;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = GameFunctions.class, remap = false)
public abstract class AvengerGunDropMixin {
    @Inject(method = "shouldDropOnDeath", at = @At("HEAD"), cancellable = true)
    private static void brinKeepAvengerGunOnLoan(
        ItemStack stack,
        Player player,
        CallbackInfoReturnable<Boolean> cir
    ) {
        if (player == null || stack.isEmpty()) return;
        if (CowboyDuel.isDuelGun(stack)) {
            cir.setReturnValue(false);
            return;
        }
        if (!BrinShopAccess.isFirearm(stack)) return;
        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(player.level());
        if (gameWorld.isRole(player, WatheRoles.VIGILANTE)) return;
        if (gameWorld.isRole(player, BrinRoles.AVENGER)) {
            cir.setReturnValue(false);
        }
    }
}
