package cn.erindax.brinswathe.mixin;

import cn.erindax.brinswathe.BrinRoles;
import cn.erindax.brinswathe.BrinSubversion;
import cn.erindax.brinswathe.component.SubverterComponent;
import cn.erindax.brinswathe.component.SubverterWinComponent;
import dev.doctor4t.wathe.cca.GameRoundEndComponent;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.client.gui.RoleAnnouncementTexts;
import dev.doctor4t.wathe.game.GameFunctions;
import java.util.List;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = GameRoundEndComponent.class, remap = false)
public abstract class SubverterRoundEndMixin {
    @Inject(method = "setRoundEndData", at = @At("RETURN"))
    private void brinSideSubvertersWithTheirFaction(
        List<ServerPlayer> players,
        GameFunctions.WinStatus winStatus,
        CallbackInfo ci
    ) {
        if (players == null || players.isEmpty()) return;
        Level level = players.getFirst().level();
        GameWorldComponent game = GameWorldComponent.KEY.get(level);
        if (game == null) return;
        GameRoundEndComponent roundEnd = (GameRoundEndComponent) (Object) this;
        List<GameRoundEndComponent.RoundEndData> rows = roundEnd.getPlayers();
        boolean subverterWon = false;
        boolean changed = false;
        for (int index = 0; index < rows.size(); index++) {
            GameRoundEndComponent.RoundEndData row = rows.get(index);
            UUID playerId = row.player().getId();
            if (!game.isRole(playerId, BrinRoles.SUBVERTER)) continue;
            Player subverterPlayer = level.getPlayerByUUID(playerId);
            SubverterComponent subverter = subverterPlayer == null ? null : SubverterComponent.KEY.get(subverterPlayer);
            SubverterComponent.Side side = subverter == null ? null : subverter.side();
            RoleAnnouncementTexts.RoleAnnouncementText faction;
            if (side == SubverterComponent.Side.KILLER) {
                faction = RoleAnnouncementTexts.KILLER;
            } else if (side == SubverterComponent.Side.CIVILIAN) {
                faction = RoleAnnouncementTexts.CIVILIAN;
            } else {
                faction = winStatus == GameFunctions.WinStatus.KILLERS
                    ? RoleAnnouncementTexts.CIVILIAN
                    : RoleAnnouncementTexts.KILLER;
            }
            if (side != null && side.wins(winStatus)) subverterWon = true;
            rows.set(index, new GameRoundEndComponent.RoundEndData(row.player(), faction, row.wasDead()));
            changed = true;
        }
        if (changed) roundEnd.sync();
        SubverterWinComponent.KEY.get(level).setSubverterWon(subverterWon && !BrinSubversion.hasCustomWinner(level));
    }
}
