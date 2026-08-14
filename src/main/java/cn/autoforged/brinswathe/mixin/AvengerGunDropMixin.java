package cn.autoforged.brinswathe.mixin;

import cn.autoforged.brinswathe.BrinRoles;
import cn.autoforged.brinswathe.BrinShopAccess;
import cn.autoforged.brinswathe.CowboyDuel;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Wathe hardcodes the revolver as always dropping on death, ahead of the ShouldDropOnDeath event, so
 * the event cannot save the avenger's borrowed gun from entering circulation. Vetoing here keeps every
 * firearm an avenger dies with out of other players' hands - the gun only ever exists on loan.
 */
@Mixin(value = GameFunctions.class, remap = false)
public abstract class AvengerGunDropMixin {

    @Inject(method = "shouldDropOnDeath", at = @At("HEAD"), cancellable = true)
    private static void brinKeepAvengerGunOnLoan(
        ItemStack stack,
        Player player,
        CallbackInfoReturnable<Boolean> cir
    ) {
        if (player == null || stack.isEmpty()) return;
        // The cowboy duel's revolver is a loan too: a duelist dying with it must not leave it lying in
        // the arena for the next duel's participants.
        if (CowboyDuel.isDuelGun(stack)) {
            cir.setReturnValue(false);
            return;
        }
        if (!BrinShopAccess.isFirearm(stack)) return;
        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(player.level());
        if (gameWorld.isRole(player, BrinRoles.AVENGER)) {
            cir.setReturnValue(false);
        }
    }
}
