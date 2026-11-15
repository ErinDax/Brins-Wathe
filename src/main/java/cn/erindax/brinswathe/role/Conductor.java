package cn.erindax.brinswathe.role;

import cn.erindax.brinswathe.NoelleAccess;
import cn.erindax.brinswathe.config.Tunable;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.PlayerShopComponent;
import net.minecraft.server.level.ServerPlayer;

public final class Conductor {
    private Conductor() {
    }

    public static void handleArmor(ServerPlayer player, GameWorldComponent gameWorld) {
        if (!NoelleAccess.isRole(gameWorld, player, NoelleAccess.CONDUCTOR_ID)) return;
        if (NoelleAccess.noelleAbilityCooldown(player) > 0) return;
        PlayerShopComponent shop = PlayerShopComponent.KEY.get(player);
        int cost = Tunable.CONDUCTOR_ARMOR_COST.intValue();
        if (shop == null || shop.balance < cost) return;
        if (!NoelleAccess.addBartenderArmor(player)) return;
        shop.balance -= cost;
        shop.sync();
        NoelleAccess.setNoelleAbilityCooldown(player, Tunable.CONDUCTOR_ARMOR_COOLDOWN_SECONDS.ticks());
    }
}
