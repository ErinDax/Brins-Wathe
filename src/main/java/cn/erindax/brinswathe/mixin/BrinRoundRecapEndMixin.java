package cn.erindax.brinswathe.mixin;

import cn.erindax.brinswathe.component.BrinRoundRecapComponent;
import dev.doctor4t.wathe.cca.GameRoundEndComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(value = GameRoundEndComponent.class, remap = false)
public abstract class BrinRoundRecapEndMixin {
    @Inject(method = "setRoundEndData", at = @At("RETURN"))
    private void brinSnapshotRoundIdentities(
        List<ServerPlayer> players,
        GameFunctions.WinStatus winStatus,
        CallbackInfo ci
    ) {
        if (players == null || players.isEmpty()) return;
        BrinRoundRecapComponent recap = BrinRoundRecapComponent.KEY.get(players.getFirst().level());
        if (recap == null) return;
        recap.snapshotIdentities(players);
        recap.announceAll();
    }
}
