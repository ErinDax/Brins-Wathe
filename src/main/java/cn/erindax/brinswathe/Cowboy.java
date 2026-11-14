package cn.erindax.brinswathe;

import cn.erindax.brinswathe.component.CowboyComponent;
import cn.erindax.brinswathe.config.ModConfig;
import cn.erindax.brinswathe.network.AbilityC2SPacket;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public final class Cowboy {
    private Cowboy() {
    }

    public static void handleDuel(ServerPlayer player, GameWorldComponent gameWorld,
                                  AbilityC2SPacket payload) {
        if (!gameWorld.isRole(player, ModRoles.COWBOY)) return;
        if (gameWorld.getGameStatus() != GameWorldComponent.GameStatus.ACTIVE) return;
        if (payload.targetId() == null) return;
        ServerPlayer target = player.server.getPlayerList().getPlayer(payload.targetId());
        if (target == null || target.equals(player) || target.level() != player.level()) return;
        if (!GameFunctions.isPlayerAliveAndSurvival(target)) {
            player.displayClientMessage(
                Component.translatable("message.brinswathe.cowboy.target_dead")
                    .withStyle(net.minecraft.ChatFormatting.RED),
                true
            );
            return;
        }
        CowboyComponent cowboy = CowboyComponent.KEY.get(player);
        if (cowboy == null) return;
        if (cowboy.duelUsed()) {
            player.displayClientMessage(
                Component.translatable("message.brinswathe.cowboy.already_used")
                    .withStyle(net.minecraft.ChatFormatting.RED),
                true
            );
            return;
        }
        if (CowboyDuel.isActive()) return;
        net.minecraft.core.BlockPos posA = ModConfig.cowboyArenaA();
        net.minecraft.core.BlockPos posB = ModConfig.cowboyArenaB();
        net.minecraft.core.BlockPos posSpectator = ModConfig.cowboyArenaSpectator();
        if (posA == null || posB == null || posSpectator == null) {
            player.displayClientMessage(
                Component.translatable("message.brinswathe.cowboy.arena_unset")
                    .withStyle(net.minecraft.ChatFormatting.RED),
                false
            );
            return;
        }
        if (!AbilityCost.spend(player, "cowboy")) return;
        cowboy.markDuelUsed();
        CowboyDuel.start(player, target, posA, posB, posSpectator);
    }
}
