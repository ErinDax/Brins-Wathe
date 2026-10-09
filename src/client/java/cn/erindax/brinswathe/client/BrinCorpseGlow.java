package cn.erindax.brinswathe.client;

import cn.erindax.brinswathe.BrinRoles;
import cn.erindax.brinswathe.component.IllusionistComponent;
import cn.erindax.brinswathe.component.PuppeteerControlComponent;
import dev.doctor4t.wathe.api.WatheRoles;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.entity.PlayerBodyEntity;
import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.GameType;

@Environment(EnvType.CLIENT)
public final class BrinCorpseGlow {
    public static final int COLOUR = 0xFF606060;
    private BrinCorpseGlow() {
    }

    public static boolean isGlowingCorpse(Entity entity) {
        if (!(entity instanceof PlayerBodyEntity body)) return false;
        Minecraft client = Minecraft.getInstance();
        LocalPlayer viewer = client.player;
        if (viewer == null || client.level == null) return false;
        GameWorldComponent game = GameWorldComponent.KEY.get(client.level);
        boolean vigilante = game.isRole(viewer, WatheRoles.VIGILANTE) && !hasLivingMortician(client, game);
        if (!vigilante && !game.isRole(viewer, BrinRoles.ARCHIVIST)) return false;
        return !body.isInvisible()
            && !IllusionistComponent.isIllusionModel(body)
            && !PuppeteerControlComponent.isPuppetModel(body);
    }

    private static boolean hasLivingMortician(Minecraft client, GameWorldComponent game) {
        ClientPacketListener connection = client.getConnection();
        if (connection == null) return false;
        for (UUID id : game.getAllWithRole(BrinRoles.MORTICIAN)) {
            PlayerInfo info = connection.getPlayerInfo(id);
            if (info == null) continue;
            GameType mode = info.getGameMode();
            if (mode != GameType.SPECTATOR && mode != GameType.CREATIVE) return true;
        }
        return false;
    }
}
