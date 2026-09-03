package cn.erindax.brinswathe;

import cn.erindax.brinswathe.component.ArchivistComponent;
import cn.erindax.brinswathe.component.AvengerComponent;
import cn.erindax.brinswathe.component.BerserkerComponent;
import cn.erindax.brinswathe.component.BombComponent;
import cn.erindax.brinswathe.component.BoneharvesterComponent;
import cn.erindax.brinswathe.component.CompensatorComponent;
import cn.erindax.brinswathe.component.CowboyComponent;
import cn.erindax.brinswathe.component.EavesdropperComponent;
import cn.erindax.brinswathe.component.GamblerComponent;
import cn.erindax.brinswathe.component.IllusionistComponent;
import cn.erindax.brinswathe.component.MediumComponent;
import cn.erindax.brinswathe.component.MorticianComponent;
import cn.erindax.brinswathe.component.NightmareComponent;
import cn.erindax.brinswathe.component.PenitentComponent;
import cn.erindax.brinswathe.component.PuppeteerControlComponent;
import cn.erindax.brinswathe.component.SniperComponent;
import cn.erindax.brinswathe.component.StalkerComponent;
import cn.erindax.brinswathe.component.StaminaComponent;
import cn.erindax.brinswathe.component.StuntDoubleComponent;
import cn.erindax.brinswathe.component.TrapperComponent;
import cn.erindax.brinswathe.component.WatchmanComponent;
import cn.erindax.brinswathe.component.ZhangshiComponent;
import cn.erindax.brinswathe.config.BrinConfig;
import dev.doctor4t.wathe.api.GameMode;
import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.api.Role.MoodType;
import dev.doctor4t.wathe.api.WatheGameModes;
import dev.doctor4t.wathe.api.WatheRoles;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.PlayerShopComponent;
import dev.doctor4t.wathe.index.WatheItems;
import java.util.HashMap;
import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;
import org.agmas.harpymodloader.Harpymodloader;
import org.agmas.harpymodloader.events.ModdedRoleAssigned;
import org.agmas.harpymodloader.events.ResetPlayerEvent;
import org.BsXinQin.kinswathe.KinsWatheConfig;
import org.BsXinQin.kinswathe.KinsWatheItems;
import org.BsXinQin.kinswathe.KinsWatheRoles;
import org.BsXinQin.kinswathe.roles.hacker.HackerPhoneComponent;

public class BrinRoles {
    private static final HashMap<String, Role> ROLES = new HashMap<>();

    public static Role PUPPETEER = registerRole(
        new Role(
            ResourceLocation.fromNamespaceAndPath("brin", "puppeteer"),
            0x8B0000,
            false,
            true,
            MoodType.FAKE,
            WatheRoles.KILLER.getMaxSprintTime(),
            true
        )
    );

    public static Role STUNT_DOUBLE = registerRole(
        new Role(
            ResourceLocation.fromNamespaceAndPath("brin", "stunt_double"),
            0x4477CC,
            true,
            false,
            MoodType.REAL,
            WatheRoles.CIVILIAN.getMaxSprintTime(),
            false
        )
    );

    public static Role MEDIUM = registerRole(
        new Role(
            ResourceLocation.fromNamespaceAndPath("brin", "medium"),
            0x9370DB,
            true,
            false,
            MoodType.REAL,
            WatheRoles.CIVILIAN.getMaxSprintTime(),
            false
        )
    );

    public static Role EAVESDROPPER = registerRole(
        new Role(
            ResourceLocation.fromNamespaceAndPath("brin", "eavesdropper"),
            0x00CED1,
            true,
            false,
            MoodType.REAL,
            WatheRoles.CIVILIAN.getMaxSprintTime(),
            false
        )
    );

    public static Role WATCHMAN = registerRole(
        new Role(
            ResourceLocation.fromNamespaceAndPath("brin", "watchman"),
            0x228B22,
            true,
            false,
            MoodType.REAL,
            WatheRoles.CIVILIAN.getMaxSprintTime(),
            false
        )
    );

    public static Role TRAPPER = registerRole(
        new Role(
            ResourceLocation.fromNamespaceAndPath("brin", "beastcatcher"),
            0x8B4513,
            false,
            true,
            MoodType.FAKE,
            WatheRoles.KILLER.getMaxSprintTime(),
            true
        )
    );

    public static Role NIGHTMARE = registerRole(
        new Role(
            ResourceLocation.fromNamespaceAndPath("brin", "nightmare"),
            0x4B0082,
            false,
            false,
            MoodType.FAKE,
            -1,
            true
        )
    );

    public static Role ILLUSIONIST = registerRole(
        new Role(
            ResourceLocation.fromNamespaceAndPath("brin", "illusionist"),
            0xFF69B4,
            false,
            true,
            MoodType.FAKE,
            WatheRoles.KILLER.getMaxSprintTime(),
            true
        )
    );

    public static Role SNIPER = registerRole(
        new Role(
            ResourceLocation.fromNamespaceAndPath("brin", "sniper"),
            0x556B2F,
            false,
            true,
            MoodType.FAKE,
            WatheRoles.KILLER.getMaxSprintTime(),
            true
        )
    );

    public static Role BERSERKER = registerRole(
        new Role(
            ResourceLocation.fromNamespaceAndPath("brin", "berserker"),
            0xFF4500,
            false,
            false,
            MoodType.FAKE,
            -1,
            true
        )
    );

    public static Role ARCHIVIST = registerRole(
        new Role(
            ResourceLocation.fromNamespaceAndPath("brin", "archivist"),
            0x8FBC8F,
            true,
            false,
            MoodType.REAL,
            WatheRoles.CIVILIAN.getMaxSprintTime(),
            false
        )
    );

    public static Role BONEHARVESTER = registerRole(
        new Role(
            ResourceLocation.fromNamespaceAndPath("brin", "boneharvester"),
            0xD2B48C,
            false,
            true,
            MoodType.FAKE,
            -1,
            true
        )
    );

    public static Role COMPENSATOR = registerRole(
        new Role(
            ResourceLocation.fromNamespaceAndPath("brin", "compensator"),
            0xCD853F,
            false,
            false,
            MoodType.FAKE,
            WatheRoles.CIVILIAN.getMaxSprintTime(),
            true
        )
    );

    public static Role GAMBLER = registerRole(
        new Role(
            ResourceLocation.fromNamespaceAndPath("brin", "gambler"),
            0xFFD700,
            false,
            false,
            MoodType.FAKE,
            WatheRoles.CIVILIAN.getMaxSprintTime(),
            true
        )
    );

    public static Role PENITENT = registerRole(
        new Role(
            ResourceLocation.fromNamespaceAndPath("brin", "penitent"),
            0x8B0000,
            false,
            false,
            MoodType.FAKE,
            -1,
            true
        )
    );

    public static Role TERRORIST = registerRole(
        new Role(
            ResourceLocation.fromNamespaceAndPath("brin", "terrorist"),
            0x6B5A2B,
            false,
            false,
            MoodType.FAKE,
            WatheRoles.CIVILIAN.getMaxSprintTime(),
            true
        )
    );

    public static Role BOMBER = registerRole(
        new Role(
            ResourceLocation.fromNamespaceAndPath("brin", "bomber"),
            0xFF8C00,
            false,
            true,
            MoodType.FAKE,
            WatheRoles.KILLER.getMaxSprintTime(),
            true
        )
    );

    public static Role ZHANGSHI = registerRole(
        new Role(
            ResourceLocation.fromNamespaceAndPath("brin", "zhangshi"),
            0xC0392B,
            false,
            true,
            MoodType.FAKE,
            WatheRoles.KILLER.getMaxSprintTime(),
            true
        )
    );

    public static Role MORTICIAN = registerRole(
        new Role(
            ResourceLocation.fromNamespaceAndPath("brin", "mortician"),
            0x708090,
            false,
            true,
            MoodType.FAKE,
            WatheRoles.KILLER.getMaxSprintTime(),
            true
        )
    );

    public static Role AVENGER = registerRole(
        new Role(
            ResourceLocation.fromNamespaceAndPath("brin", "avenger"),
            0xB22222,
            true,
            false,
            MoodType.REAL,
            WatheRoles.CIVILIAN.getMaxSprintTime(),
            false
        )
    );

    public static Role STALKER = registerRole(
        new Role(
            ResourceLocation.fromNamespaceAndPath("brin", "stalker"),
            0xC71585,
            true,
            false,
            MoodType.REAL,
            WatheRoles.CIVILIAN.getMaxSprintTime(),
            false
        )
    );

    public static Role COWBOY = registerRole(
        new Role(
            ResourceLocation.fromNamespaceAndPath("brin", "cowboy"),
            0xB8860B,
            true,
            false,
            MoodType.REAL,
            WatheRoles.CIVILIAN.getMaxSprintTime(),
            false
        )
    );

    public static HashMap<String, Role> getRoles() {
        return ROLES;
    }

    public static void init() {
        Harpymodloader.setRoleMaximum(PUPPETEER, 1);
        Harpymodloader.setRoleMaximum(STUNT_DOUBLE, 1);
        Harpymodloader.setRoleMaximum(MEDIUM, 1);
        Harpymodloader.setRoleMaximum(EAVESDROPPER, 1);
        Harpymodloader.setRoleMaximum(WATCHMAN, 1);
        Harpymodloader.setRoleMaximum(TRAPPER, 1);
        Harpymodloader.setRoleMaximum(NIGHTMARE, 1);
        Harpymodloader.setRoleMaximum(ILLUSIONIST, 1);
        Harpymodloader.setRoleMaximum(BERSERKER, 1);
        Harpymodloader.setRoleMaximum(ARCHIVIST, 1);
        Harpymodloader.setRoleMaximum(BONEHARVESTER, 1);
        Harpymodloader.setRoleMaximum(COMPENSATOR, 1);
        Harpymodloader.setRoleMaximum(GAMBLER, 1);
        Harpymodloader.setRoleMaximum(PENITENT, 1);
        Harpymodloader.setRoleMaximum(SNIPER, 1);
        Harpymodloader.setRoleMaximum(TERRORIST, 1);
        Harpymodloader.setRoleMaximum(ZHANGSHI, 1);
        Harpymodloader.setRoleMaximum(BOMBER, 1);
        Harpymodloader.setRoleMaximum(MORTICIAN, 1);
        Harpymodloader.setRoleMaximum(AVENGER, 1);
        Harpymodloader.setRoleMaximum(STALKER, 1);
        Harpymodloader.setRoleMaximum(COWBOY, 1);

        ModdedRoleAssigned.EVENT.register((player, role) -> {
            if (BrinNoelleAccess.EXECUTIONER_ID.equals(role.identifier())) {
                BrinNoelleAccess.resetExecutionerWon(player);
            }
            if (ROLES.containsValue(role)) {
                Integer initialBalance = BrinConfig.initialBalance(configRoleId(role));
                if (initialBalance != null) {
                    PlayerShopComponent.KEY.get(player).setBalance(initialBalance);
                } else if (role.equals(ARCHIVIST)) {
                    PlayerShopComponent.KEY.get(player).setBalance(0);
                }
            }
            if (role.equals(PENITENT)) {
                PenitentComponent component = PenitentComponent.KEY.get(player);
                if (component != null) {
                    component.reset();
                    component.initializeShield();
                }
            }
            if (role.equals(SNIPER)) {
                initializeSniper(player);
            }
            if (role.equals(ZHANGSHI)) {
                ZhangshiComponent component = ZhangshiComponent.KEY.get(player);
                if (component != null) component.reset();
            }
            if (role.equals(AVENGER)) {
                AvengerComponent component = AvengerComponent.KEY.get(player);
                if (component != null) component.reset();
            }
            if (role.equals(STALKER)) {
                StalkerComponent component = StalkerComponent.KEY.get(player);
                if (component != null) component.reset();
            }
            if (role.equals(COWBOY)) {
                CowboyComponent component = CowboyComponent.KEY.get(player);
                if (component != null) component.reset();
            }
            if (role.equals(WATCHMAN) && player instanceof ServerPlayer serverPlayer) {
                ensureWatchmanRoomKey(serverPlayer);
            }
            giveHackerPhone(player, role);
        });

        ResetPlayerEvent.EVENT.register(player -> {
            BrinNoelleAccess.resetExecutionerWon(player);
            PuppeteerControlComponent comp = PuppeteerControlComponent.KEY.get(player);
            if (comp != null) comp.reset();
            StuntDoubleComponent sdComp = StuntDoubleComponent.KEY.get(player);
            if (sdComp != null) sdComp.reset();
            StaminaComponent staminaComp = StaminaComponent.KEY.get(player);
            if (staminaComp != null) staminaComp.reset();
            MediumComponent mediumComp = MediumComponent.KEY.get(player);
            if (mediumComp != null) mediumComp.reset();
            EavesdropperComponent eavesdropperComp = EavesdropperComponent.KEY.get(player);
            if (eavesdropperComp != null) eavesdropperComp.reset();
            WatchmanComponent watchmanComp = WatchmanComponent.KEY.get(player);
            if (watchmanComp != null) watchmanComp.reset();
            TrapperComponent trapperComp = TrapperComponent.KEY.get(player);
            if (trapperComp != null) trapperComp.reset();
            NightmareComponent nightmareComp = NightmareComponent.KEY.get(player);
            if (nightmareComp != null) nightmareComp.reset();
            IllusionistComponent illusionistComp = IllusionistComponent.KEY.get(player);
            if (illusionistComp != null) illusionistComp.reset();
            BerserkerComponent berserkerComp = BerserkerComponent.KEY.get(player);
            if (berserkerComp != null) berserkerComp.reset();
            ArchivistComponent archivistComp = ArchivistComponent.KEY.get(player);
            if (archivistComp != null) archivistComp.reset();
            BoneharvesterComponent boneharvesterComp = BoneharvesterComponent.KEY.get(player);
            if (boneharvesterComp != null) boneharvesterComp.reset();
            CompensatorComponent compensatorComp = CompensatorComponent.KEY.get(player);
            if (compensatorComp != null) compensatorComp.reset();
            GamblerComponent gamblerComp = GamblerComponent.KEY.get(player);
            if (gamblerComp != null) gamblerComp.reset();
            PenitentComponent penitentComp = PenitentComponent.KEY.get(player);
            if (penitentComp != null) penitentComp.reset();
            SniperComponent sniperComp = SniperComponent.KEY.get(player);
            if (sniperComp != null) sniperComp.reset();
            ZhangshiComponent zhangshiComp = ZhangshiComponent.KEY.get(player);
            if (zhangshiComp != null) zhangshiComp.reset();
            BombComponent bombComp = BombComponent.KEY.get(player);
            if (bombComp != null) bombComp.reset();
            MorticianComponent morticianComp = MorticianComponent.KEY.get(player);
            if (morticianComp != null) morticianComp.reset();
            AvengerComponent avengerComp = AvengerComponent.KEY.get(player);
            if (avengerComp != null) avengerComp.reset();
            StalkerComponent stalkerComp = StalkerComponent.KEY.get(player);
            if (stalkerComp != null) stalkerComp.reset();
            CowboyComponent cowboyComp = CowboyComponent.KEY.get(player);
            if (cowboyComp != null) cowboyComp.reset();
        });
    }

    public static Role registerRole(Role role) {
        WatheRoles.registerRole(role);
        ROLES.put(configRoleId(role), role);
        return role;
    }

    public static String configRoleId(Role role) {
        String roleId = role.identifier().getPath();
        return "beastcatcher".equals(roleId) ? "beast_trapper" : roleId;
    }

    private static void giveHackerPhone(Player player, Role role) {
        if (!ROLES.containsValue(role) || !role.canUseKiller()) return;
        if (player.getServer() == null || hasHackerPhone(player)) return;

        GameWorldComponent game = GameWorldComponent.KEY.get(player.level());
        boolean hackerPresent = player.getServer().getPlayerList().getPlayers().stream()
            .anyMatch(other -> game.isRole(other, KinsWatheRoles.HACKER));
        if (!hackerPresent) return;

        HackerPhoneComponent phone = HackerPhoneComponent.KEY.get(player);
        player.addItem(phone.getPhone());
    }

    private static final int TRAIN_ROOM_COUNT = 7;
    private static final int ROOM_KEY_COLOUR = 0xFF8C00;

    public static void ensureWatchmanRoomKey(ServerPlayer player) {
        Inventory inventory = player.getInventory();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            if (inventory.getItem(slot).is(WatheItems.KEY)) return;
        }
        player.addItem(roomKey(player.getRandom().nextInt(TRAIN_ROOM_COUNT) + 1));
    }
    private static ItemStack roomKey(int roomNumber) {
        ItemStack stack = new ItemStack(WatheItems.KEY);
        stack.set(DataComponents.LORE, new ItemLore(List.of(
            Component.literal("Room " + roomNumber)
                .withStyle(style -> style.withItalic(false).withColor(ROOM_KEY_COLOUR))
        )));
        return stack;
    }
    public static void initializeSniper(Player player) {
        SniperComponent component = SniperComponent.KEY.get(player);
        if (component != null) {
            component.reset();
            component.startCooldown(openingCooldownSeconds(player));
        }
        giveSniperRevolver(player);
    }
    private static int openingCooldownSeconds(Player player) {
        KinsWatheConfig config = KinsWatheConfig.HANDLER.instance();
        if (!config.EnableStartSafeTime) return 0;
        GameMode gameMode = GameWorldComponent.KEY.get(player.level()).getGameMode();
        if (gameMode == WatheGameModes.DISCOVERY || gameMode == WatheGameModes.LOOSE_ENDS) return 0;
        return Math.max(0, config.StartingCooldown);
    }
    private static void giveSniperRevolver(Player player) {
        if (player.level().isClientSide) return;
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            if (player.getInventory().getItem(slot).is(WatheItems.REVOLVER)) return;
        }
        player.addItem(WatheItems.REVOLVER.getDefaultInstance());
    }
    private static boolean hasHackerPhone(Player player) {
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            if (player.getInventory().getItem(slot).is(KinsWatheItems.PHONE)) return true;
        }
        return false;
    }
    public static String getRoleId(GameWorldComponent game, Player player) {
        for (var entry : ROLES.entrySet()) {
            if (game.isRole(player, entry.getValue())) return entry.getKey();
        }
        return null;
    }
    public static boolean ignoresOpeningSafeTime(GameWorldComponent game, Player player) {
        return game != null && player != null && game.isRole(player, STALKER);
    }
}
