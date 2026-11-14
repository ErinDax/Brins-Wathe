package cn.erindax.brinswathe;

import cn.erindax.brinswathe.component.MuzzlerAbilityComponent;
import cn.erindax.brinswathe.config.Tunable;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.PlayerShopComponent;
import net.minecraft.server.level.ServerPlayer;
import org.aussiebox.starexpress.StarryExpressRoles;
import org.aussiebox.starexpress.cca.AbilityComponent;

public final class Muzzler {
    private Muzzler() {
    }

    public static void handleSilence(ServerPlayer player) {
        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(player.level());
        if (!gameWorld.isRole(player, StarryExpressRoles.MUZZLER)) return;
        AbilityComponent ability = AbilityComponent.KEY.get(player);
        if (ability == null || ability.cooldown > 0) return;
        MuzzlerAbilityComponent muzzler = MuzzlerAbilityComponent.KEY.get(player);
        if (muzzler == null || muzzler.getTicks() > 0) return;
        PlayerShopComponent shop = PlayerShopComponent.KEY.get(player);
        int cost = Tunable.MUZZLER_SILENCE_COST.intValue();
        if (shop == null || shop.balance < cost) return;
        shop.balance -= cost;
        shop.sync();
        ability.setCooldown(Tunable.MUZZLER_SILENCE_COOLDOWN_SECONDS.ticks());
        muzzler.setTicks(Tunable.MUZZLER_SILENCE_DURATION_SECONDS.ticks());
    }
}
