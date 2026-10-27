package cn.erindax.brinswathe;

import cn.erindax.brinswathe.network.BrinWelcomeS2CPacket;
import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.api.WatheRoles;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import org.agmas.harpymodloader.Harpymodloader;
import org.jetbrains.annotations.Nullable;

public final class BrinWelcome {
    public static final int KIND_ROLE = 0;
    public static final int KIND_CIVILIAN = 1;
    public static final int KIND_VIGILANTE = 2;
    public static final int KIND_KILLER = 3;

    private BrinWelcome() {
    }

    public static boolean send(ServerPlayer player, @Nullable Role role, int kind, int killers, int targets) {
        if (role == null || !ServerPlayNetworking.canSend(player, BrinWelcomeS2CPacket.TYPE)) return false;
        ServerPlayNetworking.send(player, new BrinWelcomeS2CPacket(role.identifier().toString(), kind, killers, targets));
        return true;
    }

    public static boolean sendAssigned(ServerPlayer player, GameWorldComponent game, int killers, int targets) {
        Role role = game.getRole(player);
        return send(player, role, assignedKind(game, player, role), killers, targets);
    }

    public static int vanillaKind(@Nullable Role role, int vanillaKind) {
        return role != null && Harpymodloader.VANNILA_ROLES.contains(role) ? vanillaKind : KIND_ROLE;
    }

    private static int assignedKind(GameWorldComponent game, ServerPlayer player, @Nullable Role role) {
        if (role == null || !Harpymodloader.VANNILA_ROLES.contains(role)) return KIND_ROLE;
        if (game.isRole(player, WatheRoles.KILLER)) return KIND_KILLER;
        if (game.isRole(player, WatheRoles.VIGILANTE)) return KIND_VIGILANTE;
        return KIND_CIVILIAN;
    }
}
