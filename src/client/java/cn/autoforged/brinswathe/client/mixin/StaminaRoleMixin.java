package cn.autoforged.brinswathe.client.mixin;

import cn.autoforged.brinswathe.component.StaminaComponent;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Environment(EnvType.CLIENT)
@Mixin(Role.class)
public abstract class StaminaRoleMixin {
    @ModifyReturnValue(method = "getMaxSprintTime", at = @At("RETURN"))
    private int brinUseConfiguredMaximum(int original) {
        if (original < 0) return original;
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return original;
        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(player.level());
        if (gameWorld == null
            || gameWorld.getRole(player) != (Object) this
            || gameWorld.canUseKillerFeatures(player)) return original;
        StaminaComponent stamina = StaminaComponent.KEY.get(player);
        return stamina == null ? original : stamina.maxStamina;
    }
}
