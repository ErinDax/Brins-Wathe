package cn.erindax.brinswathe.client.mixin;

import cn.erindax.brinswathe.CowboyDuel;
import dev.doctor4t.wathe.cca.PlayerPsychoComponent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(value = PlayerPsychoComponent.class, remap = false)
public abstract class CowboyDuelPsychoBatMixin {
    @Shadow
    @Final
    private Player player;

    @Inject(method = "clientTick", at = @At("HEAD"), cancellable = true)
    private void brinKeepDuelGunSelected(CallbackInfo ci) {
        Inventory inventory = this.player.getInventory();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            if (CowboyDuel.isDuelGun(inventory.getItem(slot))) {
                ci.cancel();
                return;
            }
        }
    }
}
