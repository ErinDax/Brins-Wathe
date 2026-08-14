package cn.autoforged.brinswathe.client.mixin;

import cn.autoforged.brinswathe.client.BrinsWatheClient;
import dev.doctor4t.wathe.item.RevolverItem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(RevolverItem.class)
public abstract class IllusionistRevolverTargetMixin {
    private static final ResourceLocation FAKE_REVOLVER_ID =
        ResourceLocation.fromNamespaceAndPath("noellesroles", "fake_revolver");

    @Inject(method = "getGunTarget", at = @At("HEAD"), cancellable = true, remap = false)
    private static void brinIncludeIllusionModels(Player shooter, CallbackInfoReturnable<HitResult> cir) {
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(shooter.getMainHandItem().getItem());
        if (FAKE_REVOLVER_ID.equals(itemId)) return;
        cir.setReturnValue(BrinsWatheClient.findWeaponTarget(shooter, 15.0D));
    }
}
