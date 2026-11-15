package cn.erindax.brinswathe.client;

import cn.erindax.brinswathe.client.role.MorphlingClient;
import cn.erindax.brinswathe.component.MorticianComponent;
import cn.erindax.brinswathe.component.PuppeteerControlComponent;
import dev.doctor4t.wathe.client.WatheClient;
import dev.doctor4t.wathe.entity.PlayerBodyEntity;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public final class DisguiseClient {
    private DisguiseClient() {
    }

    static void tickMorticianDisguiseSkins(Minecraft client) {
        if (client.level == null) return;
        ClientPacketListener connection = client.getConnection();
        if (connection == null) return;
        for (Entity entity : client.level.entitiesForRendering()) {
            if (!(entity instanceof PlayerBodyEntity body)) continue;
            if (!MorticianComponent.isDisguiseBody(body)) continue;
            UUID skinId = body.getPlayerUuid();
            if (skinId == null || WatheClient.PLAYER_ENTRIES_CACHE.get(skinId) != null) continue;
            PlayerInfo info = connection.getPlayerInfo(skinId);
            if (info != null) WatheClient.PLAYER_ENTRIES_CACHE.put(skinId, info);
        }
    }

    @Nullable
    public static Component disguisedName(Player player) {
        MorticianComponent mortician = MorticianComponent.KEY.get(player);
        if (mortician != null && mortician.isDisguised()) {
            return borrowedBodyName(player, mortician.disguiseBodyId);
        }
        PuppeteerControlComponent puppeteer = PuppeteerControlComponent.KEY.get(player);
        if (puppeteer != null && puppeteer.isControlling()) {
            return borrowedBodyName(player, puppeteer.puppetEntityId);
        }
        Component morphName = MorphlingClient.disguiseName(player);
        if (morphName != null) return morphName;
        return null;
    }

    private static Component borrowedBodyName(Player player, @Nullable UUID bodyId) {
        Entity body = AimClient.findClientEntity(Minecraft.getInstance(), bodyId);
        UUID skinId = body instanceof PlayerBodyEntity playerBody ? playerBody.getPlayerUuid() : null;
        if (skinId == null) return Component.empty();
        if (player.level().getPlayerByUUID(skinId) instanceof Player skinOwner) {
            return skinOwner.getDisplayName();
        }

        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        PlayerInfo info = connection == null ? null : connection.getPlayerInfo(skinId);
        return info == null ? Component.empty() : Component.literal(info.getProfile().getName());
    }
}
