package cn.autoforged.brinswathe.mixin;

import dev.doctor4t.wathe.index.WatheItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class KeyStackSizeMixin {
    private static final ResourceLocation MASTER_KEY_ID =
        ResourceLocation.fromNamespaceAndPath("noellesroles", "master_key");

    @Inject(method = "getMaxStackSize", at = @At("HEAD"), cancellable = true)
    private void brinAllowKeyStacking(CallbackInfoReturnable<Integer> cir) {
        ItemStack stack = (ItemStack) (Object) this;
        if (stack.is(WatheItems.KEY)
            || MASTER_KEY_ID.equals(BuiltInRegistries.ITEM.getKey(stack.getItem()))) {
            cir.setReturnValue(64);
        }
    }
}
