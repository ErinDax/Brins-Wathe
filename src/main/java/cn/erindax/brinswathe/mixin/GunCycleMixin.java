package cn.erindax.brinswathe.mixin;

import cn.erindax.brinswathe.KnifeSkins;
import dev.doctor4t.wathe.index.WatheCosmetics;
import dev.doctor4t.wathe.index.WatheItems;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Item.class)
public abstract class GunCycleMixin {
    @Inject(method = "overrideOtherStackedOnMe", at = @At("HEAD"), cancellable = true)
    private void brinCycleGunSkins(
        ItemStack stack,
        ItemStack other,
        Slot slot,
        ClickAction clickType,
        Player player,
        SlotAccess access,
        CallbackInfoReturnable<Boolean> cir
    ) {
        if (!stack.is(WatheItems.REVOLVER)) return;
        if (clickType != ClickAction.SECONDARY || !other.isEmpty()) return;
        if (KnifeSkins.extraGunModelSkinNames().isEmpty()) return;

        String current = KnifeSkins.resolveGunSkinName(WatheCosmetics.getSkin(stack));
        String next = KnifeSkins.nextGunName(current);
        KnifeSkins.applySkin(stack, "gun", next);
        if (player instanceof ServerPlayer serverPlayer) {
            KnifeSkins.setGunSkin(serverPlayer.getUUID(), next);
        }
        cir.setReturnValue(true);
    }
}
