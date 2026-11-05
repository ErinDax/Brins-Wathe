package cn.erindax.brinswathe.mixin;

import cn.erindax.brinswathe.BrinConspirator;
import dev.doctor4t.wathe.cca.GameRoundEndComponent;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.client.gui.RoleAnnouncementTexts;
import dev.doctor4t.wathe.game.GameFunctions;
import java.util.List;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = GameRoundEndComponent.class, remap = false)
public abstract class ConspiratorRoundEndMixin {
    @Inject(method = "setRoundEndData", at = @At("RETURN"))
    private void brinSideConspiratorsWithKillers(
        List<ServerPlayer> players,
        GameFunctions.WinStatus winStatus,
        CallbackInfo ci
    ) {
        if (players == null || players.isEmpty()) return;
        GameWorldComponent game = GameWorldComponent.KEY.get(players.getFirst().level());
        if (game == null) return;
        GameRoundEndComponent roundEnd = (GameRoundEndComponent) (Object) this;
        boolean passengersWon = winStatus == GameFunctions.WinStatus.PASSENGERS
            || winStatus == GameFunctions.WinStatus.TIME;
        List<GameRoundEndComponent.RoundEndData> rows = roundEnd.getPlayers();
        boolean changed = false;
        for (int index = 0; index < rows.size(); index++) {
            GameRoundEndComponent.RoundEndData row = rows.get(index);
            if (!BrinConspirator.isConspirator(game, row.player().getId())) continue;
            rows.set(index, new GameRoundEndComponent.RoundEndData(
                row.player(),
                RoleAnnouncementTexts.KILLER,
                row.wasDead() || passengersWon
            ));
            changed = true;
        }
        if (changed) roundEnd.sync();
    }
}
