package cn.erindax.brinswathe;

import java.util.Locale;
import net.minecraft.world.entity.player.Player;

public final class BrinSkinEditors {
    private BrinSkinEditors() {
    }

    public static boolean canEdit(Player player) {
        if (player == null) return false;
        String name = player.getGameProfile().getName();
        if (name == null || name.isBlank()) return false;
        for (String allowed : BrinIcFlags.skinEditors) {
            if (allowed != null && allowed.equalsIgnoreCase(name)) return true;
        }
        return false;
    }

    public static boolean isType(String type) {
        return "knife".equalsIgnoreCase(type) || "gun".equalsIgnoreCase(type);
    }

    public static String normalizeType(String type) {
        return type == null ? "" : type.toLowerCase(Locale.ROOT);
    }
}
