package cn.erindax.brinswathe.mixin;

import cn.erindax.brinswathe.Faction;
import cn.erindax.brinswathe.GamblerBonus;
import cn.erindax.brinswathe.ModRoles;
import cn.erindax.brinswathe.component.ModCustomWinnerComponent;
import cn.erindax.brinswathe.component.CoWinnerComponent;
import dev.doctor4t.wathe.cca.GameRoundEndComponent;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.client.gui.RoleAnnouncementTexts;
import dev.doctor4t.wathe.game.GameFunctions;
import java.util.List;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.BsXinQin.kinswathe.component.CustomWinnerComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = GameRoundEndComponent.class, remap = false)
public abstract class FactionFollowerRoundEndMixin {
    @Inject(method = "setRoundEndData", at = @At("RETURN"))
    private void brinSideFollowersWithTheirFaction(
        List<ServerPlayer> players,
        GameFunctions.WinStatus winStatus,
        CallbackInfo ci
    ) {
        if (players == null || players.isEmpty()) return;
        Level level = players.getFirst().level();
        GameWorldComponent game = GameWorldComponent.KEY.get(level);
        if (game == null) return;
        boolean customWinner = brinHasCustomWinner(level);
        GameRoundEndComponent roundEnd = (GameRoundEndComponent) (Object) this;
        List<GameRoundEndComponent.RoundEndData> rows = roundEnd.getPlayers();
        boolean subverterWon = false;
        boolean gamblerWon = false;
        boolean changed = false;
        for (int index = 0; index < rows.size(); index++) {
            GameRoundEndComponent.RoundEndData row = rows.get(index);
            UUID playerId = row.player().getId();
            if (!Faction.isFollower(game, playerId)) continue;
            Player follower = level.getPlayerByUUID(playerId);
            Faction faction = follower == null ? null : Faction.followedBy(game, follower);
            RoleAnnouncementTexts.RoleAnnouncementText text;
            if (faction != null) {
                text = faction.endText();
            } else {
                text = winStatus == GameFunctions.WinStatus.KILLERS
                    ? RoleAnnouncementTexts.CIVILIAN
                    : RoleAnnouncementTexts.KILLER;
            }
            if (faction != null && faction.wins(winStatus) && !customWinner) {
                if (game.isRole(playerId, ModRoles.GAMBLER)) {
                    gamblerWon = true;
                    GamblerBonus.queue(playerId);
                } else {
                    subverterWon = true;
                }
            }
            rows.set(index, new GameRoundEndComponent.RoundEndData(row.player(), text, row.wasDead()));
            changed = true;
        }
        if (changed) roundEnd.sync();
        CoWinnerComponent.KEY.get(level).set(subverterWon, gamblerWon);
    }

    @Unique
    private static boolean brinHasCustomWinner(Level level) {
        if (ModCustomWinnerComponent.KEY.get(level).hasCustomWinner()) return true;
        CustomWinnerComponent kins = CustomWinnerComponent.KEY.getNullable(level);
        return kins != null && kins.hasCustomWinner();
    }
}
