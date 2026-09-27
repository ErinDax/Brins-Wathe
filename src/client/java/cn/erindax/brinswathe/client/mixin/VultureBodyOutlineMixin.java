package cn.erindax.brinswathe.client.mixin;

import cn.erindax.brinswathe.BrinNoelleAccess;
import cn.erindax.brinswathe.component.IllusionistComponent;
import cn.erindax.brinswathe.component.PuppeteerControlComponent;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.entity.PlayerBodyEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Minecraft.class)
public abstract class VultureBodyOutlineMixin {
    @ModifyReturnValue(method = "shouldEntityAppearGlowing", at = @At("RETURN"))
    private boolean brinVultureBodyOutline(boolean original, @Local(argsOnly = true) Entity entity) {
        if (!(entity instanceof PlayerBodyEntity body)) return original;
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.level == null) return original;
        GameWorldComponent game = GameWorldComponent.KEY.get(client.level);
        if (!BrinNoelleAccess.isRole(game, client.player, BrinNoelleAccess.VULTURE_ID)) return original;
        if (IllusionistComponent.isIllusionModel(body) || PuppeteerControlComponent.isPuppetModel(body)) return false;
        if (BrinNoelleAccess.isVultured(entity)) return false;
        return true;
    }
}
