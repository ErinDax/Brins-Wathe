package cn.autoforged.brinswathe.mixin;

import cn.autoforged.brinswathe.component.BrinCustomWinnerComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameFunctions.class)
public abstract class BrinCustomWinnerResetMixin {
    @Inject(method = "initializeGame", at = @At("HEAD"))
    private static void brinResetCustomWinner(ServerLevel serverLevel, CallbackInfo ci) {
        BrinCustomWinnerComponent.KEY.get(serverLevel).reset();
    }
}
