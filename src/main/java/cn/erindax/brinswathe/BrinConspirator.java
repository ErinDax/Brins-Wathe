package cn.erindax.brinswathe;

import dev.doctor4t.wathe.api.WatheRoles;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public final class BrinConspirator {
    private BrinConspirator() {
    }

    public static boolean isConspirator(GameWorldComponent game, Player player) {
        return game != null && player != null && game.isRole(player, BrinRoles.CONSPIRATOR);
    }

    public static boolean isConspirator(GameWorldComponent game, UUID playerId) {
        return game != null && playerId != null && game.isRole(playerId, BrinRoles.CONSPIRATOR);
    }

    public static void announce(Level world, GameWorldComponent game) {
        List<ServerPlayer> conspirators = new ArrayList<>();
        for (Player player : world.players()) {
            if (player instanceof ServerPlayer serverPlayer && isConspirator(game, serverPlayer)) {
                conspirators.add(serverPlayer);
            }
        }
        if (conspirators.isEmpty()) return;

        List<ServerPlayer> killers = new ArrayList<>();
        for (UUID killerId : game.getAllKillerTeamPlayers()) {
            if (world.getPlayerByUUID(killerId) instanceof ServerPlayer killer) killers.add(killer);
        }

        int killerColor = WatheRoles.KILLER.color();
        for (ServerPlayer killer : killers) {
            killer.sendSystemMessage(
                Component.translatable("message.brinswathe.conspirator.present").withColor(killerColor)
            );
        }

        Component names = killerNames(killers);
        for (ServerPlayer conspirator : conspirators) {
            conspirator.sendSystemMessage(
                Component.translatable("message.brinswathe.conspirator.killers", names).withColor(killerColor)
            );
        }
    }

    private static Component killerNames(List<ServerPlayer> killers) {
        if (killers.isEmpty()) return Component.translatable("message.brinswathe.conspirator.no_killers");
        MutableComponent joined = Component.empty();
        for (int index = 0; index < killers.size(); index++) {
            if (index > 0) joined.append(Component.translatable("message.brinswathe.conspirator.separator"));
            joined.append(killers.get(index).getDisplayName());
        }
        return joined;
    }
}
