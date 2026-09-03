package cn.erindax.brinswathe.client.mixin;

import cn.erindax.brinswathe.client.CorpseGlow;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Environment(EnvType.CLIENT)
@Mixin(Entity.class)
public abstract class CorpseGlowColorMixin {
    @ModifyReturnValue(method = "getTeamColor", at = @At("RETURN"))
    private int brinCorpseGlowColour(int original) {
        Entity entity = (Entity) (Object) this;
        return CorpseGlow.isGlowingCorpse(entity) ? CorpseGlow.COLOUR : original;
    }
}
