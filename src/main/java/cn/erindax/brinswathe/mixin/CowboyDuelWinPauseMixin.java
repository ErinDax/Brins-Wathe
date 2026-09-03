package cn.erindax.brinswathe.mixin;

import cn.erindax.brinswathe.CowboyDuel;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.gamemode.DiscoveryGameMode;
import dev.doctor4t.wathe.game.gamemode.LooseEndsGameMode;
import dev.doctor4t.wathe.game.gamemode.MurderGameMode;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = {MurderGameMode.class, DiscoveryGameMode.class, LooseEndsGameMode.class}, remap = false)
public abstract class CowboyDuelWinPauseMixin {
    @Inject(method = "tickServerGameLoop", at = @At("HEAD"), cancellable = true, remap = false)
    private void brinPauseWinCheckDuringDuel(ServerLevel world, GameWorldComponent game, CallbackInfo ci) {
        if (CowboyDuel.isActive()) ci.cancel();
    }
}
