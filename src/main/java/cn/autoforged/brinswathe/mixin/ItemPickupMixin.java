package cn.autoforged.brinswathe.mixin;

import cn.autoforged.brinswathe.BrinRoles;
import cn.autoforged.brinswathe.BrinShopAccess;
import cn.autoforged.brinswathe.component.AvengerComponent;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.index.WatheItems;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public abstract class ItemPickupMixin {

    @Shadow
    public abstract net.minecraft.world.item.ItemStack getItem();

    @Inject(method = "playerTouch", at = @At("HEAD"), cancellable = true)
    private void brinPreventRevolverPickup(Player player, CallbackInfo ci) {
        if (player.level().isClientSide) return;

        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(player.level());
        if (gameWorld.isRole(player, BrinRoles.BERSERKER)
            || gameWorld.isRole(player, BrinRoles.NIGHTMARE)
            || gameWorld.isRole(player, BrinRoles.MEDIUM)) {
            if (getItem().is(WatheItems.REVOLVER)) {
                ci.cancel();
            }
        }

        // Keys drop on death, so barring the survival expert from buying one is not enough - the item is
        // left on the ground rather than deleted, so whoever may carry it still can.
        if (gameWorld.isRole(player, BrinRoles.WATCHMAN)
            && BrinShopAccess.isSurvivalExpertExcludedItem(getItem())) {
            ci.cancel();
        }

        // The avenger only ever holds a gun during the witness window; anything picked up inside it is
        // swept away with the granted revolver when the window closes, so this stays exploit-free.
        if (gameWorld.isRole(player, BrinRoles.AVENGER) && BrinShopAccess.isFirearm(getItem())) {
            AvengerComponent avenger = AvengerComponent.KEY.get(player);
            if (avenger == null || !avenger.hasGunWindow()) {
                ci.cancel();
            }
        }
    }
}
