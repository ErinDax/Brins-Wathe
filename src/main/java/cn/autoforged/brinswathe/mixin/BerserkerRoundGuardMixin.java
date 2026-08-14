package cn.autoforged.brinswathe.mixin;

import cn.autoforged.brinswathe.BrinsWathe;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.gamemode.MurderGameMode;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MurderGameMode.class)
public abstract class BerserkerRoundGuardMixin {
    @Inject(
        method = "tickServerGameLoop",
        at = @At(
            value = "INVOKE",
            target = "Ldev/doctor4t/wathe/cca/GameRoundEndComponent;setRoundEndData(Ljava/util/List;Ldev/doctor4t/wathe/game/GameFunctions$WinStatus;)V"
        ),
        cancellable = true
    )
    private void brinDelayNativeWin(ServerLevel level, GameWorldComponent gameWorld, CallbackInfo ci) {
        if (BrinsWathe.checkBerserkerWin(level.getServer(), gameWorld)
            || BrinsWathe.checkPenitentWin(level.getServer(), gameWorld)) {
            ci.cancel();
        }
    }
}
