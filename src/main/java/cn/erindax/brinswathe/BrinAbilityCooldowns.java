package cn.erindax.brinswathe;

import cn.erindax.brinswathe.component.BombComponent;
import cn.erindax.brinswathe.component.NightmareComponent;
import cn.erindax.brinswathe.component.PuppeteerControlComponent;
import cn.erindax.brinswathe.component.SniperComponent;
import net.minecraft.world.entity.player.Player;

public final class BrinAbilityCooldowns {
    private BrinAbilityCooldowns() {
    }
    public static void refreshKillerAbilityCooldowns(Player player) {
        SniperComponent sniper = SniperComponent.KEY.get(player);
        if (sniper != null) sniper.clearCooldown();
        PuppeteerControlComponent puppeteer = PuppeteerControlComponent.KEY.get(player);
        if (puppeteer != null) puppeteer.clearCraftCooldown();

        NightmareComponent nightmare = NightmareComponent.KEY.get(player);
        if (nightmare != null) nightmare.clearForcedSleepTaskCooldown();

        BombComponent bomb = BombComponent.KEY.get(player);
        if (bomb != null) bomb.clearPurchaseCooldown();
    }
}
