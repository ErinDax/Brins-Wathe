package cn.erindax.brinswathe;

import cn.erindax.brinswathe.config.ModConfig;
import dev.doctor4t.wathe.cca.PlayerShopComponent;
import net.minecraft.server.level.ServerPlayer;

public final class AbilityCost {
    private AbilityCost() {
    }

    public static boolean spend(ServerPlayer player, String roleId) {
        int cost = ModConfig.skillCost(roleId);
        if (cost == 0) return true;
        PlayerShopComponent shop = PlayerShopComponent.KEY.get(player);
        if (shop == null || shop.balance < cost) return false;
        shop.balance -= cost;
        shop.sync();
        return true;
    }
}
