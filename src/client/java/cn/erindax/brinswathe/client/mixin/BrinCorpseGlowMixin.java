package cn.erindax.brinswathe.client.mixin;

import cn.erindax.brinswathe.client.BrinCorpseGlow;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Environment(EnvType.CLIENT)
@Mixin(Minecraft.class)
public abstract class BrinCorpseGlowMixin {
    @ModifyReturnValue(method = "shouldEntityAppearGlowing", at = @At("RETURN"))
    private boolean brinCorpseGlow(boolean original, @Local(argsOnly = true) Entity entity) {
        return original || BrinCorpseGlow.isGlowingCorpse(entity);
    }
}
