package cn.erindax.brinswathe.client.mixin;

import dev.doctor4t.wathe.client.WatheClient;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.KeyMapping;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(KeyMapping.class)
public abstract class InstinctToggleMixin {
    @Unique
    private static boolean brinInstinctToggled;
    @Unique
    private boolean brinPhysicallyDown;
    @Inject(method = "setDown", at = @At("HEAD"))
    private void brinFlipInstinctOnPress(boolean down, CallbackInfo ci) {
        if ((Object) this != WatheClient.instinctKeybind) return;
        if (down && !this.brinPhysicallyDown) {
            brinInstinctToggled = !brinInstinctToggled;
        }
        this.brinPhysicallyDown = down;
    }
    @Inject(method = "isDown", at = @At("HEAD"), cancellable = true)
    private void brinInstinctAsToggle(CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this != WatheClient.instinctKeybind) return;
        cir.setReturnValue(brinInstinctToggled);
    }
}
