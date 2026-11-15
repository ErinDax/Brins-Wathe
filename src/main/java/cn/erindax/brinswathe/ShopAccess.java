package cn.erindax.brinswathe;

import cn.erindax.brinswathe.config.ModConfig;
import cn.erindax.brinswathe.config.ModConfig.ShopItem;
import cn.erindax.brinswathe.component.BombComponent;
import cn.erindax.brinswathe.component.NightmareComponent;
import cn.erindax.brinswathe.component.PenitentComponent;
import cn.erindax.brinswathe.component.PuppeteerControlComponent;
import cn.erindax.brinswathe.component.ResurrectedComponent;
import cn.erindax.brinswathe.config.Tunable;
import cn.erindax.brinswathe.role.TrapperTraps;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.PlayerPoisonComponent;
import dev.doctor4t.wathe.cca.PlayerShopComponent;
import dev.doctor4t.wathe.game.GameConstants;
import dev.doctor4t.wathe.game.GameFunctions;
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
public final class ShopAccess {
    private static final ResourceLocation MASTER_KEY_ID =
        ResourceLocation.fromNamespaceAndPath("noellesroles", "master_key");
    static final ResourceLocation STUPID_EXPRESS_THIEF_ID =
        ResourceLocation.fromNamespaceAndPath("stupid_express", "thief");
    static final ResourceLocation STUPID_EXPRESS_ARSONIST_ID =
        ResourceLocation.fromNamespaceAndPath("stupid_express", "arsonist");
    static final ResourceLocation STUPID_EXPRESS_NECROMANCER_ID =
        ResourceLocation.fromNamespaceAndPath("stupid_express", "necromancer");

    private ShopAccess() {
    }

    public static boolean canUseShopAndEconomy(GameWorldComponent game, Player player) {
        return RoleTraits.of(game, player).hasEconomy(game, player);
    }

    public static boolean showsBalanceHud(GameWorldComponent game, Player player) {
        RoleTraits traits = RoleTraits.of(game, player);
        return traits.hasEconomy(game, player) || traits.showsBalanceHud();
    }
    public static boolean hasNoShop(GameWorldComponent game, Player player) {
        return RoleTraits.of(game, player).isShopClosed()
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
        RoleTraits traits = RoleTraits.of(game, player);
        String roleId = traits.configId();
        List<ShopEntry> entries = traits.shopEntries(game, player);
        if (entries == null || roleId == null) return entries;
        if (game.isRole(player, ModRoles.WATCHMAN)) {
            entries = entries.stream()
                .filter(entry -> !isSurvivalExpertExcludedItem(entry.stack()))
                .toList();
        }
        return entries.stream().map(entry -> reprice(entry, roleId)).toList();
    }

    static List<ShopEntry> emptyShop(GameWorldComponent game, Player player, String roleId) {
        return List.of();
    }

    static List<ShopEntry> standardShop(GameWorldComponent game, Player player, String roleId) {
        return GameConstants.SHOP_ENTRIES;
    }

    static List<ShopEntry> nightmareShop(GameWorldComponent game, Player player, String roleId) {
        return NightmareComponent.isNightmareHour(game, player)
            ? GameConstants.SHOP_ENTRIES.stream()
                .filter(entry -> !isFirearm(entry.stack()))
                .toList()
            : List.of();
    }

    static List<ShopEntry> puppeteerShop(GameWorldComponent game, Player player, String roleId) {
        return Stream.concat(
            GameConstants.SHOP_ENTRIES.stream()
                .filter(entry -> !isFirearm(entry.stack())),
            Stream.of(new PuppeteerSelfDestructShopEntry())
        ).toList();
    }

    static List<ShopEntry> sniperShop(GameWorldComponent game, Player player, String roleId) {
        return GameConstants.SHOP_ENTRIES.stream()
            .filter(entry -> !entry.stack().is(WatheItems.KNIFE)
                && !entry.stack().is(WatheItems.PSYCHO_MODE))
            .toList();
    }

    static List<ShopEntry> trapperShop(GameWorldComponent game, Player player, String roleId) {
        return Stream.concat(
            Stream.of(new TrapperExtraTrapShopEntry()),
            GameConstants.SHOP_ENTRIES.stream()
                .filter(entry -> !entry.stack().is(WatheItems.PSYCHO_MODE))
        ).toList();
    }

    static List<ShopEntry> gamblerShop(GameWorldComponent game, Player player, String roleId) {
        return GameConstants.SHOP_ENTRIES.stream()
            .filter(entry -> entry.stack().is(WatheItems.FIRECRACKER)
                || entry.stack().is(WatheItems.LOCKPICK))
            .map(entry -> entry.stack().is(WatheItems.LOCKPICK)
                ? new RepricedShopEntry(entry, Tunable.GAMBLER_LOCKPICK_PRICE.intValue())
                : entry)
            .toList();
    }

    static List<ShopEntry> penitentShop(GameWorldComponent game, Player player, String roleId) {
        return Stream.concat(
            GameConstants.SHOP_ENTRIES.stream()
                .filter(entry -> !entry.stack().is(WatheItems.PSYCHO_MODE)
                    && !entry.stack().is(WatheItems.GRENADE)
                    && !entry.stack().is(WatheItems.BLACKOUT)
                    && !entry.stack().is(WatheItems.CROWBAR)),
            Stream.of(new PenitentIdentityHintShopEntry(), new PenitentAntidoteShopEntry())
        ).toList();
    }

    static List<ShopEntry> berserkerShop(GameWorldComponent game, Player player, String roleId) {
        return List.of(
            new ShopEntry(
                WatheItems.PSYCHO_MODE.getDefaultInstance(),
                ModConfig.shopPrice(roleId, ShopItem.PSYCHO_MODE),
                ShopEntry.Type.TOOL
            ),
            new ShopEntry(
                WatheItems.LOCKPICK.getDefaultInstance(),
                Tunable.BERSERKER_LOCKPICK_PRICE.intValue(),
                ShopEntry.Type.TOOL
            ),
            new ShopEntry(
                WatheItems.CROWBAR.getDefaultInstance(),
                Tunable.BERSERKER_CROWBAR_PRICE.intValue(),
                ShopEntry.Type.TOOL
            )
        );
    }

    static List<ShopEntry> zhangshiShop(GameWorldComponent game, Player player, String roleId) {
        return Stream.concat(
            GameConstants.SHOP_ENTRIES.stream(),
            Stream.of(new ShopEntry(
                ModItems.XUEZI.getDefaultInstance(),
                ModConfig.xueziPrice(),
                ShopEntry.Type.TOOL
            ))
        ).toList();
    }

    static List<ShopEntry> bomberShop(GameWorldComponent game, Player player, String roleId) {
        return Stream.concat(
            Stream.of(new BomberBombShopEntry(), new BomberMineShopEntry()),
            GameConstants.SHOP_ENTRIES.stream()
                .filter(entry -> !isFirearm(entry.stack())
                    && !entry.stack().is(WatheItems.PSYCHO_MODE)
                    && !entry.stack().is(WatheItems.KNIFE))
                .map(entry -> entry.stack().is(WatheItems.GRENADE)
                    ? new RepricedShopEntry(entry, ModConfig.bomberGrenadePrice())
                    : entry)
        ).toList();
    }

    static List<ShopEntry> boneharvesterShop(GameWorldComponent game, Player player, String roleId) {
        return java.util.stream.Stream.concat(
            GameConstants.SHOP_ENTRIES.stream(),
            java.util.stream.Stream.of(new ShopEntry(
                ModItems.BONE_KNIFE.getDefaultInstance(),
                ModConfig.boneKnifePrice(),
                ShopEntry.Type.TOOL
            ))
        ).toList();
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
        return stack.is(ModItems.BOMB);
    }

    public static boolean isBomberShopMine(ItemStack stack) {
        return stack.is(ModItems.MINE);
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
            return new RepricedShopEntry(entry, ModConfig.shopPrice(roleId, ShopItem.KNIFE));
        }
        if (entry.stack().is(WatheItems.REVOLVER)) {
            return new RepricedShopEntry(entry, ModConfig.shopPrice(roleId, ShopItem.REVOLVER));
        }
        if (entry.stack().is(WatheItems.PSYCHO_MODE)) {
            return new RepricedShopEntry(entry, ModConfig.shopPrice(roleId, ShopItem.PSYCHO_MODE));
        }
        return entry;
    }
    static List<ShopEntry> civilianRevolverShop(GameWorldComponent game, Player player, String roleId) {
        if (!ModConfig.revolverShopEnabled(roleId)) return List.of();
        return List.of(new ShopEntry(
            WatheItems.REVOLVER.getDefaultInstance(),
            ModConfig.shopPrice(roleId, ShopItem.REVOLVER),
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
            super(puppeteerSelfDestructStack(), ModConfig.puppeteerSelfDestructPrice(), Type.TOOL);
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
            super(penitentIdentityHintStack(), ModConfig.penitentIdentityHintPrice(), Type.TOOL);
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
            super(penitentAntidoteStack(), ModConfig.penitentAntidotePrice(), Type.POISON);
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
            super(ModItems.BOMB.getDefaultInstance(), ModConfig.bomberBombPrice(), Type.TOOL);
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
            component.startBombPurchaseCooldown(ModConfig.bomberBombPurchaseCooldownSeconds());
            return true;
        }
    }
    private static final class BomberMineShopEntry extends ShopEntry {
        private BomberMineShopEntry() {
            super(ModItems.MINE.getDefaultInstance(), ModConfig.bomberMinePrice(), Type.TOOL);
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
            if (serverPlayer.getInventory().contains(ModItems.MINE.getDefaultInstance())) return false;
            if (!serverPlayer.addItem(ModItems.MINE.getDefaultInstance())) return false;
            component.startMinePurchaseCooldown(ModConfig.bomberBombPurchaseCooldownSeconds());
            return true;
        }
    }
    private static final class TrapperExtraTrapShopEntry extends ShopEntry {
        private TrapperExtraTrapShopEntry() {
            super(extraTrapStack(), ModConfig.trapperExtraTrapPrice(), Type.TOOL);
        }
        @Override
        public boolean onBuy(Player player) {
            if (!(player instanceof ServerPlayer serverPlayer)) return false;
            return TrapperTraps.place(serverPlayer, true);
        }
    }
    private static ItemStack extraTrapStack() {
        ItemStack stack = ModItems.EXTRA_TRAP.getDefaultInstance();
        stack.set(DataComponents.CUSTOM_NAME, Component.translatable("item.brinswathe.extra_trap"));
        return stack;
    }
    static List<ShopEntry> voodooShop(GameWorldComponent game, Player player, String roleId) {
        return List.of(new ShopEntry(
            voodooNightVisionStack(),
            Tunable.VOODOO_NIGHT_VISION_PRICE.intValue(),
            ShopEntry.Type.TOOL
        ));
    }
    private static ItemStack voodooNightVisionStack() {
        ItemStack stack = new ItemStack(Items.SPLASH_POTION);
        stack.set(
            DataComponents.POTION_CONTENTS,
            new PotionContents(
                java.util.Optional.empty(),
                java.util.Optional.empty(),
                List.of(new MobEffectInstance(
                    MobEffects.NIGHT_VISION,
                    Tunable.VOODOO_NIGHT_VISION_SECONDS.ticks(),
                    0, false, true, true
                ))
            )
        );
        stack.set(DataComponents.CUSTOM_NAME, Component.literal("夜视药水瓶"));
        return stack;
    }
    static List<ShopEntry> mimicShop(GameWorldComponent game, Player player, String roleId) {
        return List.of(
            entry(WatheItems.LOCKPICK.getDefaultInstance(), Tunable.MIMIC_LOCKPICK_PRICE, ShopEntry.Type.TOOL),
            entry(delusionVialStack(), Tunable.MIMIC_DELUSION_VIAL_PRICE, ShopEntry.Type.POISON),
            entry(WatheItems.FIRECRACKER.getDefaultInstance(), Tunable.MIMIC_FIRECRACKER_PRICE, ShopEntry.Type.TOOL),
            entry(WatheItems.NOTE.getDefaultInstance(), Tunable.MIMIC_NOTE_PRICE, ShopEntry.Type.TOOL),
            entry(WatheItems.REVOLVER.getDefaultInstance(), Tunable.MIMIC_REVOLVER_PRICE, ShopEntry.Type.WEAPON),
            new MiceyesShopEntry()
        );
    }
    private static ShopEntry entry(ItemStack stack, Tunable price, ShopEntry.Type type) {
        return new ShopEntry(stack, price.intValue(), type);
    }
    static List<ShopEntry> noisemakerShop(GameWorldComponent game, Player player, String roleId) {
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
            Component.literal("自身与标记目标发光 " + Tunable.NOISEMAKER_SHOUT_GLOW_SECONDS.text() + " 秒")
                .withColor(0xAAAAAA)
        )));
        return stack;
    }
    static List<ShopEntry> necromancerShop(GameWorldComponent game, Player player, String roleId) {
        return List.of(
            new NecromancerRefreshShopEntry(),
            entry(WatheItems.KNIFE.getDefaultInstance(), Tunable.NECROMANCER_KNIFE_PRICE, ShopEntry.Type.WEAPON),
            entry(WatheItems.GRENADE.getDefaultInstance(), Tunable.NECROMANCER_GRENADE_PRICE, ShopEntry.Type.WEAPON),
            entry(WatheItems.FIRECRACKER.getDefaultInstance(), Tunable.NECROMANCER_FIRECRACKER_PRICE, ShopEntry.Type.TOOL),
            entry(WatheItems.LOCKPICK.getDefaultInstance(), Tunable.NECROMANCER_LOCKPICK_PRICE, ShopEntry.Type.TOOL),
            entry(WatheItems.CROWBAR.getDefaultInstance(), Tunable.NECROMANCER_CROWBAR_PRICE, ShopEntry.Type.TOOL),
            entry(WatheItems.BODY_BAG.getDefaultInstance(), Tunable.NECROMANCER_BODY_BAG_PRICE, ShopEntry.Type.TOOL),
            entry(new ItemStack(WatheItems.NOTE, 4), Tunable.NECROMANCER_NOTE_PRICE, ShopEntry.Type.TOOL)
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
            super(necromancerRefreshStack(), Tunable.NECROMANCER_REFRESH_PRICE.intValue(), Type.WEAPON);
        }
        @Override
        public boolean onBuy(Player player) {
            return refreshStupidAbility(player) && addNecromancerRevive(player);
        }
    }
    static final class NoisemakerFirecrackerShopEntry extends ShopEntry {
        private NoisemakerFirecrackerShopEntry() {
            super(
                WatheItems.FIRECRACKER.getDefaultInstance(),
                Tunable.NOISEMAKER_FIRECRACKER_PRICE.intValue(),
                Type.TOOL
            );
        }
        @Override
        public boolean onBuy(Player player) {
            return insertStackInFreeSlot(player, WatheItems.FIRECRACKER.getDefaultInstance());
        }
    }
    static final class NoisemakerShoutShopEntry extends ShopEntry {
        private NoisemakerShoutShopEntry() {
            super(noisemakerShoutStack(), Tunable.NOISEMAKER_SHOUT_PRICE.intValue(), Type.TOOL);
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
                int price = Tunable.NOISEMAKER_SHOUT_PRICE.intValue();
                shop.balance -= price;
                shop.sync();
                player.displayClientMessage(
                    Component.literal("购买成功！花费 " + price + " 金币").withStyle(ChatFormatting.GREEN),
                    true
                );
                player.getCooldowns().addCooldown(
                    WatheItems.FIRECRACKER,
                    Tunable.NOISEMAKER_SHOUT_COOLDOWN_SECONDS.ticks()
                );
            } else {
                player.getCooldowns().addCooldown(
                    WatheItems.FIRECRACKER,
                    Tunable.NOISEMAKER_SHOUT_CREDIT_COOLDOWN_SECONDS.ticks()
                );
                player.displayClientMessage(
                    Component.literal(
                        "金币不足！赊账购买，物品进入"
                            + Tunable.NOISEMAKER_SHOUT_CREDIT_COOLDOWN_SECONDS.text()
                            + "秒冷却"
                    ).withStyle(ChatFormatting.DARK_PURPLE),
                    true
                );
            }
            applyNoisemakerGlow(serverPlayer);
            return true;
        }
    }
    static final class MiceyesShopEntry extends ShopEntry {
        private MiceyesShopEntry() {
            super(miceyesStack(), Tunable.MIMIC_MICEYES_PRICE.intValue(), Type.TOOL);
        }
        @Override
        public boolean onBuy(Player player) {
            if (!(player instanceof ServerPlayer)) return false;
            if (!insertStackInFreeSlot(player, stack().copy())) return false;
            if (ModModifiers.hasModifier(player, ModModifiers.MICEYES) || IcModifiers.MICEYES == null) {
                return true;
            }
            WorldModifierComponent modifiers = WorldModifierComponent.KEY.get(player.level());
            modifiers.addModifier(player.getUUID(), IcModifiers.MICEYES);
            ModifierAssigned.EVENT.invoker().assignModifier(player, IcModifiers.MICEYES);
            return true;
        }
    }
    private static void applyNoisemakerGlow(ServerPlayer player) {
        int glowTicks = Tunable.NOISEMAKER_SHOUT_GLOW_SECONDS.ticks();
        if (glowTicks <= 0) return;
        player.addEffect(new MobEffectInstance(MobEffects.GLOWING, glowTicks, 0, false, false));
        UUID targetId = NoelleAccess.voodooTarget(player);
        if (targetId == null) return;
        Player target = player.level().getPlayerByUUID(targetId);
        if (target != null && GameFunctions.isPlayerAliveAndSurvival(target) && target != player) {
            target.addEffect(new MobEffectInstance(MobEffects.GLOWING, glowTicks, 0, false, false));
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
