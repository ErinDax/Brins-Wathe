package cn.erindax.brinswathe.client.mixin;

import cn.erindax.brinswathe.client.BrinSecretMurderClient;
import dev.doctor4t.wathe.client.WatheClient;
import dev.doctor4t.wathe.game.GameFunctions;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(value = WatheClient.class, priority = 100)
public abstract class BrinSecretInstinctMixin {
    @Inject(method = "getInstinctHighlight", at = @At("HEAD"), cancellable = true, remap = false)
    private static void brinSecretMurderHighlight(Entity target, CallbackInfoReturnable<Integer> cir) {
        if (!(target instanceof Player player) || player == Minecraft.getInstance().player) return;
        if (!BrinSecretMurderClient.isLocalSecretKiller() || !WatheClient.isInstinctEnabled()) return;
        if (GameFunctions.isPlayerSpectatingOrCreative(player)) {
            cir.setReturnValue(-1);
            return;
        }
        cir.setReturnValue(BrinSecretMurderClient.highlight(player.getUUID()));
    }
}
