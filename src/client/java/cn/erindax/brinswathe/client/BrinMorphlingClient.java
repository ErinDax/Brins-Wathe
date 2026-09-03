package cn.erindax.brinswathe.client;

import cn.erindax.brinswathe.BrinMorphlingAccess;
import cn.erindax.brinswathe.BrinNoelleAccess;
import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import cn.erindax.brinswathe.network.BrinAbilityC2SPacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public final class BrinMorphlingClient {
    private BrinMorphlingClient() {
    }

    public static boolean isMorphling(GameWorldComponent game, Player player) {
        return BrinNoelleAccess.isRole(game, player, BrinNoelleAccess.MORPHLING_ID);
    }

    public static List<UUID> deadTeammates(GameWorldComponent game, Player self) {
        List<UUID> targets = new ArrayList<>();
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        if (game == null || self == null || connection == null) return targets;
        for (UUID teammateId : game.getAllKillerTeamPlayers()) {
            if (teammateId.equals(self.getUUID())) continue;
            if (connection.getPlayerInfo(teammateId) == null) continue;
            Player teammate = self.level().getPlayerByUUID(teammateId);
            if (teammate != null && GameFunctions.isPlayerAliveAndSurvival(teammate)) continue;
            Role role = game.getRole(teammateId);
            if (role == null || !role.canUseKiller()) continue;
            targets.add(teammateId);
        }
        return targets;
    }

    @Nullable
    public static PlayerSkin disguiseSkin(Player player) {
        if (BrinMorphlingAccess.morphTicks(player) <= 0) return null;
        return skin(BrinMorphlingAccess.disguise(player));
    }

    @Nullable
    public static Component disguiseName(Player player) {
        if (BrinMorphlingAccess.morphTicks(player) <= 0) return null;
        UUID disguiseId = BrinMorphlingAccess.disguise(player);
        if (disguiseId == null) return null;
        Player living = player.level().getPlayerByUUID(disguiseId);
        if (living != null) return living.getDisplayName();
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        PlayerInfo info = connection == null ? null : connection.getPlayerInfo(disguiseId);
        return info == null ? null : Component.literal(info.getProfile().getName());
    }

    @Nullable
    public static PlayerSkin skin(UUID playerId) {
        if (playerId == null) return null;
        Minecraft client = Minecraft.getInstance();
        if (client.level != null && client.level.getPlayerByUUID(playerId) instanceof Player living) {
            ClientPacketListener connection = client.getConnection();
            PlayerInfo info = connection == null ? null : connection.getPlayerInfo(playerId);
            if (info != null) return info.getSkin();
        }
        ClientPacketListener connection = client.getConnection();
        PlayerInfo info = connection == null ? null : connection.getPlayerInfo(playerId);
        return info == null ? null : info.getSkin();
    }

    public static String name(UUID playerId) {
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        PlayerInfo info = connection == null ? null : connection.getPlayerInfo(playerId);
        return info == null ? playerId.toString().substring(0, 8) : info.getProfile().getName();
    }

    public static void sendMorph(UUID targetId) {
        if (targetId == null || BrinMorphlingAccess.morphTicks(Minecraft.getInstance().player) != 0) return;
        ClientPlayNetworking.send(new BrinAbilityC2SPacket(
            BrinAbilityC2SPacket.ABILITY_MORPHLING_MORPH,
            targetId
        ));
    }
}
