package cn.autoforged.brinswathe.client.mixin;

import cn.autoforged.brinswathe.client.BrinsWatheClient;
import dev.doctor4t.wathe.item.KnifeItem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(KnifeItem.class)
public abstract class IllusionistKnifeTargetMixin {
    @Inject(method = "getKnifeTarget", at = @At("HEAD"), cancellable = true, remap = false)
    private static void brinIncludeIllusionModels(Player attacker, CallbackInfoReturnable<HitResult> cir) {
        cir.setReturnValue(BrinsWatheClient.findWeaponTarget(attacker, 3.0D));
    }
}
