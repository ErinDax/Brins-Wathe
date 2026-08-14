package cn.autoforged.brinswathe.client.mixin;

import cn.autoforged.brinswathe.component.IllusionistComponent;
import dev.doctor4t.wathe.client.WatheClient;
import dev.doctor4t.wathe.entity.PlayerBodyEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(value = WatheClient.class, priority = 2000)
public abstract class BuriedCorpseInstinctMixin {
    @Inject(method = "getInstinctHighlight", at = @At("HEAD"), cancellable = true, remap = false)
    private static void brinHideBuriedCorpseOutline(
        Entity target,
        CallbackInfoReturnable<Integer> cir
    ) {
        if (target instanceof PlayerBodyEntity body
            && (body.isInvisible() || IllusionistComponent.isIllusionModel(body))) {
            cir.setReturnValue(-1);
        }
    }
}
