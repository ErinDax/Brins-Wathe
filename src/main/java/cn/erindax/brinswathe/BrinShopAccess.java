package cn.erindax.brinswathe;

import cn.erindax.brinswathe.config.BrinConfig;
import cn.erindax.brinswathe.config.BrinConfig.ShopItem;
import cn.erindax.brinswathe.component.BombComponent;
import cn.erindax.brinswathe.component.NightmareComponent;
import cn.erindax.brinswathe.component.PenitentComponent;
import cn.erindax.brinswathe.component.PuppeteerControlComponent;
import cn.erindax.brinswathe.component.ResurrectedComponent;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.PlayerPoisonComponent;
import dev.doctor4t.wathe.cca.PlayerShopComponent;
import dev.doctor4t.wathe.game.GameConstants;
import dev.doctor4t.wathe.game.GameFunctions;
import dev.doctor4t.wathe.api.WatheRoles;
import dev.doctor4t.wathe.index.WatheItems;
import dev.doctor4t.wathe.util.ShopEntry;
import org.BsXinQin.kinswathe.KinsWatheItems;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.entity.player.Player;
import org.agmas.harpymodloader.component.WorldModifierComponent;
import org.agmas.harpymodloader.events.ModifierAssigned;
import org.ladysnake.cca.api.v3.component.ComponentKey;
public final class BrinShopAccess {
    private static final ResourceLocation MASTER_KEY_ID =
        ResourceLocation.fromNamespaceAndPath("noellesroles", "master_key");
    private static final ResourceLocation STUPID_EXPRESS_THIEF_ID =
        ResourceLocation.fromNamespaceAndPath("stupid_express", "thief");
    private static final ResourceLocation STUPID_EXPRESS_ARSONIST_ID =
        ResourceLocation.fromNamespaceAndPath("stupid_express", "arsonist");
    private static final ResourceLocation STUPID_EXPRESS_NECROMANCER_ID =
        ResourceLocation.fromNamespaceAndPath("stupid_express", "necromancer");

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
            || game.isRole(player, WatheRoles.CIVILIAN)
            || NightmareComponent.isNightmareHour(game, player)
            || isRole(game, player, STUPID_EXPRESS_THIEF_ID)
            || isRole(game, player, STUPID_EXPRESS_NECROMANCER_ID)
            || isRole(game, player, BrinNoelleAccess.MIMIC_ID)
            || isRole(game, player, BrinNoelleAccess.NOISEMAKER_ID)
            || isRole(game, player, BrinNoelleAccess.VOODOO_ID);
    }

    public static boolean showsBalanceHud(GameWorldComponent game, Player player) {
        return canUseShopAndEconomy(game, player)
            || isRole(game, player, BrinNoelleAccess.CONDUCTOR_ID)
            || game.isRole(player, WatheRoles.VIGILANTE);
    }
    public static boolean hasNoShop(GameWorldComponent game, Player player) {
        return isRole(game, player, STUPID_EXPRESS_ARSONIST_ID)
            || isThief(game, player)
            || ResurrectedComponent.isResurrected(player);
    }
    public static boolean isThief(GameWorldComponent game, Player player) {
        return isRole(game, player, STUPID_EXPRESS_THIEF_ID);
    }

    public static boolean isArsonist(GameWorldComponent game, Player player) {
        return isRole(game, player, STUPID_EXPRESS_ARSONIST_ID);
    }
    public static List<ShopEntry> getShopEntries(GameWorldComponent game, Player player) {
        if (hasNoShop(game, player)) return List.of();
        if (isRole(game, player, STUPID_EXPRESS_NECROMANCER_ID)) return necromancerShop();
        if (isRole(game, player, BrinNoelleAccess.MIMIC_ID)) return mimicShop();
        if (isRole(game, player, BrinNoelleAccess.NOISEMAKER_ID)) return noisemakerShop();
        if (isRole(game, player, BrinNoelleAccess.VOODOO_ID)) return voodooShop();
        String roleId = BrinRoles.getRoleId(game, player);
        if (roleId == null && game.isRole(player, WatheRoles.CIVILIAN)) {
            roleId = "civilian";
        }
        if (roleId == null && game.isRole(player, WatheRoles.VIGILANTE)) {
            roleId = "vigilante";
        }
        if (roleId == null) return null;

        List<ShopEntry> entries = switch (roleId) {
            case "civilian", "medium", "eavesdropper", "watchman", "archivist" ->
                civilianRevolverShop(roleId);
            case "vigilante" -> List.of();
            case "nightmare" -> NightmareComponent.isNightmareHour(game, player)
                ? GameConstants.SHOP_ENTRIES.stream()
                    .filter(entry -> !isFirearm(entry.stack()))
                    .toList()
                : List.of();
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
            case "beast_trapper" -> Stream.concat(
                Stream.of(new TrapperExtraTrapShopEntry()),
                GameConstants.SHOP_ENTRIES.stream()
                    .filter(entry -> !entry.stack().is(WatheItems.PSYCHO_MODE))
            ).toList();
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
            case "illusionist", "mortician" -> GameConstants.SHOP_ENTRIES;
            case "zhangshi" -> Stream.concat(
                GameConstants.SHOP_ENTRIES.stream(),
                Stream.of(new ShopEntry(
                    BrinItems.XUEZI.getDefaultInstance(),
                    BrinConfig.xueziPrice(),
                    ShopEntry.Type.TOOL
                ))
            ).toList();
            case "bomber" -> Stream.concat(
                Stream.of(new BomberBombShopEntry(), new BomberMineShopEntry()),
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
    public static boolean ignoresOwnedCheck(ShopEntry entry) {
        return entry instanceof NecromancerRefreshShopEntry
            || entry instanceof NoisemakerFirecrackerShopEntry
            || entry instanceof MiceyesShopEntry;
    }
    public static boolean isCreditPurchase(ShopEntry entry) {
        return entry instanceof NoisemakerShoutShopEntry;
    }
    public static boolean isFirearm(ItemStack stack) {
        return stack.is(WatheItems.REVOLVER) || stack.is(WatheItems.DERRINGER);
    }

    public static boolean isBomberShopBomb(ItemStack stack) {
        return stack.is(BrinItems.BOMB);
    }

    public static boolean isBomberShopMine(ItemStack stack) {
        return stack.is(BrinItems.MINE);
    }

    public static boolean isSurvivalExpertExcludedItem(ItemStack stack) {
        return stack.is(WatheItems.LOCKPICK)
            || stack.is(KinsWatheItems.MEDICAL_KIT)
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
    private static final class BomberBombShopEntry extends ShopEntry {
        private BomberBombShopEntry() {
            super(BrinItems.BOMB.getDefaultInstance(), BrinConfig.bomberBombPrice(), Type.TOOL);
        }
        @Override
        public boolean onBuy(Player player) {
            if (!(player instanceof ServerPlayer serverPlayer)) return false;
            BombComponent component = BombComponent.KEY.get(serverPlayer);
            if (component == null) return false;

            int remaining = component.bombPurchaseCooldownTicks();
            if (remaining > 0) {
                serverPlayer.displayClientMessage(
                    Component.translatable(
                        "message.brinswathe.bomber.bomb_purchase_cooldown",
                        Mth.ceil(remaining / 20.0F)
                    ).withStyle(ChatFormatting.RED),
                    false
                );
                return false;
            }
            if (!super.onBuy(player)) return false;
            component.startBombPurchaseCooldown(BrinConfig.bomberBombPurchaseCooldownSeconds());
            return true;
        }
    }
    private static final class BomberMineShopEntry extends ShopEntry {
        private BomberMineShopEntry() {
            super(BrinItems.MINE.getDefaultInstance(), BrinConfig.bomberMinePrice(), Type.TOOL);
        }
        @Override
        public boolean onBuy(Player player) {
            if (!(player instanceof ServerPlayer serverPlayer)) return false;
            BombComponent component = BombComponent.KEY.get(serverPlayer);
            if (component == null) return false;
            int remaining = component.minePurchaseCooldownTicks();
            if (remaining > 0) {
                serverPlayer.displayClientMessage(
                    Component.translatable(
                        "message.brinswathe.bomber.mine_purchase_cooldown",
                        Mth.ceil(remaining / 20.0F)
                    ).withStyle(ChatFormatting.RED),
                    false
                );
                return false;
            }
            if (serverPlayer.getInventory().contains(BrinItems.MINE.getDefaultInstance())) return false;
            if (!serverPlayer.addItem(BrinItems.MINE.getDefaultInstance())) return false;
            component.startMinePurchaseCooldown(BrinConfig.bomberBombPurchaseCooldownSeconds());
            return true;
        }
    }
    private static final class TrapperExtraTrapShopEntry extends ShopEntry {
        private TrapperExtraTrapShopEntry() {
            super(extraTrapStack(), BrinConfig.trapperExtraTrapPrice(), Type.TOOL);
        }
        @Override
        public boolean onBuy(Player player) {
            if (!(player instanceof ServerPlayer serverPlayer)) return false;
            return TrapperTraps.place(serverPlayer, true);
        }
    }
    private static ItemStack extraTrapStack() {
        ItemStack stack = BrinItems.EXTRA_TRAP.getDefaultInstance();
        stack.set(DataComponents.CUSTOM_NAME, Component.translatable("item.brinswathe.extra_trap"));
        return stack;
    }
    private static List<ShopEntry> voodooShop() {
        return List.of(new ShopEntry(voodooNightVisionStack(), 300, ShopEntry.Type.TOOL));
    }
    private static ItemStack voodooNightVisionStack() {
        ItemStack stack = new ItemStack(Items.SPLASH_POTION);
        stack.set(
            DataComponents.POTION_CONTENTS,
            new PotionContents(
                java.util.Optional.empty(),
                java.util.Optional.empty(),
                List.of(new MobEffectInstance(MobEffects.NIGHT_VISION, 1200, 0, false, true, true))
            )
        );
        stack.set(DataComponents.CUSTOM_NAME, Component.literal("夜视药水瓶"));
        return stack;
    }
    private static List<ShopEntry> mimicShop() {
        return List.of(
            new ShopEntry(WatheItems.LOCKPICK.getDefaultInstance(), 50, ShopEntry.Type.TOOL),
            new ShopEntry(delusionVialStack(), 30, ShopEntry.Type.POISON),
            new ShopEntry(WatheItems.FIRECRACKER.getDefaultInstance(), 5, ShopEntry.Type.TOOL),
            new ShopEntry(WatheItems.NOTE.getDefaultInstance(), 5, ShopEntry.Type.TOOL),
            new ShopEntry(WatheItems.REVOLVER.getDefaultInstance(), 300, ShopEntry.Type.WEAPON),
            new MiceyesShopEntry()
        );
    }
    private static List<ShopEntry> noisemakerShop() {
        return List.of(
            new NoisemakerFirecrackerShopEntry(),
            new NoisemakerShoutShopEntry()
        );
    }
    private static ItemStack delusionVialStack() {
        Item item = BuiltInRegistries.ITEM.get(
            ResourceLocation.fromNamespaceAndPath("noellesroles", "delusion_vial")
        );
        return item == Items.AIR ? Items.POTION.getDefaultInstance() : item.getDefaultInstance();
    }
    private static ItemStack miceyesStack() {
        ItemStack stack = Items.PAPER.getDefaultInstance();
        stack.set(DataComponents.CUSTOM_NAME, Component.literal("杀手之眼").withStyle(ChatFormatting.WHITE, ChatFormatting.ITALIC));
        stack.set(DataComponents.LORE, new ItemLore(List.of(
            Component.literal("购买杀手之眼").withStyle(ChatFormatting.GRAY),
            Component.literal("你已经学会了像杀手一样观察").withStyle(ChatFormatting.GRAY),
            Component.literal("和他们一样，拥有\"本能\"").withStyle(ChatFormatting.RED)
        )));
        return stack;
    }
    private static ItemStack noisemakerShoutStack() {
        ItemStack stack = WatheItems.FIRECRACKER.getDefaultInstance();
        stack.set(DataComponents.CUSTOM_NAME, Component.literal("大叫").withStyle(ChatFormatting.YELLOW));
        stack.set(DataComponents.LORE, new ItemLore(List.of(
            Component.literal("自身与标记目标发光 30 秒").withColor(0xAAAAAA)
        )));
        return stack;
    }
    private static List<ShopEntry> necromancerShop() {
        return List.of(
            new NecromancerRefreshShopEntry(),
            new ShopEntry(WatheItems.KNIFE.getDefaultInstance(), 200, ShopEntry.Type.WEAPON),
            new ShopEntry(WatheItems.GRENADE.getDefaultInstance(), 350, ShopEntry.Type.WEAPON),
            new ShopEntry(WatheItems.FIRECRACKER.getDefaultInstance(), 10, ShopEntry.Type.TOOL),
            new ShopEntry(WatheItems.LOCKPICK.getDefaultInstance(), 50, ShopEntry.Type.TOOL),
            new ShopEntry(WatheItems.CROWBAR.getDefaultInstance(), 25, ShopEntry.Type.TOOL),
            new ShopEntry(WatheItems.BODY_BAG.getDefaultInstance(), 200, ShopEntry.Type.TOOL),
            new ShopEntry(new ItemStack(WatheItems.NOTE, 4), 10, ShopEntry.Type.TOOL)
        );
    }
    private static ItemStack necromancerRefreshStack() {
        ItemStack stack = new ItemStack(WatheItems.PSYCHO_MODE);
        stack.set(DataComponents.CUSTOM_NAME, Component.literal("刷新CD").withStyle(ChatFormatting.DARK_PURPLE));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        stack.set(DataComponents.LORE, new ItemLore(List.of(
            Component.literal("刷新CD并且增加一次复活次数").withColor(0xAAAAAA)
        )));
        return stack;
    }
    private static boolean refreshStupidAbility(Player player) {
        try {
            Class<?> clazz = Class.forName("pro.fazeclan.river.stupid_express.cca.AbilityCooldownComponent");
            Object key = clazz.getField("KEY").get(null);
            if (!(key instanceof ComponentKey<?> componentKey)) return false;
            Object component = componentKey.get(player);
            if (component == null) return false;
            clazz.getMethod("setCooldown", int.class).invoke(component, 0);
            clazz.getMethod("sync").invoke(component);
            return true;
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            return false;
        }
    }

    private static boolean addNecromancerRevive(Player player) {
        try {
            Class<?> clazz = Class.forName(
                "pro.fazeclan.river.stupid_express.role.necromancer.cca.NecromancerComponent"
            );
            Object key = clazz.getField("KEY").get(null);
            if (!(key instanceof ComponentKey<?> componentKey)) return false;
            Object component = componentKey.get(player.level());
            if (component == null) return false;
            clazz.getMethod("increaseAvailableRevives").invoke(component);
            clazz.getMethod("sync").invoke(component);
            return true;
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            return false;
        }
    }
    static final class NecromancerRefreshShopEntry extends ShopEntry {
        private NecromancerRefreshShopEntry() {
            super(necromancerRefreshStack(), 350, Type.WEAPON);
        }
        @Override
        public boolean onBuy(Player player) {
            return refreshStupidAbility(player) && addNecromancerRevive(player);
        }
    }
    static final class NoisemakerFirecrackerShopEntry extends ShopEntry {
        private NoisemakerFirecrackerShopEntry() {
            super(WatheItems.FIRECRACKER.getDefaultInstance(), 75, Type.TOOL);
        }
        @Override
        public boolean onBuy(Player player) {
            return insertStackInFreeSlot(player, WatheItems.FIRECRACKER.getDefaultInstance());
        }
    }
    static final class NoisemakerShoutShopEntry extends ShopEntry {
        private NoisemakerShoutShopEntry() {
            super(noisemakerShoutStack(), 100, Type.TOOL);
        }
        @Override
        public boolean onBuy(Player player) {
            if (!(player instanceof ServerPlayer serverPlayer)) return false;
            if (player.getCooldowns().isOnCooldown(WatheItems.FIRECRACKER)) {
                player.displayClientMessage(Component.literal("物品冷却中").withStyle(ChatFormatting.YELLOW), true);
                return true;
            }
            PlayerShopComponent shop = PlayerShopComponent.KEY.get(player);
            if (shop == null) return false;
            if (shop.balance >= 0) {
                shop.balance -= 100;
                shop.sync();
                player.displayClientMessage(Component.literal("购买成功！花费 100 金币").withStyle(ChatFormatting.GREEN), true);
                player.getCooldowns().addCooldown(WatheItems.FIRECRACKER, 800);
            } else {
                player.getCooldowns().addCooldown(WatheItems.FIRECRACKER, 2400);
                player.displayClientMessage(Component.literal("金币不足！赊账购买，物品进入120秒冷却").withStyle(ChatFormatting.DARK_PURPLE), true);
            }
            applyNoisemakerGlow(serverPlayer);
            return true;
        }
    }
    static final class MiceyesShopEntry extends ShopEntry {
        private MiceyesShopEntry() {
            super(miceyesStack(), 350, Type.TOOL);
        }
        @Override
        public boolean onBuy(Player player) {
            if (!(player instanceof ServerPlayer)) return false;
            if (!insertStackInFreeSlot(player, stack().copy())) return false;
            if (BrinModifiers.hasModifier(player, BrinModifiers.MICEYES) || BrinIcModifiers.MICEYES == null) {
                return true;
            }
            WorldModifierComponent modifiers = WorldModifierComponent.KEY.get(player.level());
            modifiers.addModifier(player.getUUID(), BrinIcModifiers.MICEYES);
            ModifierAssigned.EVENT.invoker().assignModifier(player, BrinIcModifiers.MICEYES);
            return true;
        }
    }
    private static void applyNoisemakerGlow(ServerPlayer player) {
        player.addEffect(new MobEffectInstance(MobEffects.GLOWING, 600, 0, false, false));
        UUID targetId = BrinNoelleAccess.voodooTarget(player);
        if (targetId == null) return;
        Player target = player.level().getPlayerByUUID(targetId);
        if (target != null && GameFunctions.isPlayerAliveAndSurvival(target) && target != player) {
            target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 600, 0, false, false));
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
