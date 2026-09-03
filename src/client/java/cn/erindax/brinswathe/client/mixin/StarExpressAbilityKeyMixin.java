package cn.erindax.brinswathe.client.mixin;

import cn.erindax.brinswathe.client.BrinsWatheClient;
import net.minecraft.client.KeyMapping;
import org.aussiebox.starexpress.client.StarryExpressClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = StarryExpressClient.class, remap = false)
public abstract class StarExpressAbilityKeyMixin {
    @Redirect(
        method = "onInitializeClient",
        at = @At(
            value = "INVOKE",
            target = "Lnet/fabricmc/fabric/api/client/keybinding/v1/KeyBindingHelper;registerKeyBinding(Lnet/minecraft/client/KeyMapping;)Lnet/minecraft/client/KeyMapping;"
        )
    )
    private KeyMapping brinUseSharedAbilityKey(KeyMapping duplicateKey) {
        return BrinsWatheClient.shareAbilityBind(duplicateKey);
    }
}
