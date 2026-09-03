package cn.erindax.brinswathe.mixin;

import cn.erindax.brinswathe.BrinHarpyRoles;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import java.util.UUID;
import org.agmas.harpymodloader.component.WorldModifierComponent;
import org.agmas.harpymodloader.modded_murder.ModdedMurderGameMode;
import org.agmas.harpymodloader.modifiers.Modifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ModdedMurderGameMode.class)
public abstract class BrinRoleModifierBlacklistMixin {
    @WrapWithCondition(
        method = "lambda$assignModifiers$1",
        at = @At(
            value = "INVOKE",
            target = "Lorg/agmas/harpymodloader/component/WorldModifierComponent;addModifier(Ljava/util/UUID;Lorg/agmas/harpymodloader/modifiers/Modifier;)V",
            ordinal = 1,
            remap = false
        )
    )
    private static boolean brinAllowRandomModifier(
        WorldModifierComponent component,
        UUID playerId,
        Modifier modifier,
        @Local(argsOnly = true) GameWorldComponent gameWorld
    ) {
        return !BrinHarpyRoles.isModifierBlacklisted(gameWorld.getRole(playerId), modifier);
    }
}
