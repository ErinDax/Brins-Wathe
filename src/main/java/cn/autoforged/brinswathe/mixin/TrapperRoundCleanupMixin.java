package cn.autoforged.brinswathe.mixin;

import cn.autoforged.brinswathe.component.TrapperComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameFunctions.class)
public abstract class TrapperRoundCleanupMixin {
    @Inject(method = "initializeGame", at = @At("HEAD"))
    private static void brinClearOldTrapsOnStart(ServerLevel level, CallbackInfo ci) {
        TrapperComponent.clearAllTrapEntities(level.getServer());
    }

    @Inject(method = "stopGame", at = @At("HEAD"))
    private static void brinClearTrapsOnStop(ServerLevel level, CallbackInfo ci) {
        TrapperComponent.clearAllTrapEntities(level.getServer());
    }
}
