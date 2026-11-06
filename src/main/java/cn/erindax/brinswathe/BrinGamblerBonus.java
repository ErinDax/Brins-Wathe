package cn.erindax.brinswathe;

import dev.doctor4t.wathe.cca.PlayerShopComponent;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public final class BrinGamblerBonus {
    public static final int AMOUNT = 50;
    private static final Set<UUID> PENDING = new HashSet<>();

    private BrinGamblerBonus() {
    }

    public static void queue(UUID playerId) {
        PENDING.add(playerId);
    }

    public static void pay(List<ServerPlayer> players) {
        if (PENDING.isEmpty()) return;
        for (ServerPlayer player : players) {
            if (!PENDING.contains(player.getUUID())) continue;
            PlayerShopComponent.KEY.get(player).addToBalance(AMOUNT);
            player.displayClientMessage(
                Component.translatable("message.brinswathe.gambler.bonus", AMOUNT).withColor(BrinRoles.GAMBLER.color()),
                false
            );
        }
        PENDING.clear();
    }
}
