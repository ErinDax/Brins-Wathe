package cn.erindax.brinswathe.admin;

import cn.erindax.brinswathe.BrinIcFlags;
import cn.erindax.brinswathe.component.StaminaComponent;
import cn.erindax.brinswathe.config.BrinConfig;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

public enum BrinAdminSetting {
    INSTINCT_NIGHT_VISION(Page.RULES, "instinct", Kind.BOOL, "brinswathe nv",
        () -> BrinIcFlags.instinctNightVision),
    INSTINCT_HUD_NAMES_THROUGH_WALLS(Page.RULES, "instinct", Kind.BOOL, "brinswathe hudnames",
        () -> BrinIcFlags.instinctHudNamesThroughWalls),
    ALLOW_KILLME(Page.RULES, "round", Kind.BOOL, "brinswathe km",
        () -> BrinIcFlags.allowKillme),
    PLAN_B(Page.RULES, "round", Kind.BOOL, "brinswathe planb",
        () -> BrinIcFlags.planB),
    BAD_GUESSER(Page.RULES, "round", Kind.BOOL, "brinswathe badguesser",
        () -> BrinIcFlags.badGuesser),
    PUNISH_EARLY_FALL(Page.RULES, "round", Kind.BOOL, "brinswathe error",
        () -> BrinIcFlags.punishEarlyFall),
    AFK_KICK(Page.RULES, "round", Kind.ON_OFF, "brinswathe afk",
        BrinConfig::afkKickEnabled),
    SIZE2SPEED(Page.RULES, "round", Kind.BOOL, "brinswathe setconfig size2speed",
        () -> BrinIcFlags.size2speed),
    DRAFT(Page.RULES, "selection", Kind.BOOL, "brinswathe draft",
        () -> BrinIcFlags.draft),
    ROLE_WEIGHTS(Page.RULES, "selection", Kind.BOOL, "brinswathe weights",
        () -> BrinIcFlags.roleWeights),
    ROLE_REPEAT_GUARD(Page.RULES, "selection", Kind.BOOL, "brinswathe setconfig roleRepeatGuard",
        () -> BrinIcFlags.roleRepeatGuard),
    MUSIC_BOX_ENABLED(Page.RULES, "music_box", Kind.BOOL, "brinswathe setconfig musicBoxEnabled",
        () -> BrinIcFlags.musicBoxEnabled),
    DRAFT_BOOST(Page.NUMBERS, "selection", Kind.FLOAT, BrinIcFlags.DRAFT_MIN_BOOST, BrinIcFlags.DRAFT_MAX_BOOST,
        "brinswathe draftboost", () -> BrinIcFlags.draftBoost),
    ROLE_REPEAT_STRENGTH(Page.NUMBERS, "selection", Kind.FLOAT, 0.0F, BrinIcFlags.ROLE_REPEAT_MAX_STRENGTH,
        "brinswathe setconfig roleRepeatStrength", () -> BrinIcFlags.roleRepeatStrength),
    ROLE_REPEAT_DECAY(Page.NUMBERS, "selection", Kind.FLOAT, 0.0F, BrinIcFlags.ROLE_REPEAT_MAX_DECAY,
        "brinswathe setconfig roleRepeatDecay", () -> BrinIcFlags.roleRepeatDecay),
    ROLE_REPEAT_MAX_STREAK(Page.NUMBERS, "selection", Kind.INT, 0, BrinIcFlags.ROLE_REPEAT_MAX_STREAK_LIMIT,
        "brinswathe setconfig roleRepeatMaxStreak", () -> BrinIcFlags.roleRepeatMaxStreak),
    NEUTRAL_ROLE_COUNT(Page.NUMBERS, "role_count", Kind.INT, -99, 99,
        "brinswathe setRoleCount neutral", BrinConfig::harpyNeutralRoleCount),
    KILLER_ROLE_COUNT(Page.NUMBERS, "role_count", Kind.INT, -99, 99,
        "brinswathe setRoleCount killer", BrinConfig::harpyKillerRoleCount),
    VIGILANTE_ROLE_COUNT(Page.NUMBERS, "role_count", Kind.INT, -99, 99,
        "brinswathe setRoleCount vigilante", BrinConfig::harpyVigilanteRoleCount),
    RESET_ITEMS_COOLDOWN_SECONDS(Page.NUMBERS, "items", Kind.INT, 0, 300,
        "brinswathe setcd", () -> BrinIcFlags.resetItemsCooldownSeconds),
    RESET_ITEMS_LIST(Page.NUMBERS, "items", Kind.ITEMS, "brinswathe setconfig resetItemsList",
        () -> List.copyOf(BrinIcFlags.resetItemsList)),
    PSYCHO_MIN_PLAYERS_FOR_EXTRA_ARMOUR(Page.NUMBERS, "psycho", Kind.INT, 1, 100,
        "brinswathe setconfig psychoMinPlayersForExtraArmour", () -> BrinIcFlags.psychoMinPlayersForExtraArmour),
    PSYCHO_PLAYERS_PER_EXTRA_ARMOUR(Page.NUMBERS, "psycho", Kind.INT, 1, 100,
        "brinswathe setconfig psychoPlayersPerExtraArmour", () -> BrinIcFlags.psychoPlayersPerExtraArmour),
    MAX_STAMINA(Page.NUMBERS, "stamina", Kind.INT, 1, 1000,
        "brinswathe setbrinspeed maxStamina", StaminaComponent::globalMaxStamina),
    RUN_SPEED(Page.NUMBERS, "stamina", Kind.FLOAT, 0.0F, 10.0F,
        "brinswathe setbrinspeed runSpeed", StaminaComponent::globalRunSpeed),
    REGEN_RATE(Page.NUMBERS, "stamina", Kind.INT, 0, 100,
        "brinswathe setbrinspeed regenRate", StaminaComponent::globalRegenRate),
    MUSIC_BOX_MAX_KILOBYTES(Page.NUMBERS, "music_box", Kind.INT,
        BrinIcFlags.MUSIC_BOX_MIN_KILOBYTES, BrinIcFlags.MUSIC_BOX_MAX_KILOBYTES,
        "brinswathe setconfig musicBoxMaxKilobytes", () -> BrinIcFlags.musicBoxMaxKilobytes);

    private static final int MAX_ITEMS = 64;
    private final Page page;
    private final String section;
    private final Kind kind;
    private final float min;
    private final float max;
    private final String command;
    private final Supplier<Object> reader;

    BrinAdminSetting(Page page, String section, Kind kind, String command, Supplier<Object> reader) {
        this(page, section, kind, 0.0F, 0.0F, command, reader);
    }

    BrinAdminSetting(Page page, String section, Kind kind, float min, float max, String command, Supplier<Object> reader) {
        this.page = page;
        this.section = section;
        this.kind = kind;
        this.min = min;
        this.max = max;
        this.command = command;
        this.reader = reader;
    }

    public String id() {
        return this.name().toLowerCase(Locale.ROOT);
    }

    public Page page() {
        return this.page;
    }

    public String section() {
        return this.section;
    }

    public Kind kind() {
        return this.kind;
    }

    public float min() {
        return this.min;
    }

    public float max() {
        return this.max;
    }

    public String usage() {
        return "/" + this.command + " " + switch (this.kind) {
            case BOOL -> "<true|false>";
            case ON_OFF -> "<on|off>";
            case INT, FLOAT -> "<" + formatNumber(this.min) + "~" + formatNumber(this.max) + ">";
            case ITEMS -> "<item_id, ...>";
        };
    }

    public String read() {
        Object value = this.reader.get();
        if (value instanceof Float number) return formatNumber(number);
        if (value instanceof List<?> list) return String.join(", ", list.stream().map(String::valueOf).toList());
        return String.valueOf(value);
    }

    @Nullable
    public String normalize(String raw) {
        String value = raw.trim();
        return switch (this.kind) {
            case BOOL, ON_OFF -> "true".equals(value) || "false".equals(value) ? value : null;
            case INT -> this.normalizeInt(value);
            case FLOAT -> this.normalizeFloat(value);
            case ITEMS -> normalizeItems(value);
        };
    }

    public String command(String value) {
        return switch (this.kind) {
            case ON_OFF -> this.command + ("true".equals(value) ? " on" : " off");
            case ITEMS -> this.command + " " + (value.isEmpty() ? "[]" : value);
            default -> this.command + " " + value;
        };
    }

    @Nullable
    public static BrinAdminSetting byId(String id) {
        for (BrinAdminSetting setting : values()) {
            if (setting.id().equals(id)) return setting;
        }
        return null;
    }

    public static String formatNumber(float value) {
        String text = Float.toString(value);
        return text.endsWith(".0") ? text.substring(0, text.length() - 2) : text;
    }

    public static boolean isItem(String raw) {
        ResourceLocation id = ResourceLocation.tryParse(raw.trim());
        return id != null && BuiltInRegistries.ITEM.containsKey(id) && BuiltInRegistries.ITEM.get(id) != Items.AIR;
    }

    @Nullable
    private String normalizeInt(String value) {
        try {
            int number = Integer.parseInt(value);
            return number < this.min || number > this.max ? null : Integer.toString(number);
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    @Nullable
    private String normalizeFloat(String value) {
        try {
            float number = Float.parseFloat(value);
            if (!Float.isFinite(number) || number < this.min || number > this.max) return null;
            return formatNumber(number == 0.0F ? 0.0F : number);
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    @Nullable
    private static String normalizeItems(String value) {
        String trimmed = value.startsWith("[") && value.endsWith("]") ? value.substring(1, value.length() - 1) : value;
        List<String> ids = new ArrayList<>();
        for (String part : trimmed.split(",")) {
            String token = part.trim().replace("\"", "");
            if (token.isEmpty()) continue;
            if (!isItem(token)) return null;
            String id = ResourceLocation.parse(token).toString();
            if (!ids.contains(id)) ids.add(id);
        }
        return ids.size() > MAX_ITEMS ? null : String.join(", ", ids);
    }

    public enum Page {
        RULES,
        NUMBERS;

        public String id() {
            return this.name().toLowerCase(Locale.ROOT);
        }
    }

    public enum Kind {
        BOOL,
        ON_OFF,
        INT,
        FLOAT,
        ITEMS;

        public boolean isToggle() {
            return this == BOOL || this == ON_OFF;
        }
    }
}
