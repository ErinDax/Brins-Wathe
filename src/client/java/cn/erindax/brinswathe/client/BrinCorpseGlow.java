package cn.erindax.brinswathe.client;

import cn.erindax.brinswathe.BrinRoles;
import cn.erindax.brinswathe.component.IllusionistComponent;
import dev.doctor4t.wathe.api.WatheRoles;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.entity.PlayerBodyEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;

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
        if (!game.isRole(viewer, WatheRoles.VIGILANTE) && !game.isRole(viewer, BrinRoles.ARCHIVIST)) return false;
        return !body.isInvisible() && !IllusionistComponent.isIllusionModel(body);
    }
}
