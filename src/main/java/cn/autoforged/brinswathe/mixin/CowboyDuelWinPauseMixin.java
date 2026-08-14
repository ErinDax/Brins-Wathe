package cn.autoforged.brinswathe.mixin;

import cn.autoforged.brinswathe.CowboyDuel;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.gamemode.DiscoveryGameMode;
import dev.doctor4t.wathe.game.gamemode.LooseEndsGameMode;
import dev.doctor4t.wathe.game.gamemode.MurderGameMode;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Every wathe round-end verdict (team wiped out, timer expired) lives in the game modes' server loop.
 * A cowboy duel benches everyone but the two duelists as spectators, which reads exactly like a wiped
 * team, so the whole verdict is put on hold until the duel resolves.
 */
@Mixin(value = {MurderGameMode.class, DiscoveryGameMode.class, LooseEndsGameMode.class}, remap = false)
public abstract class CowboyDuelWinPauseMixin {
    @Inject(method = "tickServerGameLoop", at = @At("HEAD"), cancellable = true, remap = false)
    private void brinPauseWinCheckDuringDuel(ServerLevel world, GameWorldComponent game, CallbackInfo ci) {
        if (CowboyDuel.isActive()) ci.cancel();
    }
}
