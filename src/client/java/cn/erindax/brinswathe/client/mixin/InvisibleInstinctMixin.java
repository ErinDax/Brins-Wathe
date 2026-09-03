package cn.erindax.brinswathe.client.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import cn.erindax.brinswathe.component.IllusionistComponent;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.client.WatheClient;
import dev.doctor4t.wathe.entity.PlayerBodyEntity;
import dev.doctor4t.wathe.game.GameFunctions;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Environment(EnvType.CLIENT)
@Mixin(value = WatheClient.class, priority = 10000)
public abstract class InvisibleInstinctMixin {
    @ModifyReturnValue(method = "getInstinctHighlight", at = @At("RETURN"), remap = false)
    private static int brinHideInvisibleFromInnocentInstinct(int color, Entity target) {
        if (color == -1) return color;
        Player localPlayer = Minecraft.getInstance().player;
        if (localPlayer == null) return color;
        if (GameFunctions.isPlayerSpectatingOrCreative(localPlayer)) return color;
        if (target instanceof PlayerBodyEntity body) {
            if (body.isInvisible() || IllusionistComponent.isIllusionModel(body)) return -1;
            return color;
        }
        if (!(target instanceof Player targetPlayer) || targetPlayer == localPlayer) return color;
        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(localPlayer.level());
        if (gameWorld.canUseKillerFeatures(localPlayer)) return color;
        if (!brinIsStealthed(targetPlayer)) return color;
        return -1;
    }
    @Unique
    private static boolean brinIsStealthed(Player player) {
        return player.isInvisible() || player.hasEffect(MobEffects.INVISIBILITY);
    }
}
