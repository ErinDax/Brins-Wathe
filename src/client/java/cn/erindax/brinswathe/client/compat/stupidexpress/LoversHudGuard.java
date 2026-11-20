package cn.erindax.brinswathe.client.compat.stupidexpress;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;

@Environment(EnvType.CLIENT)
public final class LoversHudGuard {
    private LoversHudGuard() {
    }

    public static List<UUID> loadedOnly(List<UUID> lovers) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return lovers;
        List<UUID> loaded = new ArrayList<>(lovers);
        loaded.removeIf(lover -> level.getPlayerByUUID(lover) == null);
        return loaded;
    }
}
