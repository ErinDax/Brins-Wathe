package cn.erindax.brinswathe.client.mixin;

import cn.erindax.brinswathe.BrinNoelleAccess;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.entity.PlayerBodyEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Entity.class)
public abstract class VultureBodyColorMixin {
    private static final int VULTURE_BODY_OUTLINE_COLOR = 0xFFFFFF00;

    @ModifyReturnValue(method = "getTeamColor", at = @At("RETURN"))
    private int brinVultureBodyColor(int original) {
        Entity entity = (Entity) (Object) this;
        if (!(entity instanceof PlayerBodyEntity)) return original;
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.level == null) return original;
        GameWorldComponent game = GameWorldComponent.KEY.get(client.level);
        if (!BrinNoelleAccess.isRole(game, client.player, BrinNoelleAccess.VULTURE_ID)) return original;
        if (BrinNoelleAccess.isVultured(entity)) return original;
        return VULTURE_BODY_OUTLINE_COLOR;
    }
}
