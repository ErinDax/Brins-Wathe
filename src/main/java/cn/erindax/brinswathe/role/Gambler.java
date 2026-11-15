package cn.erindax.brinswathe.role;

import cn.erindax.brinswathe.AbilityCost;
import cn.erindax.brinswathe.ModRoles;
import cn.erindax.brinswathe.component.GamblerComponent;
import cn.erindax.brinswathe.network.AbilityC2SPacket;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

public final class Gambler {
    private Gambler() {
    }

    public static void handleBet(ServerPlayer player, GameWorldComponent gameWorld, AbilityC2SPacket payload) {
        if (!gameWorld.isRole(player, ModRoles.GAMBLER) || payload.targetId() == null) return;
        GamblerComponent gambler = GamblerComponent.KEY.get(player);
        if (gambler == null || gambler.betTarget() != null) return;
        ServerPlayer target = player.server.getPlayerList().getPlayer(payload.targetId());
        if (target == null || target == player || target.level() != player.level()) return;
        if (!GameFunctions.isPlayerAliveAndSurvival(target) || gameWorld.getRole(target) == null) return;
        if (!AbilityCost.spend(player, "gambler")) return;
        gambler.placeBet(target.getUUID(), target.getName().getString());
        player.playNotifySound(SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.0F, 1.0F);
    }
}
