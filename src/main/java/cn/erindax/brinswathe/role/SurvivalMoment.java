package cn.erindax.brinswathe.role;

import cn.erindax.brinswathe.ModRoles;
import cn.erindax.brinswathe.component.SurvivalMomentComponent;
import cn.erindax.brinswathe.config.Tunable;
import dev.doctor4t.wathe.cca.GameRoundEndComponent;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import org.BsXinQin.kinswathe.component.GameSafeComponent;

public final class SurvivalMoment {
    private SurvivalMoment() {
    }

    public static void tick(ServerLevel level, SurvivalMomentComponent moment) {
        GameWorldComponent game = GameWorldComponent.KEY.get(level);
        if (game.getGameStatus() != GameWorldComponent.GameStatus.ACTIVE) {
            moment.reset();
            return;
        }
        if (CowboyDuel.isActive() || GameSafeComponent.KEY.get(level).isGameSafe) return;
        if (!onlyWatchmanLeft(level, game)) {
            moment.reset();
            return;
        }
        if (!moment.isActive()) {
            begin(level, moment);
            return;
        }
        if (moment.countDown()) win(level);
    }

    private static boolean onlyWatchmanLeft(ServerLevel level, GameWorldComponent game) {
        boolean watchmanAlive = false;
        for (ServerPlayer player : level.players()) {
            if (GameFunctions.isPlayerEliminated(player)) continue;
            if (!game.isInnocent(player) || Conspirator.isConspirator(game, player)) continue;
            if (!game.isRole(player, ModRoles.WATCHMAN)) return false;
            watchmanAlive = true;
        }
        return watchmanAlive;
    }

    private static void begin(ServerLevel level, SurvivalMomentComponent moment) {
        int seconds = Tunable.WATCHMAN_SURVIVAL_SECONDS.intValue();
        moment.start(seconds * 20);
        Component message = Component.translatable("message.brinswathe.watchman.survival_moment", seconds)
            .withStyle(style -> style.withColor(ModRoles.WATCHMAN.color()));
        for (ServerPlayer player : level.players()) {
            player.sendSystemMessage(message);
            player.playNotifySound(SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.0F, 1.0F);
        }
    }

    private static void win(ServerLevel level) {
        Component message = Component.translatable("message.brinswathe.watchman.survival_moment.win")
            .withStyle(style -> style.withColor(ModRoles.WATCHMAN.color()));
        for (ServerPlayer player : level.players()) player.sendSystemMessage(message);
        GameRoundEndComponent.KEY.get(level).setRoundEndData(level.players(), GameFunctions.WinStatus.PASSENGERS);
        GameFunctions.stopGame(level);
    }
}
