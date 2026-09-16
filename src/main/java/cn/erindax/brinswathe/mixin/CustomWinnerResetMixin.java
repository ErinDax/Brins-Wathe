package cn.erindax.brinswathe.mixin;

import cn.erindax.brinswathe.component.ModCustomWinnerComponent;
import cn.erindax.brinswathe.component.RoundRecapComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameFunctions.class)
public abstract class CustomWinnerResetMixin {
    @Inject(method = "initializeGame", at = @At("HEAD"))
    private static void brinResetCustomWinner(ServerLevel serverLevel, CallbackInfo ci) {
        ModCustomWinnerComponent.KEY.get(serverLevel).reset();
        RoundRecapComponent.KEY.get(serverLevel).reset();
    }
}
