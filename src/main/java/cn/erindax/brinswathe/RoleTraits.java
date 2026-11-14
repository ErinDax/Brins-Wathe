package cn.erindax.brinswathe;

import cn.erindax.brinswathe.component.NightmareComponent;
import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.util.ShopEntry;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public final class RoleTraits {
    @FunctionalInterface
    public interface Shop {
        List<ShopEntry> entries(GameWorldComponent game, Player player, String roleId);
    }

    private static final Map<ResourceLocation, RoleTraits> BY_ROLE = new HashMap<>();
    private static final Map<String, RoleTraits> BY_CONFIG_ID = new HashMap<>();
    private static final RoleTraits NONE = new RoleTraits(null);

    static {
        wathe("civilian").economy().shop(ShopAccess::civilianRevolverShop);
        wathe("vigilante").balanceHud().skilledCivilian().shop(ShopAccess::emptyShop);

        brin("puppeteer").shop(ShopAccess::puppeteerShop);
        brin("stunt_double").skilledCivilian();
        brin("medium").economy().skilledCivilian().shop(ShopAccess::emptyShop);
        brin("conspirator").economy().skilledCivilian().shop(ShopAccess::civilianRevolverShop);
        brin("watchman").economy().skilledCivilian().shop(ShopAccess::civilianRevolverShop);
        brin("beastcatcher", "beast_trapper").shop(ShopAccess::trapperShop);
        brin("nightmare").economyWhen(NightmareComponent::isNightmareHour).shop(ShopAccess::nightmareShop);
        brin("illusionist").shop(ShopAccess::standardShop);
        brin("sniper").shop(ShopAccess::sniperShop);
        brin("berserker").economy().shop(ShopAccess::berserkerShop);
        brin("archivist").economy().skilledCivilian().shop(ShopAccess::civilianRevolverShop);
        brin("boneharvester").shop(ShopAccess::boneharvesterShop);
        brin("compensator");
        brin("gambler").economy().shop(ShopAccess::gamblerShop);
        brin("penitent").economy().shop(ShopAccess::penitentShop);
        brin("terrorist").shop(ShopAccess::emptyShop);
        brin("bomber").shop(ShopAccess::bomberShop);
        brin("zhangshi").shop(ShopAccess::zhangshiShop);
        brin("mortician").shop(ShopAccess::standardShop);
        brin("avenger").skilledCivilian().shop(ShopAccess::emptyShop);
        brin("stalker").skilledCivilian().shop(ShopAccess::emptyShop);
        brin("cowboy").economy().skilledCivilian().shop(ShopAccess::emptyShop);
        brin("valentina").skilledCivilian().shop(ShopAccess::emptyShop);
        brin("subverter").shop(ShopAccess::emptyShop);
        brin("inspector").balanceHud().skilledCivilian().shop(ShopAccess::emptyShop);
        brin("zhuimu").balanceHud().skilledCivilian().shop(ShopAccess::emptyShop);
        brin("spy").shop(ShopAccess::emptyShop);
        brin("grappler").shop(ShopAccess::standardShop);
        brin("half_spirit").shop(ShopAccess::standardShop);

        other(ShopAccess.STUPID_EXPRESS_THIEF_ID).economy().shopClosed();
        other(ShopAccess.STUPID_EXPRESS_ARSONIST_ID).shopClosed();
        other(ShopAccess.STUPID_EXPRESS_NECROMANCER_ID).economy().shop(ShopAccess::necromancerShop);
        other(NoelleAccess.MIMIC_ID).economy().shop(ShopAccess::mimicShop);
        other(NoelleAccess.NOISEMAKER_ID).economy().shop(ShopAccess::noisemakerShop);
        other(NoelleAccess.VOODOO_ID).economy().shop(ShopAccess::voodooShop);
        other(NoelleAccess.CONDUCTOR_ID).balanceHud();
    }

    @Nullable
    private final String configId;
    @Nullable
    private BiPredicate<GameWorldComponent, Player> economy;
    private boolean balanceHud;
    private boolean skilledCivilian;
    private boolean shopClosed;
    @Nullable
    private Shop shop;

    private RoleTraits(@Nullable String configId) {
        this.configId = configId;
    }

    public static RoleTraits of(GameWorldComponent game, Player player) {
        Role role = game.getRole(player);
        return role == null ? NONE : BY_ROLE.getOrDefault(role.identifier(), NONE);
    }

    public static RoleTraits byConfigId(@Nullable String roleId) {
        return BY_CONFIG_ID.getOrDefault(roleId, NONE);
    }

    @Nullable
    public String configId() {
        return this.configId;
    }

    public boolean hasEconomy(GameWorldComponent game, Player player) {
        return this.economy != null && this.economy.test(game, player);
    }

    public boolean showsBalanceHud() {
        return this.balanceHud;
    }

    public boolean isSkilledCivilian() {
        return this.skilledCivilian;
    }

    public boolean isShopClosed() {
        return this.shopClosed;
    }

    @Nullable
    public List<ShopEntry> shopEntries(GameWorldComponent game, Player player) {
        return this.shop == null ? null : this.shop.entries(game, player, this.configId);
    }

    private static RoleTraits wathe(String path) {
        return register(ResourceLocation.fromNamespaceAndPath("wathe", path), path);
    }

    private static RoleTraits brin(String path) {
        return brin(path, path);
    }

    private static RoleTraits brin(String path, String configId) {
        return register(ResourceLocation.fromNamespaceAndPath("brin", path), configId);
    }

    private static RoleTraits other(ResourceLocation role) {
        return register(role, null);
    }

    private static RoleTraits register(ResourceLocation role, @Nullable String configId) {
        RoleTraits traits = new RoleTraits(configId);
        BY_ROLE.put(role, traits);
        if (configId != null) BY_CONFIG_ID.put(configId, traits);
        return traits;
    }

    private RoleTraits economy() {
        return this.economyWhen((game, player) -> true);
    }

    private RoleTraits economyWhen(BiPredicate<GameWorldComponent, Player> condition) {
        this.economy = condition;
        return this;
    }

    private RoleTraits balanceHud() {
        this.balanceHud = true;
        return this;
    }

    private RoleTraits skilledCivilian() {
        this.skilledCivilian = true;
        return this;
    }

    private RoleTraits shopClosed() {
        this.shopClosed = true;
        return this;
    }

    private RoleTraits shop(Shop shop) {
        this.shop = shop;
        return this;
    }
}
