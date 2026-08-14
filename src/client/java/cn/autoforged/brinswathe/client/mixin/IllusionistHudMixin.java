package cn.autoforged.brinswathe.client.mixin;

import cn.autoforged.brinswathe.client.BrinsWatheClient;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(Gui.class)
public abstract class IllusionistHudMixin {
    @Inject(method = "getCameraPlayer", at = @At("RETURN"), cancellable = true)
    private void brinUseOwnerInventoryForCloneCamera(CallbackInfoReturnable<Player> cir) {
        if (cir.getReturnValue() == null && BrinsWatheClient.isControllingClone()) {
            cir.setReturnValue(Minecraft.getInstance().player);
        }
    }
}
