package cn.erindax.brinswathe;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.PlayerShopComponent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public final class GamblerSettlement {
    public static final int WIN_BONUS = 200;
    public static final int LOSS_PENALTY = 75;
    private static final Map<UUID, Boolean> PENDING = new HashMap<>();

    private GamblerSettlement() {
    }

    public static void queue(UUID playerId, boolean won) {
        PENDING.put(playerId, won);
    }

    public static void apply(GameWorldComponent game, List<ServerPlayer> players) {
        if (PENDING.isEmpty()) return;
        for (ServerPlayer player : players) {
            if (game.getRole(player) == null) continue;
            Boolean won = PENDING.remove(player.getUUID());
            if (won == null) continue;
            int amount = won ? WIN_BONUS : LOSS_PENALTY;
            PlayerShopComponent.KEY.get(player).addToBalance(won ? amount : -amount);
            player.displayClientMessage(
                Component.translatable(
                    won ? "message.brinswathe.gambler.bonus" : "message.brinswathe.gambler.penalty",
                    amount
                ).withColor(ModRoles.GAMBLER.color()),
                false
            );
        }
    }
}
