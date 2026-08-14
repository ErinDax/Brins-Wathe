package cn.autoforged.brinswathe;

import cn.autoforged.brinswathe.config.BrinConfig;
import cn.autoforged.brinswathe.config.BrinConfig.ShopItem;
import cn.autoforged.brinswathe.component.BombComponent;
import cn.autoforged.brinswathe.component.PenitentComponent;
import cn.autoforged.brinswathe.component.PuppeteerControlComponent;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.PlayerPoisonComponent;
import dev.doctor4t.wathe.game.GameConstants;
import dev.doctor4t.wathe.api.WatheRoles;
import dev.doctor4t.wathe.index.WatheItems;
import dev.doctor4t.wathe.util.ShopEntry;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.entity.player.Player;
public final class BrinShopAccess {
    private static final ResourceLocation MASTER_KEY_ID =
        ResourceLocation.fromNamespaceAndPath("noellesroles", "master_key");
    private static final ResourceLocation STUPID_EXPRESS_THIEF_ID =
        ResourceLocation.fromNamespaceAndPath("stupid_express", "thief");
    private static final ResourceLocation STUPID_EXPRESS_ARSONIST_ID =
        ResourceLocation.fromNamespaceAndPath("stupid_express", "arsonist");

    private BrinShopAccess() {
    }

    public static boolean canUseShopAndEconomy(GameWorldComponent game, Player player) {
        return game.isRole(player, BrinRoles.MEDIUM)
            || game.isRole(player, BrinRoles.EAVESDROPPER)
            || game.isRole(player, BrinRoles.WATCHMAN)
            || game.isRole(player, BrinRoles.ARCHIVIST)
            || game.isRole(player, BrinRoles.BERSERKER)
            || game.isRole(player, BrinRoles.GAMBLER)
            || game.isRole(player, BrinRoles.PENITENT)
            || game.isRole(player, BrinRoles.COWBOY)
            || game.isRole(player, WatheRoles.CIVILIAN);
    }

    /**
     * Neither of Stupid Express' own neutrals trades: both are handed their kit when the role is assigned.
     */
    public static boolean hasNoShop(GameWorldComponent game, Player player) {
        return isRole(game, player, STUPID_EXPRESS_THIEF_ID)
            || isRole(game, player, STUPID_EXPRESS_ARSONIST_ID);
    }

    public static List<ShopEntry> getShopEntries(GameWorldComponent game, Player player) {
        if (hasNoShop(game, player)) return List.of();

        String roleId = BrinRoles.getRoleId(game, player);
        if (roleId == null && game.isRole(player, WatheRoles.CIVILIAN)) {
            roleId = "civilian";
        }
        if (roleId == null) return null;

        List<ShopEntry> entries = switch (roleId) {
            case "civilian", "medium", "eavesdropper", "watchman", "archivist" ->
                civilianRevolverShop(roleId);
            case "puppeteer" -> Stream.concat(
                GameConstants.SHOP_ENTRIES.stream()
                    .filter(entry -> !isFirearm(entry.stack())
                        && !entry.stack().is(WatheItems.PSYCHO_MODE)
                        && !entry.stack().is(WatheItems.GRENADE)),
                Stream.of(new PuppeteerSelfDestructShopEntry())
            ).toList();
            case "sniper" -> GameConstants.SHOP_ENTRIES.stream()
                .filter(entry -> !entry.stack().is(WatheItems.KNIFE)
                    && !entry.stack().is(WatheItems.PSYCHO_MODE))
                .toList();
            case "beast_trapper" -> GameConstants.SHOP_ENTRIES.stream()
                .filter(entry -> !entry.stack().is(WatheItems.PSYCHO_MODE))
                .toList();
            case "gambler" -> GameConstants.SHOP_ENTRIES.stream()
                .filter(entry -> entry.stack().is(WatheItems.FIRECRACKER)
                    || entry.stack().is(WatheItems.LOCKPICK))
                .toList();
            case "penitent" -> Stream.concat(
                GameConstants.SHOP_ENTRIES.stream()
                    .filter(entry -> !entry.stack().is(WatheItems.PSYCHO_MODE)
                        && !entry.stack().is(WatheItems.GRENADE)
                        && !entry.stack().is(WatheItems.BLACKOUT)
                        && !entry.stack().is(WatheItems.CROWBAR)),
                Stream.of(new PenitentIdentityHintShopEntry(), new PenitentAntidoteShopEntry())
            ).toList();
            case "berserker" -> List.of(
                new ShopEntry(
                    WatheItems.PSYCHO_MODE.getDefaultInstance(),
                    BrinConfig.shopPrice(roleId, ShopItem.PSYCHO_MODE),
                    ShopEntry.Type.TOOL
                ),
                new ShopEntry(WatheItems.LOCKPICK.getDefaultInstance(), 50, ShopEntry.Type.TOOL),
                new ShopEntry(WatheItems.CROWBAR.getDefaultInstance(), 25, ShopEntry.Type.TOOL)
            );
            // The ordinary killer shop, listed only so the shared repricing pass below can reach it.
            case "illusionist", "mortician" -> GameConstants.SHOP_ENTRIES;
            case "zhangshi" -> Stream.concat(
                GameConstants.SHOP_ENTRIES.stream(),
                Stream.of(new ShopEntry(
                    BrinItems.XUEZI.getDefaultInstance(),
                    BrinConfig.xueziPrice(),
                    ShopEntry.Type.TOOL
                ))
            ).toList();
            // The bomb leads the list: it is the role's whole identity and gets lost at the tail end.
            case "bomber" -> Stream.concat(
                Stream.of(new BomberBombShopEntry()),
                GameConstants.SHOP_ENTRIES.stream()
                    .filter(entry -> !isFirearm(entry.stack())
                        && !entry.stack().is(WatheItems.PSYCHO_MODE)
                        && !entry.stack().is(WatheItems.KNIFE))
                    .map(entry -> entry.stack().is(WatheItems.GRENADE)
                        ? new RepricedShopEntry(entry, BrinConfig.bomberGrenadePrice())
                        : entry)
            ).toList();
            case "boneharvester" -> java.util.stream.Stream.concat(
                GameConstants.SHOP_ENTRIES.stream(),
                java.util.stream.Stream.of(new ShopEntry(
                    BrinItems.BONE_KNIFE.getDefaultInstance(),
                    BrinConfig.boneKnifePrice(),
                    ShopEntry.Type.TOOL
                ))
            ).toList();
            case "avenger", "stalker", "terrorist", "cowboy" -> List.of();
            default -> null;
        };

        if (entries == null) return null;
        if (game.isRole(player, BrinRoles.WATCHMAN)) {
            entries = entries.stream()
                .filter(entry -> !isSurvivalExpertExcludedItem(entry.stack()))
                .toList();
        }
        String resolvedRoleId = roleId;
        return entries.stream().map(entry -> reprice(entry, resolvedRoleId)).toList();
    }

    /** Both shop firearms, so a role barred from "guns" loses the derringer too. */
    public static boolean isFirearm(ItemStack stack) {
        return stack.is(WatheItems.REVOLVER) || stack.is(WatheItems.DERRINGER);
    }

    /** The survival expert is barred from every door opener, however he came by it. */
    public static boolean isSurvivalExpertExcludedItem(ItemStack stack) {
        return stack.is(WatheItems.LOCKPICK)
            || MASTER_KEY_ID.equals(BuiltInRegistries.ITEM.getKey(stack.getItem()));
    }

    private static boolean isRole(GameWorldComponent game, Player player, ResourceLocation roleId) {
        var role = game.getRole(player);
        return role != null && roleId.equals(role.identifier());
    }

    private static ShopEntry reprice(ShopEntry entry, String roleId) {
        if (entry.stack().is(WatheItems.KNIFE)) {
            return new RepricedShopEntry(entry, BrinConfig.shopPrice(roleId, ShopItem.KNIFE));
        }
        if (entry.stack().is(WatheItems.REVOLVER)) {
            return new RepricedShopEntry(entry, BrinConfig.shopPrice(roleId, ShopItem.REVOLVER));
        }
        if (entry.stack().is(WatheItems.PSYCHO_MODE)) {
            return new RepricedShopEntry(entry, BrinConfig.shopPrice(roleId, ShopItem.PSYCHO_MODE));
        }
        return entry;
    }

    private static List<ShopEntry> civilianRevolverShop(String roleId) {
        if (!BrinConfig.revolverShopEnabled(roleId)) return List.of();
        return List.of(new ShopEntry(
            WatheItems.REVOLVER.getDefaultInstance(),
            BrinConfig.shopPrice(roleId, ShopItem.REVOLVER),
            ShopEntry.Type.WEAPON
        ));
    }

    private static ItemStack penitentIdentityHintStack() {
        ItemStack stack = Items.PAPER.getDefaultInstance();
        stack.set(DataComponents.CUSTOM_NAME, Component.translatable("item.brinswathe.penitent_identity_hint"));
        return stack;
    }

    private static ItemStack penitentAntidoteStack() {
        ItemStack stack;
        try {
            Class<?> modItems = Class.forName("org.agmas.noellesroles.ModItems");
            net.minecraft.world.item.Item defenseVial =
                (net.minecraft.world.item.Item) modItems.getField("DEFENSE_VIAL").get(null);
            stack = defenseVial.getDefaultInstance();
        } catch (ReflectiveOperationException exception) {
            stack = Items.GLASS_BOTTLE.getDefaultInstance();
        }
        stack.set(DataComponents.CUSTOM_NAME, Component.translatable("item.brinswathe.penitent_antidote"));
        return stack;
    }

    private static ItemStack puppeteerSelfDestructStack() {
        ItemStack stack = Items.TNT.getDefaultInstance();
        stack.set(DataComponents.CUSTOM_NAME, Component.translatable("item.brinswathe.puppeteer_self_destruct"));
        return stack;
    }

    private static final class PuppeteerSelfDestructShopEntry extends ShopEntry {
        private PuppeteerSelfDestructShopEntry() {
            super(puppeteerSelfDestructStack(), BrinConfig.puppeteerSelfDestructPrice(), Type.TOOL);
        }

        @Override
        public boolean onBuy(Player player) {
            if (!(player instanceof ServerPlayer serverPlayer)) return false;
            PuppeteerControlComponent component = PuppeteerControlComponent.KEY.get(serverPlayer);
            if (component == null || component.selfDestructArmed) return false;
            component.selfDestructArmed = true;
            component.sync();
            return true;
        }
    }

    private static final class PenitentIdentityHintShopEntry extends ShopEntry {
        private PenitentIdentityHintShopEntry() {
            super(penitentIdentityHintStack(), BrinConfig.penitentIdentityHintPrice(), Type.TOOL);
        }

        @Override
        public boolean onBuy(Player player) {
            if (!(player instanceof ServerPlayer serverPlayer)) return false;
            PenitentComponent component = PenitentComponent.KEY.get(serverPlayer);
            return component != null && component.revealTarget(serverPlayer);
        }
    }

    private static final class PenitentAntidoteShopEntry extends ShopEntry {
        private PenitentAntidoteShopEntry() {
            super(penitentAntidoteStack(), BrinConfig.penitentAntidotePrice(), Type.POISON);
        }

        @Override
        public boolean onBuy(Player player) {
            if (!(player instanceof ServerPlayer serverPlayer)) return false;
            PlayerPoisonComponent poison = PlayerPoisonComponent.KEY.get(serverPlayer);
            if (poison == null || poison.poisonTicks <= 0) return false;
            poison.reset();
            return true;
        }
    }

    /**
     * Restocking is rate limited rather than capped, so the bomber has to spend his one bomb well instead
     * of buying a replacement the moment he plants one.
     */
    private static final class BomberBombShopEntry extends ShopEntry {
        private BomberBombShopEntry() {
            super(BrinItems.BOMB.getDefaultInstance(), BrinConfig.bomberBombPrice(), Type.TOOL);
        }

        @Override
        public boolean onBuy(Player player) {
            if (!(player instanceof ServerPlayer serverPlayer)) return false;
            BombComponent component = BombComponent.KEY.get(serverPlayer);
            if (component == null) return false;

            int remaining = component.purchaseCooldownTicks();
            if (remaining > 0) {
                serverPlayer.displayClientMessage(
                    Component.translatable(
                        "message.brinswathe.bomber.purchase_cooldown",
                        Mth.ceil(remaining / 20.0F)
                    ).withStyle(ChatFormatting.RED),
                    false
                );
                return false;
            }
            if (!super.onBuy(player)) return false;

            component.startPurchaseCooldown(BrinConfig.bomberBombPurchaseCooldownSeconds());
            return true;
        }
    }

    private static final class RepricedShopEntry extends ShopEntry {
        private final ShopEntry delegate;

        private RepricedShopEntry(ShopEntry delegate, int price) {
            super(delegate.stack().copy(), price, delegate.type());
            this.delegate = delegate;
        }

        @Override
        public boolean onBuy(Player player) {
            return this.delegate.onBuy(player);
        }
    }
}
