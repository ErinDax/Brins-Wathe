package cn.erindax.brinswathe.mixin;

import cn.erindax.brinswathe.component.RoundRecapComponent;
import cn.erindax.brinswathe.musicbox.MusicBox;
import dev.doctor4t.wathe.cca.GameRoundEndComponent;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(value = GameRoundEndComponent.class, remap = false)
public abstract class RoundRecapEndMixin {
    @Inject(method = "setRoundEndData", at = @At("RETURN"))
    private void brinSnapshotRoundIdentities(
        List<ServerPlayer> players,
        GameFunctions.WinStatus winStatus,
        CallbackInfo ci
    ) {
        if (players == null || players.isEmpty()) return;
        Level level = players.getFirst().level();
        GameWorldComponent game = GameWorldComponent.KEY.get(level);
        if (game == null || game.getGameStatus() != GameWorldComponent.GameStatus.ACTIVE) return;
        RoundRecapComponent recap = RoundRecapComponent.KEY.get(level);
        if (recap != null) {
            recap.snapshotIdentities(players);
            recap.announceAll();
        }
        MusicBox.onRoundEnd(players, winStatus);
    }
}
