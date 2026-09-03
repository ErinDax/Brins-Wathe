package cn.erindax.brinswathe.config;

import cn.erindax.brinswathe.BrinsWathe;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.Nullable;

public final class BrinConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String FILE_NAME = "brinswathe.json5";
    private static final String LEGACY_FILE_NAME = "brinswathe.json";
    private static final int CONFIG_VERSION = 39;
    private static final int ROLE_BALANCE_MIGRATION_VERSION = 39;
    private static final int PREVIOUS_ARCHIVIST_SKILL_COST = 175;
    private static final int PREVIOUS_ARCHIVIST_SKILL_COOLDOWN_SECONDS = 120;
    private static final int PREVIOUS_COWBOY_SKILL_COST = 450;
    private static final int PREVIOUS_MEDIUM_SKILL_DURATION_SECONDS = 5;
    private static final int PREVIOUS_STALKER_SKILL_COOLDOWN_SECONDS = 150;
    private static final int PREVIOUS_WATCHMAN_SKILL_COOLDOWN_SECONDS = 200;
    private static final int PREVIOUS_WATCHMAN_SKILL_COST = 300;
    private static final int PREVIOUS_TRAPPER_SKILL_COST = 125;
    private static final int PREVIOUS_BOMB_PRICE = 100;
    private static final int PREVIOUS_MORTICIAN_SKILL_COOLDOWN_SECONDS = 120;
    private static final int PREVIOUS_PENITENT_STARTING_SHIELD_LAYERS = 1;
    private static final int MINE_STUN_MIGRATION_VERSION = 23;
    private static final int PREVIOUS_MINE_STUN_SECONDS = 0;
    private static final int SNIPER_COOLDOWN_MIGRATION_VERSION = 17;
    private static final int SNIPER_KILLS_TO_RESET_MIGRATION_VERSION = 18;
    private static final int SNIPER_REVOLVER_PRICE_MIGRATION_VERSION = 11;
    private static final int PREVIOUS_SNIPER_COOLDOWN_SECONDS = 150;
    private static final int LEGACY_SNIPER_COOLDOWN_SECONDS = 300;
    private static final int PREVIOUS_SNIPER_KILLS_TO_RESET = 3;
    private static final int ACCIDENTAL_SNIPER_REVOLVER_PRICE = 175;
    private static final int PREVIOUS_SNIPER_REVOLVER_PRICE = 300;
    private static volatile Data current = createDefaults();
    private static Path configPath;

    private BrinConfig() {
    }

    public static synchronized void initialize() {
        configPath = FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME);
        Path legacyPath = FabricLoader.getInstance().getConfigDir().resolve(LEGACY_FILE_NAME);
        try {
            if (Files.exists(configPath)) {
                reload();
            } else if (Files.exists(legacyPath)) {
                current = parse(Files.readString(legacyPath, StandardCharsets.UTF_8));
                writeConfig();
                BrinsWathe.LOGGER.info("Migrated {} to {}", legacyPath.getFileName(), configPath.getFileName());
            } else {
                current = createDefaults();
                writeConfig();
            }
        } catch (Exception exception) {
            current = createDefaults();
            BrinsWathe.LOGGER.error("Failed to load {}: {}", configPath, exception.getMessage());
        }
    }

    public static synchronized void reload() throws IOException {
        if (configPath == null) {
            configPath = FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME);
        }

        if (!Files.exists(configPath)) {
            Path legacyPath = FabricLoader.getInstance().getConfigDir().resolve(LEGACY_FILE_NAME);
            if (Files.exists(legacyPath)) {
                current = parse(Files.readString(legacyPath, StandardCharsets.UTF_8));
                writeConfig();
                return;
            }
            current = createDefaults();
            writeConfig();
            return;
        }

        String source = Files.readString(configPath, StandardCharsets.UTF_8);
        int sourceVersion = sourceVersion(source);
        JsonObject sourceRoot = parseJson5(source).getAsJsonObject();
        boolean missingHarpy = !sourceRoot.has("harpy");
        current = parse(source);
        if (sourceVersion < CONFIG_VERSION || missingHarpy) writeConfig();
    }

    public static void applyServerJson(String json) {
        try {
            current = parse(json);
        } catch (RuntimeException exception) {
            BrinsWathe.LOGGER.error("Failed to apply synchronized config: {}", exception.getMessage());
        }
    }

    public static String toJson() {
        return renderJson5(current);
    }

    public static Path path() {
        return configPath;
    }

    public static List<String> announcement() {
        return current.announcement;
    }

    public static boolean afkKickEnabled() {
        return current.afk_kick.enabled;
    }

    public static int afkIdleSeconds() {
        return current.afk_kick.idle_seconds;
    }

    public static int afkCountdownSeconds() {
        return current.afk_kick.countdown_seconds;
    }

    public static synchronized void setAfkKickEnabled(boolean enabled) throws IOException {
        AfkKickSettings settings = current.afk_kick;
        current = new Data(
            CONFIG_VERSION,
            current.announcement,
            new AfkKickSettings(enabled, settings.idle_seconds, settings.countdown_seconds),
            current.roles,
            current.harpy
        );
        if (configPath == null) {
            configPath = FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME);
        }
        writeConfig();
    }

    public static int skillCost(String roleId) {
        return settings(roleId).skill_cost;
    }

    public static int skillCooldownSeconds(String roleId) {
        return settings(roleId).skill_cooldown_seconds;
    }

    public static int sniperKillsToReset() {
        Integer kills = settings("sniper").sniper_kills_to_reset;
        return kills == null ? -1 : kills;
    }

    public static int sniperGlowSeconds() {
        Integer seconds = settings("sniper").glow_seconds;
        return seconds == null ? 10 : seconds;
    }

    public static int skillDurationSeconds(String roleId) {
        Integer duration = settings(roleId).skill_duration_seconds;
        return duration == null ? 0 : duration;
    }

    public static int nightmareForcedSleepCooldownSeconds() {
        Integer cooldown = settings("nightmare").forced_sleep_task_cooldown_seconds;
        return cooldown == null ? 200 : cooldown;
    }

    public static int boneKnifePrice() {
        Integer price = settings("boneharvester").bone_knife_price;
        return price == null ? 175 : price;
    }

    public static int boneKnifeCooldownSeconds() {
        Integer cooldown = settings("boneharvester").bone_knife_cooldown_seconds;
        return cooldown == null ? 200 : cooldown;
    }

    public static int xueziPrice() {
        Integer price = settings("zhangshi").xuezi_price;
        return price == null ? 150 : price;
    }

    public static int xueziCooldownSeconds() {
        Integer cooldown = settings("zhangshi").xuezi_cooldown_seconds;
        return cooldown == null ? 200 : cooldown;
    }

    public static int xueziShieldLayers() {
        Integer layers = settings("zhangshi").xuezi_shield_layers;
        return layers == null ? 1 : Math.max(0, Math.min(5, layers));
    }

    public static float zhangshiRunSpeed() {
        Double speed = settings("zhangshi").run_speed;
        return speed == null ? 2.0F : speed.floatValue();
    }

    public static int penitentIdentityHintPrice() {
        Integer price = settings("penitent").identity_hint_price;
        return price == null ? 150 : price;
    }

    public static int penitentAntidotePrice() {
        Integer price = settings("penitent").antidote_price;
        return price == null ? 100 : price;
    }

    public static int penitentStartingShieldLayers() {
        Integer layers = settings("penitent").starting_shield_layers;
        return layers == null ? 0 : Math.max(0, layers);
    }

    public static int harpyNeutralRoleCount() {
        return current.harpy.neutral_role_count;
    }

    public static int harpyKillerRoleCount() {
        return current.harpy.killer_role_count;
    }

    public static int harpyVigilanteRoleCount() {
        return current.harpy.vigilante_role_count;
    }

    public static int harpyRoleCount(HarpyCountKind kind) {
        return switch (kind) {
            case NEUTRAL -> harpyNeutralRoleCount();
            case KILLER -> harpyKillerRoleCount();
            case VIGILANTE -> harpyVigilanteRoleCount();
        };
    }

    public static Map<String, List<String>> harpyModifierBlacklist() {
        return current.harpy.role_modifier_blacklist;
    }

    public static synchronized void setHarpyRoleCount(HarpyCountKind kind, int value) throws IOException {
        HarpyAssignmentSettings harpy = current.harpy;
        current = new Data(
            CONFIG_VERSION,
            current.announcement,
            current.afk_kick,
            current.roles,
            new HarpyAssignmentSettings(
                kind == HarpyCountKind.NEUTRAL ? value : harpy.neutral_role_count,
                kind == HarpyCountKind.KILLER ? value : harpy.killer_role_count,
                kind == HarpyCountKind.VIGILANTE ? value : harpy.vigilante_role_count,
                copyBlacklist(harpy.role_modifier_blacklist)
            )
        );
        if (configPath == null) {
            configPath = FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME);
        }
        writeConfig();
    }

    public static synchronized void setHarpyModifierBlacklist(String roleId, List<String> modifierIds) throws IOException {
        LinkedHashMap<String, List<String>> blacklist = copyBlacklist(current.harpy.role_modifier_blacklist);
        if (modifierIds == null || modifierIds.isEmpty()) {
            blacklist.remove(roleId);
        } else {
            blacklist.put(roleId, List.copyOf(modifierIds));
        }
        current = new Data(
            CONFIG_VERSION,
            current.announcement,
            current.afk_kick,
            current.roles,
            new HarpyAssignmentSettings(
                current.harpy.neutral_role_count,
                current.harpy.killer_role_count,
                current.harpy.vigilante_role_count,
                blacklist
            )
        );
        if (configPath == null) {
            configPath = FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME);
        }
        writeConfig();
    }

    public enum HarpyCountKind {
        NEUTRAL("commands.setrolecount.neutral.success"),
        KILLER("commands.setrolecount.killer.success"),
        VIGILANTE("commands.setrolecount.vigilante.success");

        private final String successKey;

        HarpyCountKind(String successKey) {
            this.successKey = successKey;
        }

        public String successKey() {
            return successKey;
        }
    }

    public static int puppeteerSelfDestructPrice() {
        Integer price = settings("puppeteer").self_destruct_price;
        return price == null ? 350 : price;
    }

    public static int puppeteerReturnCooldownSeconds() {
        Integer cooldown = settings("puppeteer").return_cooldown_seconds;
        return cooldown == null ? 60 : cooldown;
    }

    public static int puppeteerParticleDelaySeconds() {
        Integer delay = settings("puppeteer").particle_delay_seconds;
        return delay == null ? 45 : delay;
    }
    public static int terroristExplosionRange() {
        Integer range = settings("terrorist").explosion_range;
        return range == null ? 5 : range;
    }
    public static int terroristExplosionSurvivalKills() {
        Integer kills = settings("terrorist").explosion_survival_kills;
        return kills == null ? 4 : kills;
    }
    public static int trapperTrapLimit() {
        Integer limit = settings("beast_trapper").trap_limit;
        return limit == null ? 1 : Math.max(0, limit);
    }
    public static int bomberGrenadePrice() {
        Integer price = settings("bomber").grenade_price;
        return price == null ? 200 : price;
    }
    public static int bomberBombPrice() {
        Integer price = settings("bomber").bomb_price;
        return price == null ? 75 : price;
    }
    public static int bomberMinePrice() {
        Integer price = settings("bomber").mine_price;
        return price == null ? 125 : price;
    }
    public static int trapperExtraTrapPrice() {
        Integer price = settings("beast_trapper").extra_trap_price;
        return price == null ? 150 : price;
    }
    public static int trapperCatchReward() {
        Integer reward = settings("beast_trapper").trap_catch_reward;
        return reward == null ? 50 : Math.max(0, reward);
    }
    public static int sniperTasksToReset() {
        Integer tasks = settings("sniper").sniper_tasks_to_reset;
        return tasks == null ? 20 : tasks;
    }
    public static int sniperKillTaskProgress() {
        return 3;
    }
    public static int bomberBombFuseSeconds() {
        Integer seconds = settings("bomber").bomb_fuse_seconds;
        return seconds == null ? 45 : Math.max(1, seconds);
    }
    public static int bomberBombWarningSeconds() {
        Integer seconds = settings("bomber").bomb_warning_seconds;
        return seconds == null ? 30 : seconds;
    }
    public static int bomberBombExplosionSize() {
        Integer size = settings("bomber").bomb_explosion_size;
        return size == null ? 2 : Math.max(1, size);
    }
    public static int bomberBombPurchaseCooldownSeconds() {
        Integer seconds = settings("bomber").bomb_purchase_cooldown_seconds;
        return seconds == null ? 60 : seconds;
    }
    public static int bomberMineStunSeconds() {
        Integer seconds = settings("bomber").mine_stun_seconds;
        return seconds == null ? 3 : seconds;
    }
    public static int avengerGunSeconds() {
        Integer seconds = settings("avenger").skill_duration_seconds;
        return seconds == null ? 20 : Math.max(1, seconds);
    }
    public static int avengerInstinctSeconds() {
        Integer seconds = settings("avenger").instinct_seconds;
        return seconds == null ? 5 : seconds;
    }

    public static String cowboyDuelAnnounceMessage() {
        String message = settings("cowboy").duel_announce_message;
        return message == null ? "牛仔发起了决斗：{cowboy} 对决 {target}！" : message;
    }
    public static int cowboyDuelTimeoutSeconds() {
        Integer seconds = settings("cowboy").duel_timeout_seconds;
        return seconds == null ? 90 : Math.max(1, seconds);
    }

    public static int cowboyDuelCountdownSeconds() {
        Integer seconds = settings("cowboy").duel_countdown_seconds;
        return seconds == null ? 3 : Math.max(0, seconds);
    }

    public static float cowboyDuelMusicVolume() {
        Integer volume = settings("cowboy").duel_music_volume;
        int percent = volume == null ? 100 : Math.max(0, Math.min(100, volume));
        return percent / 100.0F;
    }
    @Nullable
    public static BlockPos cowboyArenaA() {
        RoleSettings cowboy = settings("cowboy");
        return cowboyArenaPos(cowboy.arena_a_x, cowboy.arena_a_y, cowboy.arena_a_z);
    }
    @Nullable
    public static BlockPos cowboyArenaB() {
        RoleSettings cowboy = settings("cowboy");
        return cowboyArenaPos(cowboy.arena_b_x, cowboy.arena_b_y, cowboy.arena_b_z);
    }
    @Nullable
    public static BlockPos cowboyArenaSpectator() {
        RoleSettings cowboy = settings("cowboy");
        return cowboyArenaPos(cowboy.arena_spectator_x, cowboy.arena_spectator_y, cowboy.arena_spectator_z);
    }
    @Nullable
    private static BlockPos cowboyArenaPos(Integer x, Integer y, Integer z) {
        int posX = x == null ? 0 : x;
        int posY = y == null ? 0 : y;
        int posZ = z == null ? 0 : z;
        if (posX == 0 && posY == 0 && posZ == 0) return null;
        return new BlockPos(posX, posY, posZ);
    }
    public static Integer initialBalance(String roleId) {
        return settings(roleId).initial_balance;
    }
    public static boolean revolverShopEnabled(String roleId) {
        Boolean enabled = settings(roleId).revolver_shop_enabled;
        return enabled == null || enabled;
    }
    public static int shopPrice(String roleId, ShopItem item) {
        ShopPrices prices = settings(roleId).shop_prices;
        return switch (item) {
            case KNIFE -> prices.knife;
            case REVOLVER -> prices.revolver;
            case PSYCHO_MODE -> prices.psycho_mode;
        };
    }
    private static RoleSettings settings(String roleId) {
        RoleSettings settings = current.roles.get(roleId);
        if (settings != null) return settings;
        RoleSettings defaults = createDefaults().roles.get(roleId);
        if (defaults != null) return defaults;
        return role(0, 0, null, 100, 300, 300);
    }

    private static Data parse(String json) {
        JsonElement rootElement = parseJson5(json);
        if (!rootElement.isJsonObject()) {
            throw new IllegalArgumentException("Config root must be an object");
        }
        JsonObject root = rootElement.getAsJsonObject();
        int sourceVersion = nonNegativeInt(root, "config_version", 0, "config");
        List<String> announcement = stringList(root, "announcement");
        JsonObject roleObjects = objectOrEmpty(root, "roles");
        Data defaults = createDefaults();
        JsonObject afkKickObject = objectOrEmpty(root, "afk_kick");
        AfkKickSettings afkKick = new AfkKickSettings(
            booleanValue(afkKickObject, "enabled", defaults.afk_kick.enabled, "afk_kick"),
            nonNegativeInt(afkKickObject, "idle_seconds", defaults.afk_kick.idle_seconds, "afk_kick"),
            nonNegativeInt(afkKickObject, "countdown_seconds", defaults.afk_kick.countdown_seconds, "afk_kick")
        );
        HarpyAssignmentSettings harpy = parseHarpy(root, defaults.harpy);
        LinkedHashMap<String, RoleSettings> parsedRoles = new LinkedHashMap<>();
        for (Map.Entry<String, RoleSettings> entry : defaults.roles.entrySet()) {
            String roleId = entry.getKey();
            RoleSettings fallback = entry.getValue();
            JsonObject role = objectOrEmpty(roleObjects, roleId);
            if ("beast_trapper".equals(roleId) && !roleObjects.has(roleId)) {
                role = objectOrEmpty(roleObjects, "trapper");
            }
            JsonObject shopPrices = hasAnyConfigurableShopPrice(roleId)
                ? objectOrEmpty(role, "shop_prices")
                : new JsonObject();
            int skillCost = configurableInt(
                role,
                "skill_cost",
                fallback.skill_cost,
                roleId,
                hasConfigurableSkillCost(roleId)
            );
            int skillCooldown = configurableInt(
                role,
                "skill_cooldown_seconds",
                fallback.skill_cooldown_seconds,
                roleId,
                hasConfigurableSkillCooldown(roleId)
            );
            if (sourceVersion < SNIPER_COOLDOWN_MIGRATION_VERSION
                && "sniper".equals(roleId)
                && (skillCooldown == PREVIOUS_SNIPER_COOLDOWN_SECONDS
                    || skillCooldown == LEGACY_SNIPER_COOLDOWN_SECONDS)) {
                skillCooldown = fallback.skill_cooldown_seconds;
            }
            if (sourceVersion < ROLE_BALANCE_MIGRATION_VERSION) {
                if ("archivist".equals(roleId) && skillCost == PREVIOUS_ARCHIVIST_SKILL_COST) {
                    skillCost = fallback.skill_cost;
                }
                if ("archivist".equals(roleId) && skillCooldown == PREVIOUS_ARCHIVIST_SKILL_COOLDOWN_SECONDS) {
                    skillCooldown = fallback.skill_cooldown_seconds;
                }
                if ("cowboy".equals(roleId) && skillCost == PREVIOUS_COWBOY_SKILL_COST) {
                    skillCost = fallback.skill_cost;
                }
                if ("stalker".equals(roleId) && skillCooldown == PREVIOUS_STALKER_SKILL_COOLDOWN_SECONDS) {
                    skillCooldown = fallback.skill_cooldown_seconds;
                }
                if ("watchman".equals(roleId) && skillCooldown == PREVIOUS_WATCHMAN_SKILL_COOLDOWN_SECONDS) {
                    skillCooldown = fallback.skill_cooldown_seconds;
                }
                if ("watchman".equals(roleId) && skillCost == PREVIOUS_WATCHMAN_SKILL_COST) {
                    skillCost = fallback.skill_cost;
                }
                if ("beast_trapper".equals(roleId) && skillCost == PREVIOUS_TRAPPER_SKILL_COST) {
                    skillCost = fallback.skill_cost;
                }
                if ("mortician".equals(roleId) && skillCooldown == PREVIOUS_MORTICIAN_SKILL_COOLDOWN_SECONDS) {
                    skillCooldown = fallback.skill_cooldown_seconds;
                }
            }
            int knifePrice = configurableInt(
                shopPrices,
                "knife",
                fallback.shop_prices.knife,
                roleId,
                hasConfigurableShopPrice(roleId, ShopItem.KNIFE)
            );
            int revolverPrice = configurableInt(
                shopPrices,
                "revolver",
                fallback.shop_prices.revolver,
                roleId,
                hasConfigurableShopPrice(roleId, ShopItem.REVOLVER)
            );
            if (sourceVersion < SNIPER_REVOLVER_PRICE_MIGRATION_VERSION
                && "sniper".equals(roleId)
                && revolverPrice == ACCIDENTAL_SNIPER_REVOLVER_PRICE) {
                revolverPrice = fallback.shop_prices.revolver;
            }
            if (sourceVersion < ROLE_BALANCE_MIGRATION_VERSION
                && "sniper".equals(roleId)
                && revolverPrice == PREVIOUS_SNIPER_REVOLVER_PRICE) {
                revolverPrice = fallback.shop_prices.revolver;
            }
            int psychoModePrice = configurableInt(
                shopPrices,
                "psycho_mode",
                fallback.shop_prices.psycho_mode,
                roleId,
                hasConfigurableShopPrice(roleId, ShopItem.PSYCHO_MODE)
            );
            Integer mineStunSeconds = configurableOptionalInt(
                role,
                "mine_stun_seconds",
                fallback.mine_stun_seconds,
                roleId,
                "bomber".equals(roleId)
            );

            if (sourceVersion < MINE_STUN_MIGRATION_VERSION
                && "bomber".equals(roleId)
                && mineStunSeconds != null
                && mineStunSeconds == PREVIOUS_MINE_STUN_SECONDS) {
                mineStunSeconds = fallback.mine_stun_seconds;
            }
            Integer sniperKillsToReset =
                configurableSniperKillsToReset(role, fallback.sniper_kills_to_reset, roleId);
            if (sourceVersion < SNIPER_KILLS_TO_RESET_MIGRATION_VERSION
                && "sniper".equals(roleId)
                && sniperKillsToReset != null
                && sniperKillsToReset == PREVIOUS_SNIPER_KILLS_TO_RESET) {
                sniperKillsToReset = fallback.sniper_kills_to_reset;
            }
            Integer skillDuration = configurableOptionalInt(
                role,
                "skill_duration_seconds",
                fallback.skill_duration_seconds,
                roleId,
                hasConfigurableSkillDuration(roleId)
            );
            if (sourceVersion < ROLE_BALANCE_MIGRATION_VERSION
                && "medium".equals(roleId)
                && skillDuration != null
                && skillDuration == PREVIOUS_MEDIUM_SKILL_DURATION_SECONDS) {
                skillDuration = fallback.skill_duration_seconds;
            }
            Integer initialBalance = configurableOptionalInt(
                role,
                "initial_balance",
                fallback.initial_balance,
                roleId,
                fallback.initial_balance != null
            );
            if (brinRoleStartsBroke(roleId) && initialBalance != null && initialBalance == 100) {
                initialBalance = 0;
            }
            parsedRoles.put(roleId, new RoleSettings(
                skillCost,
                skillCooldown,
                skillDuration,
                initialBalance,
                configurableOptionalInt(
                    role,
                    "forced_sleep_task_cooldown_seconds",
                    fallback.forced_sleep_task_cooldown_seconds,
                    roleId,
                    "nightmare".equals(roleId)
                ),
                configurableOptionalInt(
                    role,
                    "bone_knife_price",
                    fallback.bone_knife_price,
                    roleId,
                    "boneharvester".equals(roleId)
                ),
                configurableOptionalInt(
                    role,
                    "bone_knife_cooldown_seconds",
                    fallback.bone_knife_cooldown_seconds,
                    roleId,
                    "boneharvester".equals(roleId)
                ),
                configurableOptionalInt(
                    role,
                    "explosion_range",
                    fallback.explosion_range,
                    roleId,
                    "terrorist".equals(roleId)
                ),
                configurableOptionalInt(
                    role,
                    "explosion_survival_kills",
                    fallback.explosion_survival_kills,
                    roleId,
                    "terrorist".equals(roleId)
                ),
                configurableOptionalInt(
                    role,
                    "trap_limit",
                    fallback.trap_limit,
                    roleId,
                    "beast_trapper".equals(roleId)
                ),
                configurableOptionalInt(
                    role,
                    "identity_hint_price",
                    fallback.identity_hint_price,
                    roleId,
                    "penitent".equals(roleId)
                ),
                configurableOptionalInt(
                    role,
                    "antidote_price",
                    fallback.antidote_price,
                    roleId,
                    "penitent".equals(roleId)
                ),
                fallback.revolver_shop_enabled,
                new ShopPrices(knifePrice, revolverPrice, psychoModePrice),
                sniperKillsToReset
                , configurableOptionalInt(role, "xuezi_price", fallback.xuezi_price, roleId, "zhangshi".equals(roleId))
                , configurableOptionalInt(role, "xuezi_cooldown_seconds", fallback.xuezi_cooldown_seconds, roleId, "zhangshi".equals(roleId))
                , configurableOptionalInt(role, "xuezi_shield_layers", fallback.xuezi_shield_layers, roleId, "zhangshi".equals(roleId))
                , configurableOptionalDouble(role, "run_speed", fallback.run_speed, roleId, "zhangshi".equals(roleId))
                , configurableOptionalInt(role, "self_destruct_price", fallback.self_destruct_price, roleId, "puppeteer".equals(roleId))
                , configurableOptionalInt(role, "return_cooldown_seconds", fallback.return_cooldown_seconds, roleId, "puppeteer".equals(roleId))
                , configurableOptionalInt(role, "grenade_price", fallback.grenade_price, roleId, "bomber".equals(roleId))
                , migratedBomberBombPrice(role, fallback, roleId, sourceVersion)
                , configurableOptionalInt(role, "bomb_fuse_seconds", fallback.bomb_fuse_seconds, roleId, "bomber".equals(roleId))
                , configurableOptionalInt(role, "bomb_warning_seconds", fallback.bomb_warning_seconds, roleId, "bomber".equals(roleId))
                , configurableOptionalInt(role, "bomb_explosion_size", fallback.bomb_explosion_size, roleId, "bomber".equals(roleId))
                , mineStunSeconds
                , configurableOptionalIntOrDisabled(role, "particle_delay_seconds", fallback.particle_delay_seconds, roleId, "puppeteer".equals(roleId))
                , configurableOptionalInt(role, "bomb_purchase_cooldown_seconds", fallback.bomb_purchase_cooldown_seconds, roleId, "bomber".equals(roleId))
                , configurableOptionalInt(role, "instinct_seconds", fallback.instinct_seconds, roleId, "avenger".equals(roleId))
                , configurableOptionalInt(role, "glow_seconds", fallback.glow_seconds, roleId, "sniper".equals(roleId))
                , configurableOptionalInt(role, "duel_timeout_seconds", fallback.duel_timeout_seconds, roleId, "cowboy".equals(roleId))
                , configurableOptionalInt(role, "duel_countdown_seconds", fallback.duel_countdown_seconds, roleId, "cowboy".equals(roleId))
                , configurableOptionalAnyInt(role, "arena_a_x", fallback.arena_a_x, roleId, "cowboy".equals(roleId))
                , configurableOptionalAnyInt(role, "arena_a_y", fallback.arena_a_y, roleId, "cowboy".equals(roleId))
                , configurableOptionalAnyInt(role, "arena_a_z", fallback.arena_a_z, roleId, "cowboy".equals(roleId))
                , configurableOptionalAnyInt(role, "arena_b_x", fallback.arena_b_x, roleId, "cowboy".equals(roleId))
                , configurableOptionalAnyInt(role, "arena_b_y", fallback.arena_b_y, roleId, "cowboy".equals(roleId))
                , configurableOptionalAnyInt(role, "arena_b_z", fallback.arena_b_z, roleId, "cowboy".equals(roleId))
                , configurableOptionalAnyInt(role, "arena_spectator_x", fallback.arena_spectator_x, roleId, "cowboy".equals(roleId))
                , configurableOptionalAnyInt(role, "arena_spectator_y", fallback.arena_spectator_y, roleId, "cowboy".equals(roleId))
                , configurableOptionalAnyInt(role, "arena_spectator_z", fallback.arena_spectator_z, roleId, "cowboy".equals(roleId))
                , configurableOptionalString(role, "duel_announce_message", fallback.duel_announce_message, roleId, "cowboy".equals(roleId))
                , migratedPenitentStartingShield(role, fallback, roleId, sourceVersion)
                , configurableOptionalMusicVolume(role, "duel_music_volume", fallback.duel_music_volume, roleId)
                , configurableOptionalInt(role, "mine_price", fallback.mine_price, roleId, "bomber".equals(roleId))
                , configurableOptionalInt(role, "extra_trap_price", fallback.extra_trap_price, roleId, "beast_trapper".equals(roleId))
                , configurableOptionalInt(role, "tasks_to_reset", fallback.sniper_tasks_to_reset, roleId, "sniper".equals(roleId))
                , configurableOptionalInt(role, "trap_catch_reward", fallback.trap_catch_reward, roleId, "beast_trapper".equals(roleId))
            ));
        }
        return new Data(CONFIG_VERSION, announcement, afkKick, parsedRoles, harpy);
    }
    private static int sourceVersion(String json) {
        JsonElement rootElement = parseJson5(json);
        if (!rootElement.isJsonObject()) {
            throw new IllegalArgumentException("Config root must be an object");
        }
        return nonNegativeInt(rootElement.getAsJsonObject(), "config_version", 0, "config");
    }

    private static JsonElement parseJson5(String json) {
        JsonReader reader = new JsonReader(new StringReader(stripTrailingCommas(json)));
        reader.setLenient(true);
        return JsonParser.parseReader(reader);
    }
    private static String stripTrailingCommas(String source) {
        StringBuilder result = new StringBuilder(source.length());
        char quote = 0;
        boolean escaped = false;
        boolean lineComment = false;
        boolean blockComment = false;
        for (int index = 0; index < source.length(); index++) {
            char character = source.charAt(index);
            if (quote != 0) {
                result.append(character);
                if (escaped) {
                    escaped = false;
                } else if (character == '\\') {
                    escaped = true;
                } else if (character == quote) {
                    quote = 0;
                }
                continue;
            }
            if (lineComment) {
                result.append(character);
                if (character == '\n' || character == '\r') lineComment = false;
                continue;
            }
            if (blockComment) {
                result.append(character);
                if (character == '*' && index + 1 < source.length() && source.charAt(index + 1) == '/') {
                    result.append('/');
                    index++;
                    blockComment = false;
                }
                continue;
            }
            if (character == '/' && index + 1 < source.length()) {
                char next = source.charAt(index + 1);
                if (next == '/') {
                    result.append(character).append(next);
                    index++;
                    lineComment = true;
                    continue;
                }
                if (next == '*') {
                    result.append(character).append(next);
                    index++;
                    blockComment = true;
                    continue;
                }
            }
            if (character == '\'' || character == '"') {
                quote = character;
                result.append(character);
                continue;
            }
            if (character == ',' && hasTrailingContainerEnd(source, index + 1)) continue;
            result.append(character);
        }
        return result.toString();
    }
    private static boolean hasTrailingContainerEnd(String source, int start) {
        for (int index = start; index < source.length(); index++) {
            char character = source.charAt(index);
            if (Character.isWhitespace(character)) continue;
            if (character == '/' && index + 1 < source.length()) {
                char next = source.charAt(index + 1);
                if (next == '/') {
                    index += 2;
                    while (index < source.length()
                        && source.charAt(index) != '\n'
                        && source.charAt(index) != '\r') {
                        index++;
                    }
                    continue;
                }
                if (next == '*') {
                    int end = source.indexOf("*/", index + 2);
                    if (end < 0) return false;
                    index = end + 1;
                    continue;
                }
            }
            return character == '}' || character == ']';
        }
        return false;
    }
    private static List<String> stringList(JsonObject parent, String key) {
        if (!parent.has(key)) return List.of();
        JsonElement element = parent.get(key);
        if (!element.isJsonArray()) {
            throw new IllegalArgumentException(key + " must be a list of strings");
        }
        ArrayList<String> values = new ArrayList<>();
        for (JsonElement value : element.getAsJsonArray()) {
            if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isString()) {
                throw new IllegalArgumentException(key + " must be a list of strings");
            }
            values.add(value.getAsString());
        }
        return List.copyOf(values);
    }
    private static JsonObject objectOrEmpty(JsonObject parent, String key) {
        if (!parent.has(key)) return new JsonObject();
        JsonElement element = parent.get(key);
        if (!element.isJsonObject()) {
            throw new IllegalArgumentException(key + " must be an object");
        }
        return element.getAsJsonObject();
    }
    private static int nonNegativeInt(JsonObject object, String key, int fallback, String scope) {
        if (!object.has(key)) return fallback;
        JsonElement element = object.get(key);
        if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isNumber()) {
            throw new IllegalArgumentException(scope + "." + key + " must be a non-negative integer");
        }
        String raw = element.getAsString();
        if (!raw.matches("\\d+")) {
            throw new IllegalArgumentException(scope + "." + key + " must be a non-negative integer");
        }
        try {
            long value = Long.parseLong(raw);
            if (value > Integer.MAX_VALUE) throw new NumberFormatException();
            return (int) value;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(scope + "." + key + " is too large");
        }
    }
    private static Integer optionalNonNegativeInt(JsonObject object, String key, Integer fallback, String scope) {
        return fallback == null ? null : nonNegativeInt(object, key, fallback, scope);
    }
    private static Integer optionalAnyInt(JsonObject object, String key, Integer fallback, String scope) {
        if (fallback == null || !object.has(key)) return fallback;
        JsonElement element = object.get(key);
        if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isNumber()) {
            throw new IllegalArgumentException(scope + "." + key + " must be an integer");
        }
        String raw = element.getAsString();
        if (!raw.matches("-?\\d+")) {
            throw new IllegalArgumentException(scope + "." + key + " must be an integer");
        }
        try {
            long value = Long.parseLong(raw);
            if (value > Integer.MAX_VALUE || value < Integer.MIN_VALUE) throw new NumberFormatException();
            return (int) value;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(scope + "." + key + " is too large");
        }
    }
    private static int signedInt(JsonObject object, String key, int fallback, String scope) {
        Integer value = optionalAnyInt(object, key, fallback, scope);
        return value == null ? fallback : value;
    }
    private static LinkedHashMap<String, List<String>> copyBlacklist(Map<String, List<String>> source) {
        LinkedHashMap<String, List<String>> copy = new LinkedHashMap<>();
        for (Map.Entry<String, List<String>> entry : source.entrySet()) {
            copy.put(entry.getKey(), List.copyOf(entry.getValue()));
        }
        return copy;
    }

    private static HarpyAssignmentSettings parseHarpy(JsonObject root, HarpyAssignmentSettings defaults) {
        if (!root.has("harpy")) {
            return migrateHarpyFromHarpyModLoader(defaults);
        }
        JsonObject harpy = objectOrEmpty(root, "harpy");
        return new HarpyAssignmentSettings(
            signedInt(harpy, "neutral_role_count", defaults.neutral_role_count, "harpy"),
            signedInt(harpy, "killer_role_count", defaults.killer_role_count, "harpy"),
            signedInt(harpy, "vigilante_role_count", defaults.vigilante_role_count, "harpy"),
            parseBlacklist(objectOrEmpty(harpy, "role_modifier_blacklist"))
        );
    }
    private static HarpyAssignmentSettings migrateHarpyFromHarpyModLoader(HarpyAssignmentSettings defaults) {
        Path legacyPath = FabricLoader.getInstance().getConfigDir().resolve("harpymodloader.json5");
        if (!Files.exists(legacyPath)) return defaults;
        try {
            JsonObject legacy = parseJson5(Files.readString(legacyPath, StandardCharsets.UTF_8)).getAsJsonObject();
            if (!legacy.has("neutralRoleCount") && !legacy.has("killerRoleCount") && !legacy.has("vigilanteRoleCount")
                && !legacy.has("roleModifierBlacklist")) {
                return defaults;
            }
            BrinsWathe.LOGGER.info("Migrated Harpy assignment settings from {}", legacyPath.getFileName());
            return new HarpyAssignmentSettings(
                signedInt(legacy, "neutralRoleCount", defaults.neutral_role_count, "harpymodloader"),
                signedInt(legacy, "killerRoleCount", defaults.killer_role_count, "harpymodloader"),
                signedInt(legacy, "vigilanteRoleCount", defaults.vigilante_role_count, "harpymodloader"),
                parseBlacklist(objectOrEmpty(legacy, "roleModifierBlacklist"))
            );
        } catch (Exception exception) {
            BrinsWathe.LOGGER.warn("Could not migrate harpymodloader.json5 assignment keys: {}", exception.getMessage());
            return defaults;
        }
    }
    private static LinkedHashMap<String, List<String>> parseBlacklist(JsonObject object) {
        LinkedHashMap<String, List<String>> blacklist = new LinkedHashMap<>();
        for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
            if (!entry.getValue().isJsonArray()) continue;
            List<String> modifiers = new ArrayList<>();
            for (JsonElement element : entry.getValue().getAsJsonArray()) {
                if (element.isJsonPrimitive()) modifiers.add(element.getAsString());
            }
            if (!modifiers.isEmpty()) blacklist.put(entry.getKey(), List.copyOf(modifiers));
        }
        return blacklist;
    }
    private static int configurableInt(
        JsonObject object,
        String key,
        int fallback,
        String scope,
        boolean configurable
    ) {
        return configurable ? nonNegativeInt(object, key, fallback, scope) : fallback;
    }
    private static Integer configurableOptionalInt(
        JsonObject object,
        String key,
        Integer fallback,
        String scope,
        boolean configurable
    ) {
        return configurable ? optionalNonNegativeInt(object, key, fallback, scope) : fallback;
    }
    private static Integer configurableOptionalAnyInt(
        JsonObject object,
        String key,
        Integer fallback,
        String scope,
        boolean configurable
    ) {
        return configurable ? optionalAnyInt(object, key, fallback, scope) : fallback;
    }
    private static Integer configurableOptionalIntOrDisabled(
        JsonObject object,
        String key,
        Integer fallback,
        String scope,
        boolean configurable
    ) {
        return configurable ? optionalIntOrDisabled(object, key, fallback, scope) : fallback;
    }
    private static Integer optionalIntOrDisabled(JsonObject object, String key, Integer fallback, String scope) {
        if (fallback == null || !object.has(key)) return fallback;
        JsonElement element = object.get(key);
        if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isNumber()) {
            throw new IllegalArgumentException(scope + "." + key + " must be -1 or a non-negative integer");
        }
        String raw = element.getAsString();
        if ("-1".equals(raw)) return -1;
        if (!raw.matches("\\d+")) {
            throw new IllegalArgumentException(scope + "." + key + " must be -1 or a non-negative integer");
        }
        try {
            long value = Long.parseLong(raw);
            if (value > Integer.MAX_VALUE) throw new NumberFormatException();
            return (int) value;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(scope + "." + key + " is too large");
        }
    }
    private static String configurableOptionalString(
        JsonObject object,
        String key,
        String fallback,
        String scope,
        boolean configurable
    ) {
        if (!configurable || fallback == null || !object.has(key)) return fallback;
        JsonElement element = object.get(key);
        if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isString()) {
            throw new IllegalArgumentException(scope + "." + key + " must be a string");
        }
        return element.getAsString();
    }
    private static Integer configurableOptionalMusicVolume(
        JsonObject object,
        String key,
        Integer fallback,
        String roleId
    ) {
        if (!"cowboy".equals(roleId) || fallback == null || !object.has(key)) return fallback;
        JsonElement element = object.get(key);
        if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isNumber()) {
            throw new IllegalArgumentException(roleId + "." + key + " must be 0 to 100");
        }
        double value = element.getAsDouble();
        if (!Double.isFinite(value) || value < 0.0D || value > 100.0D) {
            throw new IllegalArgumentException(roleId + "." + key + " must be 0 to 100");
        }
        return (int) Math.round(value);
    }
    private static Double configurableOptionalDouble(
        JsonObject object,
        String key,
        Double fallback,
        String scope,
        boolean configurable
    ) {
        if (!configurable || fallback == null || !object.has(key)) return fallback;
        JsonElement element = object.get(key);
        if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isNumber()) {
            throw new IllegalArgumentException(scope + "." + key + " must be a number from 0.0 to 10.0");
        }
        double value = element.getAsDouble();
        if (!Double.isFinite(value) || value < 0.0D || value > 10.0D) {
            throw new IllegalArgumentException(scope + "." + key + " must be a number from 0.0 to 10.0");
        }
        return value;
    }
    private static Integer configurableSniperKillsToReset(
        JsonObject object,
        Integer fallback,
        String roleId
    ) {
        if (!"sniper".equals(roleId) || fallback == null) return fallback;
        if (!object.has("kills_to_reset")) return fallback;
        JsonElement element = object.get("kills_to_reset");
        if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isNumber()) {
            throw new IllegalArgumentException(roleId + ".kills_to_reset must be -1 or a positive integer");
        }
        String raw = element.getAsString();
        if ("-1".equals(raw)) return -1;
        if (!raw.matches("[1-9]\\d*")) {
            throw new IllegalArgumentException(roleId + ".kills_to_reset must be -1 or a positive integer");
        }
        try {
            long value = Long.parseLong(raw);
            if (value > Integer.MAX_VALUE) throw new NumberFormatException();
            return (int) value;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(roleId + ".kills_to_reset is too large");
        }
    }
    private static Integer migratedBomberBombPrice(
        JsonObject role,
        RoleSettings fallback,
        String roleId,
        int sourceVersion
    ) {
        Integer price = configurableOptionalInt(role, "bomb_price", fallback.bomb_price, roleId, "bomber".equals(roleId));
        if (sourceVersion < ROLE_BALANCE_MIGRATION_VERSION
            && "bomber".equals(roleId)
            && price != null
            && price == PREVIOUS_BOMB_PRICE) {
            return fallback.bomb_price;
        }
        return price;
    }
    private static Integer migratedPenitentStartingShield(
        JsonObject role,
        RoleSettings fallback,
        String roleId,
        int sourceVersion
    ) {
        Integer layers = configurableOptionalInt(
            role,
            "starting_shield_layers",
            fallback.starting_shield_layers,
            roleId,
            "penitent".equals(roleId)
        );
        if (sourceVersion < ROLE_BALANCE_MIGRATION_VERSION
            && "penitent".equals(roleId)
            && layers != null
            && layers == PREVIOUS_PENITENT_STARTING_SHIELD_LAYERS) {
            return fallback.starting_shield_layers;
        }
        return layers;
    }
    private static boolean hasConfigurableSkillCost(String roleId) {
        return switch (roleId) {
            case "archivist", "beast_trapper", "cowboy", "eavesdropper", "gambler", "medium", "vigilante",
                "watchman" -> true;
            default -> false;
        };
    }
    private static boolean hasConfigurableSkillCooldown(String roleId) {
        return switch (roleId) {
            case "archivist", "eavesdropper", "gambler", "illusionist", "medium", "mortician", "nightmare",
                "puppeteer", "sniper", "stalker", "stunt_double", "vigilante", "watchman", "zhangshi" -> true;
            default -> false;
        };
    }
    private static boolean hasConfigurableSkillDuration(String roleId) {
        return switch (roleId) {
            case "avenger", "illusionist", "medium", "puppeteer", "stunt_double", "zhangshi" -> true;
            default -> false;
        };
    }
    private static boolean hasConfigurableShopPrice(String roleId, ShopItem item) {
        return switch (roleId) {
            case "berserker" -> item == ShopItem.PSYCHO_MODE;
            case "boneharvester", "illusionist", "mortician" ->
                item == ShopItem.KNIFE || item == ShopItem.PSYCHO_MODE;
            case "medium" -> item == ShopItem.REVOLVER;
            case "beast_trapper", "penitent", "puppeteer" -> item == ShopItem.KNIFE;
            case "sniper" -> item == ShopItem.REVOLVER;
            case "zhangshi" -> item == ShopItem.PSYCHO_MODE;
            default -> false;
        };
    }
    private static boolean hasAnyConfigurableShopPrice(String roleId) {
        return hasConfigurableShopPrice(roleId, ShopItem.KNIFE)
            || hasConfigurableShopPrice(roleId, ShopItem.REVOLVER)
            || hasConfigurableShopPrice(roleId, ShopItem.PSYCHO_MODE);
    }
    private static boolean booleanValue(JsonObject object, String key, boolean fallback, String scope) {
        if (!object.has(key)) return fallback;
        JsonElement element = object.get(key);
        if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isBoolean()) {
            throw new IllegalArgumentException(scope + "." + key + " must be true or false");
        }
        return element.getAsBoolean();
    }
    private static Data createDefaults() {
        LinkedHashMap<String, RoleSettings> roles = new LinkedHashMap<>();
        roles.put("puppeteer", role(0, 200, null, 200, 300, 300)
            .withSkillDuration(60)
            .withPuppeteer(350, 60, 45));
        roles.put("civilian", civilianRole(0, 0, 350, true));
        roles.put("vigilante", civilianRole(200, 90, 350, false));
        roles.put("stunt_double", civilianRole(0, 30, 350, false).withSkillDuration(30));
        roles.put("medium", civilianRole(200, 120, 500, true).withSkillDuration(8));
        roles.put("eavesdropper", civilianRole(200, 200, 350, false).withSkillDuration(30));
        roles.put("watchman", civilianRole(250, 30, 350, false));
        roles.put("beast_trapper", role(75, 120, null, 100, 300, 300).withTrapLimit(1).withExtraTrapPrice(150).withTrapCatchReward(50));
        roles.put("nightmare", role(0, 60, 0, 100, 300, 300).withForcedSleepTaskCooldown(200));
        roles.put("illusionist", role(0, 120, null, 100, 300, 300).withSkillDuration(10));
        roles.put("sniper", role(0, 100000, null, 150, 225, 300)
            .withSniperKillsToReset(-1)
            .withSniperGlow(10)
            .withSniperTasksToReset(20));
        roles.put("berserker", role(0, 0, 0, 100, 300, 800));
        roles.put("archivist", civilianRole(125, 30, 350, false));
        roles.put("avenger", civilianRole(0, 0, 350, false)
            .withSkillDuration(20)
            .withAvengerInstinct(5));
        roles.put("stalker", civilianRole(0, 90, 350, false));
        roles.put("cowboy", civilianRole(500, 0, 350, false)
            .withCowboy(90, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                "牛仔发起了决斗：{cowboy} 对决 {target}！", 100));
        roles.put("boneharvester", role(0, 0, null, 100, 300, 300).withBoneKnife(175, 200));
        roles.put("bomber", role(0, 0, null, 100, 300, 300).withBomber(200, 75, 45, 30, 2, 3, 60).withMinePrice(125));
        roles.put("compensator", role(0, 0, 0, 100, 300, 300));
        roles.put("gambler", role(100, 120, 0, 100, 300, 300));
        roles.put("penitent", role(0, 0, 0, 100, 300, 300).withPenitentShop(150, 100).withStartingShieldLayers(0));
        roles.put("terrorist", role(0, 0, 0, 100, 300, 300)
            .withTerroristExplosion(5, 4)
            .withRevolverShopEnabled(false));
        roles.put("mortician", role(0, 100, null, 100, 300, 300));
        roles.put("zhangshi", role(0, 90, null, 100, 300, 300)
            .withSkillDuration(10)
            .withXuezi(150, 200, 1)
            .withRunSpeed(2.0D));
        return new Data(
            CONFIG_VERSION,
            List.of(),
            new AfkKickSettings(false, 60, 60),
            roles,
            HarpyAssignmentSettings.defaults()
        );
    }
    private static boolean brinRoleStartsBroke(String roleId) {
        return switch (roleId) {
            case "nightmare", "berserker", "compensator", "gambler", "penitent", "terrorist" -> true;
            default -> false;
        };
    }
    private static RoleSettings role(int cost, int cooldown, Integer balance, int knife, int revolver, int psychoMode) {
        return new RoleSettings(
            cost,
            cooldown,
            null,
            balance,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            new ShopPrices(knife, revolver, psychoMode),
            null
        );
    }
    private static RoleSettings civilianRole(int cost, int cooldown, int revolver, boolean revolverShopEnabled) {
        return new RoleSettings(
            cost,
            cooldown,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            revolverShopEnabled,
            new ShopPrices(100, revolver, 300),
            null
        );
    }
    private static void writeConfig() throws IOException {
        Files.createDirectories(configPath.getParent());
        Files.writeString(configPath, renderJson5(current), StandardCharsets.UTF_8);
    }
    private static String renderJson5(Data data) {
        JsonObject root = new JsonObject();
        root.addProperty("config_version", CONFIG_VERSION);
        JsonArray announcement = new JsonArray();
        for (String line : data.announcement) announcement.add(line);
        root.add("announcement", announcement);

        JsonObject afkKick = new JsonObject();
        afkKick.addProperty("enabled", data.afk_kick.enabled);
        afkKick.addProperty("idle_seconds", data.afk_kick.idle_seconds);
        afkKick.addProperty("countdown_seconds", data.afk_kick.countdown_seconds);
        root.add("afk_kick", afkKick);
        JsonObject harpy = new JsonObject();
        harpy.addProperty("neutral_role_count", data.harpy.neutral_role_count);
        harpy.addProperty("killer_role_count", data.harpy.killer_role_count);
        harpy.addProperty("vigilante_role_count", data.harpy.vigilante_role_count);
        JsonObject blacklist = new JsonObject();
        for (Map.Entry<String, List<String>> entry : data.harpy.role_modifier_blacklist.entrySet()) {
            JsonArray modifiers = new JsonArray();
            for (String modifierId : entry.getValue()) modifiers.add(modifierId);
            blacklist.add(entry.getKey(), modifiers);
        }
        harpy.add("role_modifier_blacklist", blacklist);
        root.add("harpy", harpy);

        JsonObject roles = new JsonObject();
        String[] configurableRoles = {
            "archivist",
            "avenger",
            "berserker",
            "bomber",
            "boneharvester",
            "cowboy",
            "eavesdropper",
            "gambler",
            "illusionist",
            "medium",
            "mortician",
            "nightmare",
            "penitent",
            "puppeteer",
            "sniper",
            "stalker",
            "stunt_double",
            "terrorist",
            "beast_trapper",
            "watchman",
            "zhangshi"
        };
        for (String roleId : configurableRoles) {
            roles.add(roleId, renderRoleSettings(data, roleId));
        }
        root.add("roles", roles);
        return GSON.toJson(root) + System.lineSeparator();
    }
    private static JsonObject renderRoleSettings(Data data, String roleId) {
        RoleSettings settings = data.roles.get(roleId);
        if (settings == null) settings = createDefaults().roles.get(roleId);
        JsonObject role = new JsonObject();
        JsonObject shopPrices = new JsonObject();
        switch (roleId) {
            case "archivist" -> {
                role.addProperty("skill_cost", settings.skill_cost);
                role.addProperty("skill_cooldown_seconds", settings.skill_cooldown_seconds);
            }
            case "avenger" -> {
                role.addProperty("skill_duration_seconds", settings.skill_duration_seconds);
                role.addProperty("instinct_seconds", settings.instinct_seconds);
            }
            case "berserker" -> shopPrices.addProperty("psycho_mode", settings.shop_prices.psycho_mode);
            case "bomber" -> {
                role.addProperty("grenade_price", settings.grenade_price);
                role.addProperty("bomb_price", settings.bomb_price);
                role.addProperty("mine_price", settings.mine_price);
                role.addProperty("bomb_fuse_seconds", settings.bomb_fuse_seconds);
                role.addProperty("bomb_warning_seconds", settings.bomb_warning_seconds);
                role.addProperty("bomb_explosion_size", settings.bomb_explosion_size);
                role.addProperty("mine_stun_seconds", settings.mine_stun_seconds);
                role.addProperty("bomb_purchase_cooldown_seconds", settings.bomb_purchase_cooldown_seconds);
            }
            case "boneharvester" -> {
                shopPrices.addProperty("knife", settings.shop_prices.knife);
                shopPrices.addProperty("psycho_mode", settings.shop_prices.psycho_mode);
                role.addProperty("bone_knife_price", settings.bone_knife_price);
                role.addProperty("bone_knife_cooldown_seconds", settings.bone_knife_cooldown_seconds);
            }
            case "cowboy" -> {
                role.addProperty("skill_cost", settings.skill_cost);
                role.addProperty("duel_timeout_seconds", settings.duel_timeout_seconds);
                role.addProperty("duel_countdown_seconds", settings.duel_countdown_seconds);
                role.addProperty("duel_music_volume", settings.duel_music_volume);
                role.addProperty("duel_announce_message", settings.duel_announce_message);
                role.addProperty("arena_a_x", settings.arena_a_x);
                role.addProperty("arena_a_y", settings.arena_a_y);
                role.addProperty("arena_a_z", settings.arena_a_z);
                role.addProperty("arena_b_x", settings.arena_b_x);
                role.addProperty("arena_b_y", settings.arena_b_y);
                role.addProperty("arena_b_z", settings.arena_b_z);
                role.addProperty("arena_spectator_x", settings.arena_spectator_x);
                role.addProperty("arena_spectator_y", settings.arena_spectator_y);
                role.addProperty("arena_spectator_z", settings.arena_spectator_z);
            }
            case "eavesdropper" -> {
                role.addProperty("skill_cost", settings.skill_cost);
                role.addProperty("skill_cooldown_seconds", settings.skill_cooldown_seconds);
            }
            case "gambler" -> {
                role.addProperty("skill_cost", settings.skill_cost);
                role.addProperty("skill_cooldown_seconds", settings.skill_cooldown_seconds);
            }
            case "illusionist" -> {
                shopPrices.addProperty("knife", settings.shop_prices.knife);
                shopPrices.addProperty("psycho_mode", settings.shop_prices.psycho_mode);
                role.addProperty("skill_cooldown_seconds", settings.skill_cooldown_seconds);
                role.addProperty("skill_duration_seconds", settings.skill_duration_seconds);
            }
            case "medium" -> {
                shopPrices.addProperty("revolver", settings.shop_prices.revolver);
                role.addProperty("skill_cost", settings.skill_cost);
                role.addProperty("skill_cooldown_seconds", settings.skill_cooldown_seconds);
                role.addProperty("skill_duration_seconds", settings.skill_duration_seconds);
            }
            case "mortician" -> {
                shopPrices.addProperty("knife", settings.shop_prices.knife);
                shopPrices.addProperty("psycho_mode", settings.shop_prices.psycho_mode);
                role.addProperty("skill_cooldown_seconds", settings.skill_cooldown_seconds);
            }
            case "nightmare" -> {
                role.addProperty("forced_sleep_task_cooldown_seconds", settings.forced_sleep_task_cooldown_seconds);
                role.addProperty("skill_cooldown_seconds", settings.skill_cooldown_seconds);
            }
            case "penitent" -> {
                shopPrices.addProperty("knife", settings.shop_prices.knife);
                role.addProperty("identity_hint_price", settings.identity_hint_price);
                role.addProperty("antidote_price", settings.antidote_price);
                role.addProperty("starting_shield_layers", settings.starting_shield_layers);
            }
            case "puppeteer" -> {
                shopPrices.addProperty("knife", settings.shop_prices.knife);
                role.addProperty("skill_cooldown_seconds", settings.skill_cooldown_seconds);
                role.addProperty("skill_duration_seconds", settings.skill_duration_seconds);
                role.addProperty("return_cooldown_seconds", settings.return_cooldown_seconds);
                role.addProperty("self_destruct_price", settings.self_destruct_price);
                role.addProperty("particle_delay_seconds", settings.particle_delay_seconds);
            }
            case "sniper" -> {
                shopPrices.addProperty("revolver", settings.shop_prices.revolver);
                role.addProperty("skill_cooldown_seconds", settings.skill_cooldown_seconds);
                role.addProperty("kills_to_reset", settings.sniper_kills_to_reset);
                role.addProperty("tasks_to_reset", settings.sniper_tasks_to_reset);
                role.addProperty("glow_seconds", settings.glow_seconds);
            }
            case "stalker" -> role.addProperty("skill_cooldown_seconds", settings.skill_cooldown_seconds);
            case "stunt_double" -> {
                role.addProperty("skill_cooldown_seconds", settings.skill_cooldown_seconds);
                role.addProperty("skill_duration_seconds", settings.skill_duration_seconds);
            }
            case "terrorist" -> {
                role.addProperty("explosion_range", settings.explosion_range);
                role.addProperty("explosion_survival_kills", settings.explosion_survival_kills);
            }
            case "beast_trapper" -> {
                shopPrices.addProperty("knife", settings.shop_prices.knife);
                role.addProperty("skill_cost", settings.skill_cost);
                role.addProperty("trap_limit", settings.trap_limit);
                role.addProperty("extra_trap_price", settings.extra_trap_price);
                role.addProperty("trap_catch_reward", settings.trap_catch_reward);
            }
            case "watchman" -> {
                role.addProperty("skill_cost", settings.skill_cost);
                role.addProperty("skill_cooldown_seconds", settings.skill_cooldown_seconds);
            }
            case "zhangshi" -> {
                role.addProperty("skill_cooldown_seconds", settings.skill_cooldown_seconds);
                role.addProperty("skill_duration_seconds", settings.skill_duration_seconds);
                shopPrices.addProperty("psycho_mode", settings.shop_prices.psycho_mode);
                role.addProperty("xuezi_price", settings.xuezi_price);
                role.addProperty("xuezi_cooldown_seconds", settings.xuezi_cooldown_seconds);
                role.addProperty("xuezi_shield_layers", settings.xuezi_shield_layers);
                role.addProperty("run_speed", settings.run_speed);
            }
            default -> {
            }
        }
        if (settings.initial_balance != null) {
            role.addProperty("initial_balance", settings.initial_balance);
        }
        if (shopPrices.size() > 0) role.add("shop_prices", shopPrices);
        return role;
    }
    public enum ShopItem {
        KNIFE,
        REVOLVER,
        PSYCHO_MODE
    }
    public static final class Data {
        public final int config_version;
        public final List<String> announcement;
        public final AfkKickSettings afk_kick;
        public final Map<String, RoleSettings> roles;
        public final HarpyAssignmentSettings harpy;

        private Data(
            int configVersion,
            List<String> announcement,
            AfkKickSettings afkKick,
            Map<String, RoleSettings> roles,
            HarpyAssignmentSettings harpy
        ) {
            this.config_version = configVersion;
            this.announcement = announcement;
            this.afk_kick = afkKick;
            this.roles = roles;
            this.harpy = harpy;
        }
    }
    public static final class HarpyAssignmentSettings {
        public final int neutral_role_count;
        public final int killer_role_count;
        public final int vigilante_role_count;
        public final Map<String, List<String>> role_modifier_blacklist;
        private HarpyAssignmentSettings(
            int neutralRoleCount,
            int killerRoleCount,
            int vigilanteRoleCount,
            Map<String, List<String>> roleModifierBlacklist
        ) {
            this.neutral_role_count = neutralRoleCount;
            this.killer_role_count = killerRoleCount;
            this.vigilante_role_count = vigilanteRoleCount;
            this.role_modifier_blacklist = Map.copyOf(roleModifierBlacklist);
        }
        private static HarpyAssignmentSettings defaults() {
            return new HarpyAssignmentSettings(-6, 0, 0, Map.of());
        }
    }
    public static final class AfkKickSettings {
        public final boolean enabled;
        public final int idle_seconds;
        public final int countdown_seconds;
        private AfkKickSettings(boolean enabled, int idleSeconds, int countdownSeconds) {
            this.enabled = enabled;
            this.idle_seconds = idleSeconds;
            this.countdown_seconds = countdownSeconds;
        }
    }
    public static final class RoleSettings {
        public final int skill_cost;
        public final int skill_cooldown_seconds;
        public final Integer skill_duration_seconds;
        public final Integer initial_balance;
        public final Integer forced_sleep_task_cooldown_seconds;
        public final Integer bone_knife_price;
        public final Integer bone_knife_cooldown_seconds;
        public final Integer explosion_range;
        public final Integer explosion_survival_kills;
        public final Integer trap_limit;
        public final Integer identity_hint_price;
        public final Integer antidote_price;
        public final Boolean revolver_shop_enabled;
        public final ShopPrices shop_prices;
        public final Integer sniper_kills_to_reset;
        public final Integer xuezi_price;
        public final Integer xuezi_cooldown_seconds;
        public final Integer xuezi_shield_layers;
        public final Double run_speed;
        public final Integer self_destruct_price;
        public final Integer return_cooldown_seconds;
        public final Integer grenade_price;
        public final Integer bomb_price;
        public final Integer bomb_fuse_seconds;
        public final Integer bomb_warning_seconds;
        public final Integer bomb_explosion_size;
        public final Integer mine_stun_seconds;
        public final Integer particle_delay_seconds;
        public final Integer bomb_purchase_cooldown_seconds;
        public final Integer instinct_seconds;
        public final Integer glow_seconds;
        public final Integer duel_timeout_seconds;
        public final Integer duel_countdown_seconds;
        public final Integer arena_a_x;
        public final Integer arena_a_y;
        public final Integer arena_a_z;
        public final Integer arena_b_x;
        public final Integer arena_b_y;
        public final Integer arena_b_z;
        public final Integer arena_spectator_x;
        public final Integer arena_spectator_y;
        public final Integer arena_spectator_z;
        public final String duel_announce_message;
        public final Integer starting_shield_layers;
        public final Integer duel_music_volume;
        public final Integer mine_price;
        public final Integer extra_trap_price;
        public final Integer sniper_tasks_to_reset;
        public final Integer trap_catch_reward;
        private RoleSettings(
            int skillCost,
            int skillCooldownSeconds,
            Integer skillDurationSeconds,
            Integer initialBalance,
            Integer forcedSleepTaskCooldownSeconds,
            Integer boneKnifePrice,
            Integer boneKnifeCooldownSeconds,
            Integer explosionRange,
            Integer explosionSurvivalKills,
            Integer trapLimit,
            Integer identityHintPrice,
            Integer antidotePrice,
            Boolean revolverShopEnabled,
            ShopPrices shopPrices,
            Integer sniperKillsToReset
        ) {
            this(
                skillCost,
                skillCooldownSeconds,
                skillDurationSeconds,
                initialBalance,
                forcedSleepTaskCooldownSeconds,
                boneKnifePrice,
                boneKnifeCooldownSeconds,
                explosionRange,
                explosionSurvivalKills,
                trapLimit,
                identityHintPrice,
                antidotePrice,
                revolverShopEnabled,
                shopPrices,
                sniperKillsToReset,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
            );
        }
        private RoleSettings(
            int skillCost,
            int skillCooldownSeconds,
            Integer skillDurationSeconds,
            Integer initialBalance,
            Integer forcedSleepTaskCooldownSeconds,
            Integer boneKnifePrice,
            Integer boneKnifeCooldownSeconds,
            Integer explosionRange,
            Integer explosionSurvivalKills,
            Integer trapLimit,
            Integer identityHintPrice,
            Integer antidotePrice,
            Boolean revolverShopEnabled,
            ShopPrices shopPrices,
            Integer sniperKillsToReset,
            Integer xueziPrice,
            Integer xueziCooldownSeconds,
            Integer xueziShieldLayers,
            Double runSpeed,
            Integer selfDestructPrice,
            Integer returnCooldownSeconds,
            Integer grenadePrice,
            Integer bombPrice,
            Integer bombFuseSeconds,
            Integer bombWarningSeconds,
            Integer bombExplosionSize,
            Integer mineStunSeconds,
            Integer particleDelaySeconds,
            Integer bombPurchaseCooldownSeconds,
            Integer instinctSeconds,
            Integer glowSeconds,
            Integer duelTimeoutSeconds,
            Integer duelCountdownSeconds,
            Integer arenaAX,
            Integer arenaAY,
            Integer arenaAZ,
            Integer arenaBX,
            Integer arenaBY,
            Integer arenaBZ,
            Integer arenaSpectatorX,
            Integer arenaSpectatorY,
            Integer arenaSpectatorZ,
            String duelAnnounceMessage,
            Integer startingShieldLayers,
            Integer duelMusicVolume,
            Integer minePrice,
            Integer extraTrapPrice,
            Integer sniperTasksToReset,
            Integer trapCatchReward
        ) {
            this.skill_cost = skillCost;
            this.skill_cooldown_seconds = skillCooldownSeconds;
            this.skill_duration_seconds = skillDurationSeconds;
            this.initial_balance = initialBalance;
            this.forced_sleep_task_cooldown_seconds = forcedSleepTaskCooldownSeconds;
            this.bone_knife_price = boneKnifePrice;
            this.bone_knife_cooldown_seconds = boneKnifeCooldownSeconds;
            this.explosion_range = explosionRange;
            this.explosion_survival_kills = explosionSurvivalKills;
            this.trap_limit = trapLimit;
            this.identity_hint_price = identityHintPrice;
            this.antidote_price = antidotePrice;
            this.revolver_shop_enabled = revolverShopEnabled;
            this.shop_prices = shopPrices;
            this.sniper_kills_to_reset = sniperKillsToReset;
            this.xuezi_price = xueziPrice;
            this.xuezi_cooldown_seconds = xueziCooldownSeconds;
            this.xuezi_shield_layers = xueziShieldLayers;
            this.run_speed = runSpeed;
            this.self_destruct_price = selfDestructPrice;
            this.return_cooldown_seconds = returnCooldownSeconds;
            this.grenade_price = grenadePrice;
            this.bomb_price = bombPrice;
            this.bomb_fuse_seconds = bombFuseSeconds;
            this.bomb_warning_seconds = bombWarningSeconds;
            this.bomb_explosion_size = bombExplosionSize;
            this.mine_stun_seconds = mineStunSeconds;
            this.particle_delay_seconds = particleDelaySeconds;
            this.bomb_purchase_cooldown_seconds = bombPurchaseCooldownSeconds;
            this.instinct_seconds = instinctSeconds;
            this.glow_seconds = glowSeconds;
            this.duel_timeout_seconds = duelTimeoutSeconds;
            this.duel_countdown_seconds = duelCountdownSeconds;
            this.arena_a_x = arenaAX;
            this.arena_a_y = arenaAY;
            this.arena_a_z = arenaAZ;
            this.arena_b_x = arenaBX;
            this.arena_b_y = arenaBY;
            this.arena_b_z = arenaBZ;
            this.arena_spectator_x = arenaSpectatorX;
            this.arena_spectator_y = arenaSpectatorY;
            this.arena_spectator_z = arenaSpectatorZ;
            this.duel_announce_message = duelAnnounceMessage;
            this.starting_shield_layers = startingShieldLayers;
            this.duel_music_volume = duelMusicVolume;
            this.mine_price = minePrice;
            this.extra_trap_price = extraTrapPrice;
            this.sniper_tasks_to_reset = sniperTasksToReset;
            this.trap_catch_reward = trapCatchReward;
        }
        private RoleSettings copy(
            Integer skillDurationSeconds,
            Integer initialBalance,
            Integer forcedSleepTaskCooldownSeconds,
            Integer boneKnifePrice,
            Integer boneKnifeCooldownSeconds,
            Integer explosionRange,
            Integer explosionSurvivalKills,
            Integer trapLimit,
            Integer identityHintPrice,
            Integer antidotePrice,
            Boolean revolverShopEnabled
        ) {
            return new RoleSettings(
                this.skill_cost,
                this.skill_cooldown_seconds,
                skillDurationSeconds,
                initialBalance,
                forcedSleepTaskCooldownSeconds,
                boneKnifePrice,
                boneKnifeCooldownSeconds,
                explosionRange,
                explosionSurvivalKills,
                trapLimit,
                identityHintPrice,
                antidotePrice,
                revolverShopEnabled,
                this.shop_prices,
                this.sniper_kills_to_reset,
                this.xuezi_price,
                this.xuezi_cooldown_seconds,
                this.xuezi_shield_layers,
                this.run_speed,
                this.self_destruct_price,
                this.return_cooldown_seconds,
                this.grenade_price,
                this.bomb_price,
                this.bomb_fuse_seconds,
                this.bomb_warning_seconds,
                this.bomb_explosion_size,
                this.mine_stun_seconds,
                this.particle_delay_seconds,
                this.bomb_purchase_cooldown_seconds,
                this.instinct_seconds,
                this.glow_seconds,
                this.duel_timeout_seconds,
                this.duel_countdown_seconds,
                this.arena_a_x,
                this.arena_a_y,
                this.arena_a_z,
                this.arena_b_x,
                this.arena_b_y,
                this.arena_b_z,
                this.arena_spectator_x,
                this.arena_spectator_y,
                this.arena_spectator_z,
                this.duel_announce_message,
                this.starting_shield_layers,
                this.duel_music_volume,
                this.mine_price,
                this.extra_trap_price,
                this.sniper_tasks_to_reset,
                this.trap_catch_reward
            );
        }
        private RoleSettings withSniperKillsToReset(int killsToReset) {
            return new RoleSettings(
                this.skill_cost,
                this.skill_cooldown_seconds,
                this.skill_duration_seconds,
                this.initial_balance,
                this.forced_sleep_task_cooldown_seconds,
                this.bone_knife_price,
                this.bone_knife_cooldown_seconds,
                this.explosion_range,
                this.explosion_survival_kills,
                this.trap_limit,
                this.identity_hint_price,
                this.antidote_price,
                this.revolver_shop_enabled,
                this.shop_prices,
                killsToReset,
                this.xuezi_price,
                this.xuezi_cooldown_seconds,
                this.xuezi_shield_layers,
                this.run_speed,
                this.self_destruct_price,
                this.return_cooldown_seconds,
                this.grenade_price,
                this.bomb_price,
                this.bomb_fuse_seconds,
                this.bomb_warning_seconds,
                this.bomb_explosion_size,
                this.mine_stun_seconds,
                this.particle_delay_seconds,
                this.bomb_purchase_cooldown_seconds,
                this.instinct_seconds,
                this.glow_seconds,
                this.duel_timeout_seconds,
                this.duel_countdown_seconds,
                this.arena_a_x,
                this.arena_a_y,
                this.arena_a_z,
                this.arena_b_x,
                this.arena_b_y,
                this.arena_b_z,
                this.arena_spectator_x,
                this.arena_spectator_y,
                this.arena_spectator_z,
                this.duel_announce_message,
                this.starting_shield_layers,
                this.duel_music_volume,
                this.mine_price,
                this.extra_trap_price,
                this.sniper_tasks_to_reset,
                this.trap_catch_reward
            );
        }
        private RoleSettings withSniperGlow(int glowSeconds) {
            return new RoleSettings(
                this.skill_cost,
                this.skill_cooldown_seconds,
                this.skill_duration_seconds,
                this.initial_balance,
                this.forced_sleep_task_cooldown_seconds,
                this.bone_knife_price,
                this.bone_knife_cooldown_seconds,
                this.explosion_range,
                this.explosion_survival_kills,
                this.trap_limit,
                this.identity_hint_price,
                this.antidote_price,
                this.revolver_shop_enabled,
                this.shop_prices,
                this.sniper_kills_to_reset,
                this.xuezi_price,
                this.xuezi_cooldown_seconds,
                this.xuezi_shield_layers,
                this.run_speed,
                this.self_destruct_price,
                this.return_cooldown_seconds,
                this.grenade_price,
                this.bomb_price,
                this.bomb_fuse_seconds,
                this.bomb_warning_seconds,
                this.bomb_explosion_size,
                this.mine_stun_seconds,
                this.particle_delay_seconds,
                this.bomb_purchase_cooldown_seconds,
                this.instinct_seconds,
                glowSeconds,
                this.duel_timeout_seconds,
                this.duel_countdown_seconds,
                this.arena_a_x,
                this.arena_a_y,
                this.arena_a_z,
                this.arena_b_x,
                this.arena_b_y,
                this.arena_b_z,
                this.arena_spectator_x,
                this.arena_spectator_y,
                this.arena_spectator_z,
                this.duel_announce_message,
                this.starting_shield_layers,
                this.duel_music_volume,
                this.mine_price,
                this.extra_trap_price,
                this.sniper_tasks_to_reset,
                this.trap_catch_reward
            );
        }
        private RoleSettings withSkillDuration(int seconds) {
            return copy(
                seconds,
                this.initial_balance,
                this.forced_sleep_task_cooldown_seconds,
                this.bone_knife_price,
                this.bone_knife_cooldown_seconds,
                this.explosion_range,
                this.explosion_survival_kills,
                this.trap_limit,
                this.identity_hint_price,
                this.antidote_price,
                this.revolver_shop_enabled
            );
        }
        private RoleSettings withInitialBalance(int balance) {
            return copy(
                this.skill_duration_seconds,
                balance,
                this.forced_sleep_task_cooldown_seconds,
                this.bone_knife_price,
                this.bone_knife_cooldown_seconds,
                this.explosion_range,
                this.explosion_survival_kills,
                this.trap_limit,
                this.identity_hint_price,
                this.antidote_price,
                this.revolver_shop_enabled
            );
        }
        private RoleSettings withForcedSleepTaskCooldown(int seconds) {
            return copy(
                this.skill_duration_seconds,
                this.initial_balance,
                seconds,
                this.bone_knife_price,
                this.bone_knife_cooldown_seconds,
                this.explosion_range,
                this.explosion_survival_kills,
                this.trap_limit,
                this.identity_hint_price,
                this.antidote_price,
                this.revolver_shop_enabled
            );
        }
        private RoleSettings withBoneKnife(int price, int cooldown) {
            return copy(
                this.skill_duration_seconds,
                this.initial_balance,
                this.forced_sleep_task_cooldown_seconds,
                price,
                cooldown,
                this.explosion_range,
                this.explosion_survival_kills,
                this.trap_limit,
                this.identity_hint_price,
                this.antidote_price,
                this.revolver_shop_enabled
            );
        }
        private RoleSettings withTerroristExplosion(int range, int survivalKills) {
            return copy(
                this.skill_duration_seconds,
                this.initial_balance,
                this.forced_sleep_task_cooldown_seconds,
                this.bone_knife_price,
                this.bone_knife_cooldown_seconds,
                range,
                survivalKills,
                this.trap_limit,
                this.identity_hint_price,
                this.antidote_price,
                this.revolver_shop_enabled
            );
        }
        private RoleSettings withTrapLimit(int limit) {
            return copy(
                this.skill_duration_seconds,
                this.initial_balance,
                this.forced_sleep_task_cooldown_seconds,
                this.bone_knife_price,
                this.bone_knife_cooldown_seconds,
                this.explosion_range,
                this.explosion_survival_kills,
                limit,
                this.identity_hint_price,
                this.antidote_price,
                this.revolver_shop_enabled
            );
        }
        private RoleSettings withPenitentShop(int identityHintPrice, int antidotePrice) {
            return copy(
                this.skill_duration_seconds,
                this.initial_balance,
                this.forced_sleep_task_cooldown_seconds,
                this.bone_knife_price,
                this.bone_knife_cooldown_seconds,
                this.explosion_range,
                this.explosion_survival_kills,
                this.trap_limit,
                identityHintPrice,
                antidotePrice,
                this.revolver_shop_enabled
            );
        }
        private RoleSettings withRevolverShopEnabled(boolean enabled) {
            return copy(
                this.skill_duration_seconds,
                this.initial_balance,
                this.forced_sleep_task_cooldown_seconds,
                this.bone_knife_price,
                this.bone_knife_cooldown_seconds,
                this.explosion_range,
                this.explosion_survival_kills,
                this.trap_limit,
                this.identity_hint_price,
                this.antidote_price,
                enabled
            );
        }
        private RoleSettings withXuezi(int price, int cooldown, int shieldLayers) {
            return new RoleSettings(
                this.skill_cost,
                this.skill_cooldown_seconds,
                this.skill_duration_seconds,
                this.initial_balance,
                this.forced_sleep_task_cooldown_seconds,
                this.bone_knife_price,
                this.bone_knife_cooldown_seconds,
                this.explosion_range,
                this.explosion_survival_kills,
                this.trap_limit,
                this.identity_hint_price,
                this.antidote_price,
                this.revolver_shop_enabled,
                this.shop_prices,
                this.sniper_kills_to_reset,
                price,
                cooldown,
                shieldLayers,
                this.run_speed,
                this.self_destruct_price,
                this.return_cooldown_seconds,
                this.grenade_price,
                this.bomb_price,
                this.bomb_fuse_seconds,
                this.bomb_warning_seconds,
                this.bomb_explosion_size,
                this.mine_stun_seconds,
                this.particle_delay_seconds,
                this.bomb_purchase_cooldown_seconds,
                this.instinct_seconds,
                this.glow_seconds,
                this.duel_timeout_seconds,
                this.duel_countdown_seconds,
                this.arena_a_x,
                this.arena_a_y,
                this.arena_a_z,
                this.arena_b_x,
                this.arena_b_y,
                this.arena_b_z,
                this.arena_spectator_x,
                this.arena_spectator_y,
                this.arena_spectator_z,
                this.duel_announce_message,
                this.starting_shield_layers,
                this.duel_music_volume,
                this.mine_price,
                this.extra_trap_price,
                this.sniper_tasks_to_reset,
                this.trap_catch_reward
            );
        }
        private RoleSettings withRunSpeed(double runSpeed) {
            return new RoleSettings(
                this.skill_cost,
                this.skill_cooldown_seconds,
                this.skill_duration_seconds,
                this.initial_balance,
                this.forced_sleep_task_cooldown_seconds,
                this.bone_knife_price,
                this.bone_knife_cooldown_seconds,
                this.explosion_range,
                this.explosion_survival_kills,
                this.trap_limit,
                this.identity_hint_price,
                this.antidote_price,
                this.revolver_shop_enabled,
                this.shop_prices,
                this.sniper_kills_to_reset,
                this.xuezi_price,
                this.xuezi_cooldown_seconds,
                this.xuezi_shield_layers,
                runSpeed,
                this.self_destruct_price,
                this.return_cooldown_seconds,
                this.grenade_price,
                this.bomb_price,
                this.bomb_fuse_seconds,
                this.bomb_warning_seconds,
                this.bomb_explosion_size,
                this.mine_stun_seconds,
                this.particle_delay_seconds,
                this.bomb_purchase_cooldown_seconds,
                this.instinct_seconds,
                this.glow_seconds,
                this.duel_timeout_seconds,
                this.duel_countdown_seconds,
                this.arena_a_x,
                this.arena_a_y,
                this.arena_a_z,
                this.arena_b_x,
                this.arena_b_y,
                this.arena_b_z,
                this.arena_spectator_x,
                this.arena_spectator_y,
                this.arena_spectator_z,
                this.duel_announce_message,
                this.starting_shield_layers,
                this.duel_music_volume,
                this.mine_price,
                this.extra_trap_price,
                this.sniper_tasks_to_reset,
                this.trap_catch_reward
            );
        }
        private RoleSettings withPuppeteer(int selfDestructPrice, int returnCooldownSeconds,
                                           int particleDelaySeconds) {
            return new RoleSettings(
                this.skill_cost,
                this.skill_cooldown_seconds,
                this.skill_duration_seconds,
                this.initial_balance,
                this.forced_sleep_task_cooldown_seconds,
                this.bone_knife_price,
                this.bone_knife_cooldown_seconds,
                this.explosion_range,
                this.explosion_survival_kills,
                this.trap_limit,
                this.identity_hint_price,
                this.antidote_price,
                this.revolver_shop_enabled,
                this.shop_prices,
                this.sniper_kills_to_reset,
                this.xuezi_price,
                this.xuezi_cooldown_seconds,
                this.xuezi_shield_layers,
                this.run_speed,
                selfDestructPrice,
                returnCooldownSeconds,
                this.grenade_price,
                this.bomb_price,
                this.bomb_fuse_seconds,
                this.bomb_warning_seconds,
                this.bomb_explosion_size,
                this.mine_stun_seconds,
                particleDelaySeconds,
                this.bomb_purchase_cooldown_seconds,
                this.instinct_seconds,
                this.glow_seconds,
                this.duel_timeout_seconds,
                this.duel_countdown_seconds,
                this.arena_a_x,
                this.arena_a_y,
                this.arena_a_z,
                this.arena_b_x,
                this.arena_b_y,
                this.arena_b_z,
                this.arena_spectator_x,
                this.arena_spectator_y,
                this.arena_spectator_z,
                this.duel_announce_message,
                this.starting_shield_layers,
                this.duel_music_volume,
                this.mine_price,
                this.extra_trap_price,
                this.sniper_tasks_to_reset,
                this.trap_catch_reward
            );
        }
        private RoleSettings withBomber(
            int grenadePrice,
            int bombPrice,
            int fuseSeconds,
            int warningSeconds,
            int explosionSize,
            int mineStunSeconds,
            int bombPurchaseCooldownSeconds
        ) {
            return new RoleSettings(
                this.skill_cost,
                this.skill_cooldown_seconds,
                this.skill_duration_seconds,
                this.initial_balance,
                this.forced_sleep_task_cooldown_seconds,
                this.bone_knife_price,
                this.bone_knife_cooldown_seconds,
                this.explosion_range,
                this.explosion_survival_kills,
                this.trap_limit,
                this.identity_hint_price,
                this.antidote_price,
                this.revolver_shop_enabled,
                this.shop_prices,
                this.sniper_kills_to_reset,
                this.xuezi_price,
                this.xuezi_cooldown_seconds,
                this.xuezi_shield_layers,
                this.run_speed,
                this.self_destruct_price,
                this.return_cooldown_seconds,
                grenadePrice,
                bombPrice,
                fuseSeconds,
                warningSeconds,
                explosionSize,
                mineStunSeconds,
                this.particle_delay_seconds,
                bombPurchaseCooldownSeconds,
                this.instinct_seconds,
                this.glow_seconds,
                this.duel_timeout_seconds,
                this.duel_countdown_seconds,
                this.arena_a_x,
                this.arena_a_y,
                this.arena_a_z,
                this.arena_b_x,
                this.arena_b_y,
                this.arena_b_z,
                this.arena_spectator_x,
                this.arena_spectator_y,
                this.arena_spectator_z,
                this.duel_announce_message,
                this.starting_shield_layers,
                this.duel_music_volume,
                this.mine_price,
                this.extra_trap_price,
                this.sniper_tasks_to_reset,
                this.trap_catch_reward
            );
        }
        private RoleSettings withAvengerInstinct(int instinctSeconds) {
            return new RoleSettings(
                this.skill_cost,
                this.skill_cooldown_seconds,
                this.skill_duration_seconds,
                this.initial_balance,
                this.forced_sleep_task_cooldown_seconds,
                this.bone_knife_price,
                this.bone_knife_cooldown_seconds,
                this.explosion_range,
                this.explosion_survival_kills,
                this.trap_limit,
                this.identity_hint_price,
                this.antidote_price,
                this.revolver_shop_enabled,
                this.shop_prices,
                this.sniper_kills_to_reset,
                this.xuezi_price,
                this.xuezi_cooldown_seconds,
                this.xuezi_shield_layers,
                this.run_speed,
                this.self_destruct_price,
                this.return_cooldown_seconds,
                this.grenade_price,
                this.bomb_price,
                this.bomb_fuse_seconds,
                this.bomb_warning_seconds,
                this.bomb_explosion_size,
                this.mine_stun_seconds,
                this.particle_delay_seconds,
                this.bomb_purchase_cooldown_seconds,
                instinctSeconds,
                this.glow_seconds,
                this.duel_timeout_seconds,
                this.duel_countdown_seconds,
                this.arena_a_x,
                this.arena_a_y,
                this.arena_a_z,
                this.arena_b_x,
                this.arena_b_y,
                this.arena_b_z,
                this.arena_spectator_x,
                this.arena_spectator_y,
                this.arena_spectator_z,
                this.duel_announce_message,
                this.starting_shield_layers,
                this.duel_music_volume,
                this.mine_price,
                this.extra_trap_price,
                this.sniper_tasks_to_reset,
                this.trap_catch_reward
            );
        }
        private RoleSettings withCowboy(
            int duelTimeoutSeconds,
            int duelCountdownSeconds,
            int arenaAX,
            int arenaAY,
            int arenaAZ,
            int arenaBX,
            int arenaBY,
            int arenaBZ,
            int arenaSpectatorX,
            int arenaSpectatorY,
            int arenaSpectatorZ,
            String duelAnnounceMessage,
            int duelMusicVolume
        ) {
            return new RoleSettings(
                this.skill_cost,
                this.skill_cooldown_seconds,
                this.skill_duration_seconds,
                this.initial_balance,
                this.forced_sleep_task_cooldown_seconds,
                this.bone_knife_price,
                this.bone_knife_cooldown_seconds,
                this.explosion_range,
                this.explosion_survival_kills,
                this.trap_limit,
                this.identity_hint_price,
                this.antidote_price,
                this.revolver_shop_enabled,
                this.shop_prices,
                this.sniper_kills_to_reset,
                this.xuezi_price,
                this.xuezi_cooldown_seconds,
                this.xuezi_shield_layers,
                this.run_speed,
                this.self_destruct_price,
                this.return_cooldown_seconds,
                this.grenade_price,
                this.bomb_price,
                this.bomb_fuse_seconds,
                this.bomb_warning_seconds,
                this.bomb_explosion_size,
                this.mine_stun_seconds,
                this.particle_delay_seconds,
                this.bomb_purchase_cooldown_seconds,
                this.instinct_seconds,
                this.glow_seconds,
                duelTimeoutSeconds,
                duelCountdownSeconds,
                arenaAX,
                arenaAY,
                arenaAZ,
                arenaBX,
                arenaBY,
                arenaBZ,
                arenaSpectatorX,
                arenaSpectatorY,
                arenaSpectatorZ,
                duelAnnounceMessage,
                this.starting_shield_layers,
                duelMusicVolume,
                this.mine_price,
                this.extra_trap_price,
                this.sniper_tasks_to_reset,
                this.trap_catch_reward
            );
        }
        private RoleSettings withMinePrice(int minePrice) {
            return new RoleSettings(
                this.skill_cost,
                this.skill_cooldown_seconds,
                this.skill_duration_seconds,
                this.initial_balance,
                this.forced_sleep_task_cooldown_seconds,
                this.bone_knife_price,
                this.bone_knife_cooldown_seconds,
                this.explosion_range,
                this.explosion_survival_kills,
                this.trap_limit,
                this.identity_hint_price,
                this.antidote_price,
                this.revolver_shop_enabled,
                this.shop_prices,
                this.sniper_kills_to_reset,
                this.xuezi_price,
                this.xuezi_cooldown_seconds,
                this.xuezi_shield_layers,
                this.run_speed,
                this.self_destruct_price,
                this.return_cooldown_seconds,
                this.grenade_price,
                this.bomb_price,
                this.bomb_fuse_seconds,
                this.bomb_warning_seconds,
                this.bomb_explosion_size,
                this.mine_stun_seconds,
                this.particle_delay_seconds,
                this.bomb_purchase_cooldown_seconds,
                this.instinct_seconds,
                this.glow_seconds,
                this.duel_timeout_seconds,
                this.duel_countdown_seconds,
                this.arena_a_x,
                this.arena_a_y,
                this.arena_a_z,
                this.arena_b_x,
                this.arena_b_y,
                this.arena_b_z,
                this.arena_spectator_x,
                this.arena_spectator_y,
                this.arena_spectator_z,
                this.duel_announce_message,
                this.starting_shield_layers,
                this.duel_music_volume,
                minePrice,
                this.extra_trap_price,
                this.sniper_tasks_to_reset,
                this.trap_catch_reward
            );
        }
        private RoleSettings withExtraTrapPrice(int extraTrapPrice) {
            return new RoleSettings(
                this.skill_cost,
                this.skill_cooldown_seconds,
                this.skill_duration_seconds,
                this.initial_balance,
                this.forced_sleep_task_cooldown_seconds,
                this.bone_knife_price,
                this.bone_knife_cooldown_seconds,
                this.explosion_range,
                this.explosion_survival_kills,
                this.trap_limit,
                this.identity_hint_price,
                this.antidote_price,
                this.revolver_shop_enabled,
                this.shop_prices,
                this.sniper_kills_to_reset,
                this.xuezi_price,
                this.xuezi_cooldown_seconds,
                this.xuezi_shield_layers,
                this.run_speed,
                this.self_destruct_price,
                this.return_cooldown_seconds,
                this.grenade_price,
                this.bomb_price,
                this.bomb_fuse_seconds,
                this.bomb_warning_seconds,
                this.bomb_explosion_size,
                this.mine_stun_seconds,
                this.particle_delay_seconds,
                this.bomb_purchase_cooldown_seconds,
                this.instinct_seconds,
                this.glow_seconds,
                this.duel_timeout_seconds,
                this.duel_countdown_seconds,
                this.arena_a_x,
                this.arena_a_y,
                this.arena_a_z,
                this.arena_b_x,
                this.arena_b_y,
                this.arena_b_z,
                this.arena_spectator_x,
                this.arena_spectator_y,
                this.arena_spectator_z,
                this.duel_announce_message,
                this.starting_shield_layers,
                this.duel_music_volume,
                this.mine_price,
                extraTrapPrice,
                this.sniper_tasks_to_reset,
                this.trap_catch_reward
            );
        }
        private RoleSettings withSniperTasksToReset(int tasksToReset) {
            return new RoleSettings(
                this.skill_cost,
                this.skill_cooldown_seconds,
                this.skill_duration_seconds,
                this.initial_balance,
                this.forced_sleep_task_cooldown_seconds,
                this.bone_knife_price,
                this.bone_knife_cooldown_seconds,
                this.explosion_range,
                this.explosion_survival_kills,
                this.trap_limit,
                this.identity_hint_price,
                this.antidote_price,
                this.revolver_shop_enabled,
                this.shop_prices,
                this.sniper_kills_to_reset,
                this.xuezi_price,
                this.xuezi_cooldown_seconds,
                this.xuezi_shield_layers,
                this.run_speed,
                this.self_destruct_price,
                this.return_cooldown_seconds,
                this.grenade_price,
                this.bomb_price,
                this.bomb_fuse_seconds,
                this.bomb_warning_seconds,
                this.bomb_explosion_size,
                this.mine_stun_seconds,
                this.particle_delay_seconds,
                this.bomb_purchase_cooldown_seconds,
                this.instinct_seconds,
                this.glow_seconds,
                this.duel_timeout_seconds,
                this.duel_countdown_seconds,
                this.arena_a_x,
                this.arena_a_y,
                this.arena_a_z,
                this.arena_b_x,
                this.arena_b_y,
                this.arena_b_z,
                this.arena_spectator_x,
                this.arena_spectator_y,
                this.arena_spectator_z,
                this.duel_announce_message,
                this.starting_shield_layers,
                this.duel_music_volume,
                this.mine_price,
                this.extra_trap_price,
                tasksToReset,
                this.trap_catch_reward
            );
        }
        private RoleSettings withTrapCatchReward(int trapCatchReward) {
            return new RoleSettings(
                this.skill_cost,
                this.skill_cooldown_seconds,
                this.skill_duration_seconds,
                this.initial_balance,
                this.forced_sleep_task_cooldown_seconds,
                this.bone_knife_price,
                this.bone_knife_cooldown_seconds,
                this.explosion_range,
                this.explosion_survival_kills,
                this.trap_limit,
                this.identity_hint_price,
                this.antidote_price,
                this.revolver_shop_enabled,
                this.shop_prices,
                this.sniper_kills_to_reset,
                this.xuezi_price,
                this.xuezi_cooldown_seconds,
                this.xuezi_shield_layers,
                this.run_speed,
                this.self_destruct_price,
                this.return_cooldown_seconds,
                this.grenade_price,
                this.bomb_price,
                this.bomb_fuse_seconds,
                this.bomb_warning_seconds,
                this.bomb_explosion_size,
                this.mine_stun_seconds,
                this.particle_delay_seconds,
                this.bomb_purchase_cooldown_seconds,
                this.instinct_seconds,
                this.glow_seconds,
                this.duel_timeout_seconds,
                this.duel_countdown_seconds,
                this.arena_a_x,
                this.arena_a_y,
                this.arena_a_z,
                this.arena_b_x,
                this.arena_b_y,
                this.arena_b_z,
                this.arena_spectator_x,
                this.arena_spectator_y,
                this.arena_spectator_z,
                this.duel_announce_message,
                this.starting_shield_layers,
                this.duel_music_volume,
                this.mine_price,
                this.extra_trap_price,
                this.sniper_tasks_to_reset,
                trapCatchReward
            );
        }
        private RoleSettings withStartingShieldLayers(int layers) {
            return new RoleSettings(
                this.skill_cost,
                this.skill_cooldown_seconds,
                this.skill_duration_seconds,
                this.initial_balance,
                this.forced_sleep_task_cooldown_seconds,
                this.bone_knife_price,
                this.bone_knife_cooldown_seconds,
                this.explosion_range,
                this.explosion_survival_kills,
                this.trap_limit,
                this.identity_hint_price,
                this.antidote_price,
                this.revolver_shop_enabled,
                this.shop_prices,
                this.sniper_kills_to_reset,
                this.xuezi_price,
                this.xuezi_cooldown_seconds,
                this.xuezi_shield_layers,
                this.run_speed,
                this.self_destruct_price,
                this.return_cooldown_seconds,
                this.grenade_price,
                this.bomb_price,
                this.bomb_fuse_seconds,
                this.bomb_warning_seconds,
                this.bomb_explosion_size,
                this.mine_stun_seconds,
                this.particle_delay_seconds,
                this.bomb_purchase_cooldown_seconds,
                this.instinct_seconds,
                this.glow_seconds,
                this.duel_timeout_seconds,
                this.duel_countdown_seconds,
                this.arena_a_x,
                this.arena_a_y,
                this.arena_a_z,
                this.arena_b_x,
                this.arena_b_y,
                this.arena_b_z,
                this.arena_spectator_x,
                this.arena_spectator_y,
                this.arena_spectator_z,
                this.duel_announce_message,
                layers,
                this.duel_music_volume,
                this.mine_price,
                this.extra_trap_price,
                this.sniper_tasks_to_reset,
                this.trap_catch_reward
            );
        }
    }
    public static final class ShopPrices {
        public final int knife;
        public final int revolver;
        public final int psycho_mode;
        private ShopPrices(int knife, int revolver, int psychoMode) {
            this.knife = knife;
            this.revolver = revolver;
            this.psycho_mode = psychoMode;
        }
    }
}
