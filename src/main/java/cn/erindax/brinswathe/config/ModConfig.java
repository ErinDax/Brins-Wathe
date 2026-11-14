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
import java.util.Locale;
import java.util.Map;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.Nullable;

public final class ModConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String FILE_NAME = "brinswathe.json5";
    private static final String LEGACY_FILE_NAME = "brinswathe.json";
    private static final int CONFIG_VERSION = 55;
    private static final int AFK_DEFAULT_ON_MIGRATION_VERSION = 43;
    private static final Tunable[] STARTS_BROKE = {
        Tunable.BERSERKER_INITIAL_BALANCE,
        Tunable.COMPENSATOR_INITIAL_BALANCE,
        Tunable.GAMBLER_INITIAL_BALANCE,
        Tunable.NIGHTMARE_INITIAL_BALANCE,
        Tunable.PENITENT_INITIAL_BALANCE,
        Tunable.TERRORIST_INITIAL_BALANCE
    };
    private static final Migration[] MIGRATIONS = {
        new Migration(Tunable.SNIPER_SHOP_REVOLVER, 11, 175),
        new Migration(Tunable.SNIPER_SKILL_COOLDOWN_SECONDS, 17, 150),
        new Migration(Tunable.SNIPER_SKILL_COOLDOWN_SECONDS, 17, 300),
        new Migration(Tunable.SNIPER_KILLS_TO_RESET, 18, 3),
        new Migration(Tunable.BOMBER_MINE_STUN_SECONDS, 23, 0),
        new Migration(Tunable.ARCHIVIST_SKILL_COST, 39, 175),
        new Migration(Tunable.ARCHIVIST_SKILL_COOLDOWN_SECONDS, 39, 120),
        new Migration(Tunable.BOMBER_BOMB_PRICE, 39, 100),
        new Migration(Tunable.COWBOY_SKILL_COST, 39, 450),
        new Migration(Tunable.MEDIUM_SKILL_DURATION_SECONDS, 39, 5),
        new Migration(Tunable.MORTICIAN_SKILL_COOLDOWN_SECONDS, 39, 120),
        new Migration(Tunable.PENITENT_STARTING_SHIELD_LAYERS, 39, 1),
        new Migration(Tunable.SNIPER_SHOP_REVOLVER, 39, 300),
        new Migration(Tunable.STALKER_SKILL_COOLDOWN_SECONDS, 39, 150),
        new Migration(Tunable.TRAPPER_SKILL_COST, 39, 125),
        new Migration(Tunable.WATCHMAN_SKILL_COOLDOWN_SECONDS, 39, 200),
        new Migration(Tunable.WATCHMAN_SKILL_COST, 39, 300),
        new Migration(Tunable.AVENGER_INSTINCT_SECONDS, 40, 5),
        new Migration(Tunable.PUPPETEER_SHOP_KNIFE, 41, 200),
        new Migration(Tunable.TRAPPER_SKILL_COST, 42, 75),
        new Migration(Tunable.WATCHMAN_SKILL_COST, 44, 250),
        new Migration(Tunable.PENITENT_INITIAL_BALANCE, 45, 0)
    };
    private static volatile Data current = createDefaults();
    private static Path configPath;

    private ModConfig() {
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
            current.harpy,
            current.numbers,
            current.texts
        );
        if (configPath == null) {
            configPath = FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME);
        }
        writeConfig();
    }

    public static int skillCost(String roleId) {
        Tunable tunable = Tunable.role(roleId, "skill_cost");
        return tunable == null ? 0 : tunable.intValue();
    }

    public static int skillCooldownSeconds(String roleId) {
        Tunable tunable = Tunable.role(roleId, "skill_cooldown_seconds");
        if (tunable != null) return tunable.intValue();
        return "beast_trapper".equals(roleId) ? 120 : 0;
    }

    public static int sniperKillsToReset() {
        return Tunable.SNIPER_KILLS_TO_RESET.intValue();
    }

    public static int sniperGlowSeconds() {
        return Tunable.SNIPER_GLOW_SECONDS.intValue();
    }

    public static int skillDurationSeconds(String roleId) {
        Tunable tunable = Tunable.role(roleId, "skill_duration_seconds");
        if (tunable != null) return tunable.intValue();
        return "conspirator".equals(roleId) ? 15 : 0;
    }

    public static int nightmareForcedSleepCooldownSeconds() {
        return Tunable.NIGHTMARE_FORCED_SLEEP_TASK_COOLDOWN_SECONDS.intValue();
    }

    public static int boneKnifePrice() {
        return Tunable.BONEHARVESTER_BONE_KNIFE_PRICE.intValue();
    }

    public static int boneKnifeCooldownSeconds() {
        return Tunable.BONEHARVESTER_BONE_KNIFE_COOLDOWN_SECONDS.intValue();
    }

    public static int xueziPrice() {
        return Tunable.ZHANGSHI_XUEZI_PRICE.intValue();
    }

    public static int xueziCooldownSeconds() {
        return Tunable.ZHANGSHI_XUEZI_COOLDOWN_SECONDS.intValue();
    }

    public static int xueziShieldLayers() {
        return Math.min(5, Tunable.ZHANGSHI_XUEZI_SHIELD_LAYERS.intValue());
    }

    public static float zhangshiRunSpeed() {
        return (float) Tunable.ZHANGSHI_RUN_SPEED.doubleValue();
    }

    public static int penitentIdentityHintPrice() {
        return Tunable.PENITENT_IDENTITY_HINT_PRICE.intValue();
    }

    public static int penitentAntidotePrice() {
        return Tunable.PENITENT_ANTIDOTE_PRICE.intValue();
    }

    public static int penitentStartingShieldLayers() {
        return Tunable.PENITENT_STARTING_SHIELD_LAYERS.intValue();
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
            new HarpyAssignmentSettings(
                kind == HarpyCountKind.NEUTRAL ? value : harpy.neutral_role_count,
                kind == HarpyCountKind.KILLER ? value : harpy.killer_role_count,
                kind == HarpyCountKind.VIGILANTE ? value : harpy.vigilante_role_count,
                copyBlacklist(harpy.role_modifier_blacklist)
            ),
            current.numbers,
            current.texts
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
            new HarpyAssignmentSettings(
                current.harpy.neutral_role_count,
                current.harpy.killer_role_count,
                current.harpy.vigilante_role_count,
                blacklist
            ),
            current.numbers,
            current.texts
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
        return Tunable.PUPPETEER_SELF_DESTRUCT_PRICE.intValue();
    }

    public static int puppeteerReturnCooldownSeconds() {
        return Tunable.PUPPETEER_RETURN_COOLDOWN_SECONDS.intValue();
    }

    public static int puppeteerParticleDelaySeconds() {
        return Tunable.PUPPETEER_PARTICLE_DELAY_SECONDS.intValue();
    }

    public static int puppeteerSelfDestructRange() {
        return Tunable.PUPPETEER_SELF_DESTRUCT_RANGE.intValue();
    }

    public static int terroristExplosionRange() {
        return Tunable.TERRORIST_EXPLOSION_RANGE.intValue();
    }

    public static int terroristExplosionSurvivalKills() {
        return Tunable.TERRORIST_EXPLOSION_SURVIVAL_KILLS.intValue();
    }

    public static int trapperTrapLimit() {
        return Tunable.TRAPPER_TRAP_LIMIT.intValue();
    }

    public static int bomberGrenadePrice() {
        return Tunable.BOMBER_GRENADE_PRICE.intValue();
    }

    public static int bomberBombPrice() {
        return Tunable.BOMBER_BOMB_PRICE.intValue();
    }

    public static int bomberMinePrice() {
        return Tunable.BOMBER_MINE_PRICE.intValue();
    }

    public static int trapperExtraTrapPrice() {
        return Tunable.TRAPPER_EXTRA_TRAP_PRICE.intValue();
    }

    public static int trapperCatchReward() {
        return Tunable.TRAPPER_TRAP_CATCH_REWARD.intValue();
    }

    public static int zhuimuStrugglePresses() {
        return Math.max(1, Tunable.ZHUIMU_STRUGGLE_PRESSES.intValue());
    }

    public static int sniperTasksToReset() {
        return Tunable.SNIPER_TASKS_TO_RESET.intValue();
    }

    public static int sniperKillTaskProgress() {
        return 3;
    }

    public static int bomberBombFuseSeconds() {
        return Math.max(1, Tunable.BOMBER_BOMB_FUSE_SECONDS.intValue());
    }

    public static int bomberBombWarningSeconds() {
        return Tunable.BOMBER_BOMB_WARNING_SECONDS.intValue();
    }

    public static int bomberBombExplosionSize() {
        return Math.max(1, Tunable.BOMBER_BOMB_EXPLOSION_SIZE.intValue());
    }

    public static int bomberBombPurchaseCooldownSeconds() {
        return Tunable.BOMBER_BOMB_PURCHASE_COOLDOWN_SECONDS.intValue();
    }

    public static int bomberMineStunSeconds() {
        return Tunable.BOMBER_MINE_STUN_SECONDS.intValue();
    }

    public static int avengerGunSeconds() {
        return Math.max(1, Tunable.AVENGER_SKILL_DURATION_SECONDS.intValue());
    }

    public static int avengerInstinctSeconds() {
        return Tunable.AVENGER_INSTINCT_SECONDS.intValue();
    }

    public static String cowboyDuelAnnounceMessage() {
        return Tunable.COWBOY_DUEL_ANNOUNCE_MESSAGE.text();
    }

    public static int cowboyDuelTimeoutSeconds() {
        return Math.max(1, Tunable.COWBOY_DUEL_TIMEOUT_SECONDS.intValue());
    }

    public static int cowboyDuelCountdownSeconds() {
        return Tunable.COWBOY_DUEL_COUNTDOWN_SECONDS.intValue();
    }

    public static float cowboyDuelMusicVolume() {
        return Tunable.COWBOY_DUEL_MUSIC_VOLUME.intValue() / 100.0F;
    }

    @Nullable
    public static BlockPos cowboyArenaA() {
        return cowboyArenaPos(
            Tunable.COWBOY_ARENA_A_X,
            Tunable.COWBOY_ARENA_A_Y,
            Tunable.COWBOY_ARENA_A_Z
        );
    }

    @Nullable
    public static BlockPos cowboyArenaB() {
        return cowboyArenaPos(
            Tunable.COWBOY_ARENA_B_X,
            Tunable.COWBOY_ARENA_B_Y,
            Tunable.COWBOY_ARENA_B_Z
        );
    }

    @Nullable
    public static BlockPos cowboyArenaSpectator() {
        return cowboyArenaPos(
            Tunable.COWBOY_ARENA_SPECTATOR_X,
            Tunable.COWBOY_ARENA_SPECTATOR_Y,
            Tunable.COWBOY_ARENA_SPECTATOR_Z
        );
    }

    @Nullable
    private static BlockPos cowboyArenaPos(Tunable x, Tunable y, Tunable z) {
        double[] numbers = current.numbers;
        int posX = (int) numbers[x.ordinal()];
        int posY = (int) numbers[y.ordinal()];
        int posZ = (int) numbers[z.ordinal()];
        if (posX == 0 && posY == 0 && posZ == 0) return null;
        return new BlockPos(posX, posY, posZ);
    }

    public static Integer initialBalance(String roleId) {
        Tunable tunable = Tunable.role(roleId, "initial_balance");
        return tunable == null ? null : tunable.intValue();
    }

    public static boolean revolverShopEnabled(String roleId) {
        return !"terrorist".equals(roleId) && !skilledCivilian(roleId);
    }

    public static int shopPrice(String roleId, ShopItem item) {
        Tunable tunable = Tunable.role(roleId, "shop_prices." + item.name().toLowerCase(Locale.ROOT));
        if (tunable != null) return tunable.intValue();
        return switch (item) {
            case KNIFE -> "sniper".equals(roleId) ? 150 : 100;
            case REVOLVER -> fixedRevolverPrice(roleId);
            case PSYCHO_MODE -> 300;
        };
    }

    private static int fixedRevolverPrice(String roleId) {
        if ("medium".equals(roleId)) return 500;
        return "civilian".equals(roleId) || skilledCivilian(roleId) ? 350 : 300;
    }

    private static boolean skilledCivilian(@Nullable String roleId) {
        if (roleId == null) return false;
        return switch (roleId) {
            case "archivist", "avenger", "conspirator", "cowboy", "inspector", "medium", "stalker", "stunt_double",
                "valentina", "vigilante", "watchman", "zhuimu" -> true;
            default -> false;
        };
    }

    static double number(Tunable tunable) {
        return current.numbers[tunable.ordinal()];
    }

    static String text(Tunable tunable) {
        return current.texts[tunable.ordinal()];
    }

    private static Data parse(String json) {
        JsonElement rootElement = parseJson5(json);
        if (!rootElement.isJsonObject()) {
            throw new IllegalArgumentException("Config root must be an object");
        }
        JsonObject root = rootElement.getAsJsonObject();
        int sourceVersion = nonNegativeInt(root, "config_version", 0, "config");
        List<String> announcement = stringList(root, "announcement");
        Data defaults = createDefaults();
        JsonObject afkKickObject = objectOrEmpty(root, "afk_kick");
        boolean afkEnabled = booleanValue(afkKickObject, "enabled", defaults.afk_kick.enabled, "afk_kick");
        if (sourceVersion < AFK_DEFAULT_ON_MIGRATION_VERSION) afkEnabled = defaults.afk_kick.enabled;
        AfkKickSettings afkKick = new AfkKickSettings(
            afkEnabled,
            nonNegativeInt(afkKickObject, "idle_seconds", defaults.afk_kick.idle_seconds, "afk_kick"),
            nonNegativeInt(afkKickObject, "countdown_seconds", defaults.afk_kick.countdown_seconds, "afk_kick")
        );
        HarpyAssignmentSettings harpy = parseHarpy(root, defaults.harpy);
        double[] numbers = Tunable.numbers();
        String[] texts = Tunable.texts();
        for (Tunable tunable : Tunable.values()) {
            JsonObject section = section(root, tunable);
            if (!section.has(tunable.key())) continue;
            JsonElement element = section.get(tunable.key());
            if (tunable.textual()) {
                texts[tunable.ordinal()] = tunable.readText(element);
            } else {
                numbers[tunable.ordinal()] = tunable.read(element);
            }
        }
        for (Tunable balance : STARTS_BROKE) {
            if (numbers[balance.ordinal()] == 100.0D) numbers[balance.ordinal()] = 0.0D;
        }
        for (Migration migration : MIGRATIONS) {
            int index = migration.tunable().ordinal();
            if (sourceVersion < migration.before() && numbers[index] == migration.previous()) {
                numbers[index] = migration.tunable().fallback();
            }
        }
        return new Data(CONFIG_VERSION, announcement, afkKick, harpy, numbers, texts);
    }

    private static JsonObject section(JsonObject root, Tunable tunable) {
        JsonObject section = root;
        for (String parent : tunable.parents()) {
            boolean renamed = tunable.scoped() && "beast_trapper".equals(parent) && !section.has(parent);
            section = objectOrEmpty(section, renamed ? "trapper" : parent);
        }
        return section;
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
    private static boolean booleanValue(JsonObject object, String key, boolean fallback, String scope) {
        if (!object.has(key)) return fallback;
        JsonElement element = object.get(key);
        if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isBoolean()) {
            throw new IllegalArgumentException(scope + "." + key + " must be true or false");
        }
        return element.getAsBoolean();
    }
    private static Data createDefaults() {
        return new Data(
            CONFIG_VERSION,
            List.of(),
            new AfkKickSettings(true, 60, 60),
            HarpyAssignmentSettings.defaults(),
            Tunable.numbers(),
            Tunable.texts()
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
        for (Tunable tunable : Tunable.values()) {
            JsonObject section = root;
            for (String parent : tunable.parents()) {
                if (!section.has(parent)) section.add(parent, new JsonObject());
                section = section.getAsJsonObject(parent);
            }
            int index = tunable.ordinal();
            if (tunable.textual()) {
                section.addProperty(tunable.key(), data.texts[index]);
            } else if (tunable.decimal()) {
                section.addProperty(tunable.key(), data.numbers[index]);
            } else {
                section.addProperty(tunable.key(), (int) Math.round(data.numbers[index]));
            }
        }
        return GSON.toJson(root) + System.lineSeparator();
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
        public final HarpyAssignmentSettings harpy;
        private final double[] numbers;
        private final String[] texts;

        private Data(
            int configVersion,
            List<String> announcement,
            AfkKickSettings afkKick,
            HarpyAssignmentSettings harpy,
            double[] numbers,
            String[] texts
        ) {
            this.config_version = configVersion;
            this.announcement = announcement;
            this.afk_kick = afkKick;
            this.harpy = harpy;
            this.numbers = numbers;
            this.texts = texts;
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
    private record Migration(Tunable tunable, int before, int previous) {
    }
}
