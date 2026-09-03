package cn.erindax.brinswathe.mixin;

import cn.erindax.brinswathe.BrinKnifeSkins;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "dev.doctor4t.wathe.index.WatheCosmetics", remap = false)
public interface BrinWatheCosmeticsSkinMixin {
    @Inject(method = "getSkin", at = @At("HEAD"), cancellable = true)
    private static void brinReadNbtSkin(ItemStack stack, CallbackInfoReturnable<String> cir) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) return;
        var nbt = data.copyTag();
        if (nbt.contains("wathe_skin")) {
            String raw = nbt.getString("wathe_skin");
            String resolved = BrinKnifeSkins.resolveKnifeSkinName(raw);
            cir.setReturnValue(resolved == null || resolved.isBlank() ? raw : resolved);
        }
    }
}
