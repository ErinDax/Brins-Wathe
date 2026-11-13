package cn.erindax.brinswathe.mixin;

import cn.erindax.brinswathe.Faction;
import cn.erindax.brinswathe.GamblerSettlement;
import cn.erindax.brinswathe.ModRoles;
import cn.erindax.brinswathe.component.ModCustomWinnerComponent;
import cn.erindax.brinswathe.component.CoWinnerComponent;
import cn.erindax.brinswathe.component.GamblerComponent;
import dev.doctor4t.wathe.cca.GameRoundEndComponent;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.client.gui.RoleAnnouncementTexts;
import dev.doctor4t.wathe.game.GameFunctions;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
        boolean killersWon = winStatus == GameFunctions.WinStatus.KILLERS;
        RoleAnnouncementTexts.RoleAnnouncementText winningText = killersWon
            ? RoleAnnouncementTexts.KILLER
            : RoleAnnouncementTexts.CIVILIAN;
        RoleAnnouncementTexts.RoleAnnouncementText losingText = killersWon
            ? RoleAnnouncementTexts.CIVILIAN
            : RoleAnnouncementTexts.KILLER;
        GameRoundEndComponent roundEnd = (GameRoundEndComponent) (Object) this;
        List<GameRoundEndComponent.RoundEndData> rows = roundEnd.getPlayers();
        Map<UUID, UUID> bets = new HashMap<>();
        boolean subverterWon = false;
        boolean changed = false;
        for (int index = 0; index < rows.size(); index++) {
            GameRoundEndComponent.RoundEndData row = rows.get(index);
            UUID playerId = row.player().getId();
            if (!Faction.isFollower(game, playerId)) continue;
            Player follower = level.getPlayerByUUID(playerId);
            RoleAnnouncementTexts.RoleAnnouncementText text = losingText;
            if (game.isRole(playerId, ModRoles.GAMBLER)) {
                GamblerComponent gambler = follower == null ? null : GamblerComponent.KEY.get(follower);
                UUID targetId = gambler == null ? null : gambler.betTarget();
                if (targetId != null) bets.put(playerId, targetId);
            } else {
                Faction faction = follower == null ? null : Faction.followedBy(game, follower);
                if (faction != null) {
                    text = faction.endText();
                    if (faction.wins(winStatus) && !customWinner) subverterWon = true;
                }
            }
            rows.set(index, new GameRoundEndComponent.RoundEndData(row.player(), text, row.wasDead()));
            changed = true;
        }
        Set<UUID> wonBets = new HashSet<>();
        boolean settling = !bets.isEmpty();
        while (settling) {
            settling = false;
            for (int index = 0; index < rows.size(); index++) {
                GameRoundEndComponent.RoundEndData row = rows.get(index);
                UUID gamblerId = row.player().getId();
                UUID targetId = bets.get(gamblerId);
                if (targetId == null || wonBets.contains(gamblerId) || !brinBetWon(level, roundEnd, targetId)) continue;
                rows.set(index, new GameRoundEndComponent.RoundEndData(row.player(), winningText, row.wasDead()));
                wonBets.add(gamblerId);
                settling = true;
            }
        }
        for (UUID gamblerId : bets.keySet()) {
            GamblerSettlement.queue(gamblerId, wonBets.contains(gamblerId));
        }
        if (changed) roundEnd.sync();
        CoWinnerComponent.KEY.get(level).set(subverterWon, !customWinner && !wonBets.isEmpty());
    }

    @Unique
    private static boolean brinBetWon(Level level, GameRoundEndComponent roundEnd, UUID targetId) {
        Player target = level.getPlayerByUUID(targetId);
        return target != null && GameFunctions.isPlayerAliveAndSurvival(target) && roundEnd.didWin(targetId);
    }

    @Unique
    private static boolean brinHasCustomWinner(Level level) {
        if (ModCustomWinnerComponent.KEY.get(level).hasCustomWinner()) return true;
        CustomWinnerComponent kins = CustomWinnerComponent.KEY.getNullable(level);
        return kins != null && kins.hasCustomWinner();
    }
}
