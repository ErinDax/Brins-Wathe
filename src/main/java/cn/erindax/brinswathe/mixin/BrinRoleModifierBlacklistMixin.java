package cn.erindax.brinswathe.mixin;

import cn.erindax.brinswathe.BrinHarpyRoles;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import net.minecraft.server.level.ServerPlayer;
import org.agmas.harpymodloader.modded_murder.ModdedMurderGameMode;
import org.agmas.harpymodloader.modifiers.Modifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ModdedMurderGameMode.class)
public abstract class BrinRoleModifierBlacklistMixin {
    @ModifyExpressionValue(
        method = "lambda$assignModifiers$1",
        at = @At(
            value = "INVOKE",
            target = "Ljava/util/ArrayList;contains(Ljava/lang/Object;)Z",
            ordinal = 0,
            remap = false
        )
    )
    private static boolean brinSkipBlacklistedPlayer(
        boolean disabled,
        @Local ServerPlayer player,
        @Local(argsOnly = true) GameWorldComponent gameWorld,
        @Local(argsOnly = true) Modifier modifier
    ) {
        return disabled || BrinHarpyRoles.isModifierBlacklisted(gameWorld.getRole(player), modifier);
    }
}
