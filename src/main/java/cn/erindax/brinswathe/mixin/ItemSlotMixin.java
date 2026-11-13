package cn.erindax.brinswathe.mixin;

import cn.erindax.brinswathe.BuySlots;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Inventory.class)
public abstract class ItemSlotMixin {
    @Inject(method = "add(ILnet/minecraft/world/item/ItemStack;)Z", at = @At("HEAD"), cancellable = true)
    private void brinPlaceInChosenSlot(int slot, ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (slot == -1 && BuySlots.add((Inventory) (Object) this, stack)) cir.setReturnValue(true);
    }
}
