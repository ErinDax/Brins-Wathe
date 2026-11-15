package cn.erindax.brinswathe.role;

import cn.erindax.brinswathe.ModRoles;
import cn.erindax.brinswathe.config.Tunable;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.PlayerShopComponent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public final class GamblerSettlement {
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
            int amount = won
                ? Tunable.GAMBLER_WIN_BONUS.intValue()
                : Tunable.GAMBLER_LOSS_PENALTY.intValue();
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
