package cn.erindax.brinswathe.admin;

import cn.erindax.brinswathe.IcFlags;
import cn.erindax.brinswathe.component.StaminaComponent;
import cn.erindax.brinswathe.config.ModConfig;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

public enum AdminSetting {
    INSTINCT_NIGHT_VISION(Page.RULES, "instinct", Kind.BOOL, "brinswathe nv",
        () -> IcFlags.instinctNightVision),
    INSTINCT_HUD_NAMES_THROUGH_WALLS(Page.RULES, "instinct", Kind.BOOL, "brinswathe hudnames",
        () -> IcFlags.instinctHudNamesThroughWalls),
    ALLOW_KILLME(Page.RULES, "round", Kind.BOOL, "brinswathe km",
        () -> IcFlags.allowKillme),
    PLAN_B(Page.RULES, "round", Kind.BOOL, "brinswathe planb",
        () -> IcFlags.planB),
    BAD_GUESSER(Page.RULES, "round", Kind.BOOL, "brinswathe badguesser",
        () -> IcFlags.badGuesser),
    PUNISH_EARLY_FALL(Page.RULES, "round", Kind.BOOL, "brinswathe error",
        () -> IcFlags.punishEarlyFall),
    AFK_KICK(Page.RULES, "round", Kind.ON_OFF, "brinswathe afk",
        ModConfig::afkKickEnabled),
    SIZE2SPEED(Page.RULES, "round", Kind.BOOL, "brinswathe setconfig size2speed",
        () -> IcFlags.size2speed),
    DRAFT(Page.RULES, "selection", Kind.BOOL, "brinswathe draft",
        () -> IcFlags.draft),
    SECRET_MURDER_CHANCE(Page.NUMBERS, "selection", Kind.FLOAT, 0.0F, 1.0F, "brinswathe secretmurder",
        () -> IcFlags.secretMurderChance),
    ROLE_WEIGHTS(Page.RULES, "selection", Kind.BOOL, "brinswathe weights",
        () -> IcFlags.roleWeights),
    ROLE_REPEAT_GUARD(Page.RULES, "selection", Kind.BOOL, "brinswathe setconfig roleRepeatGuard",
        () -> IcFlags.roleRepeatGuard),
    MUSIC_BOX_ENABLED(Page.RULES, "music_box", Kind.BOOL, "brinswathe setconfig musicBoxEnabled",
        () -> IcFlags.musicBoxEnabled),
    ROLE_REPEAT_STRENGTH(Page.NUMBERS, "selection", Kind.FLOAT, 0.0F, IcFlags.ROLE_REPEAT_MAX_STRENGTH,
        "brinswathe setconfig roleRepeatStrength", () -> IcFlags.roleRepeatStrength),
    ROLE_REPEAT_DECAY(Page.NUMBERS, "selection", Kind.FLOAT, 0.0F, IcFlags.ROLE_REPEAT_MAX_DECAY,
        "brinswathe setconfig roleRepeatDecay", () -> IcFlags.roleRepeatDecay),
    ROLE_REPEAT_MAX_STREAK(Page.NUMBERS, "selection", Kind.INT, 0, IcFlags.ROLE_REPEAT_MAX_STREAK_LIMIT,
        "brinswathe setconfig roleRepeatMaxStreak", () -> IcFlags.roleRepeatMaxStreak),
    NEUTRAL_ROLE_COUNT(Page.NUMBERS, "role_count", Kind.INT, -99, 99,
        "brinswathe setRoleCount neutral", ModConfig::harpyNeutralRoleCount),
    KILLER_ROLE_COUNT(Page.NUMBERS, "role_count", Kind.INT, -99, 99,
        "brinswathe setRoleCount killer", ModConfig::harpyKillerRoleCount),
    VIGILANTE_ROLE_COUNT(Page.NUMBERS, "role_count", Kind.INT, -99, 99,
        "brinswathe setRoleCount vigilante", ModConfig::harpyVigilanteRoleCount),
    RESET_ITEMS_COOLDOWN_SECONDS(Page.NUMBERS, "items", Kind.INT, 0, 300,
        "brinswathe setcd", () -> IcFlags.resetItemsCooldownSeconds),
    RESET_ITEMS_LIST(Page.NUMBERS, "items", Kind.ITEMS, "brinswathe setconfig resetItemsList",
        () -> List.copyOf(IcFlags.resetItemsList)),
    PSYCHO_MIN_PLAYERS_FOR_EXTRA_ARMOUR(Page.NUMBERS, "psycho", Kind.INT, 1, 100,
        "brinswathe setconfig psychoMinPlayersForExtraArmour", () -> IcFlags.psychoMinPlayersForExtraArmour),
    PSYCHO_PLAYERS_PER_EXTRA_ARMOUR(Page.NUMBERS, "psycho", Kind.INT, 1, 100,
        "brinswathe setconfig psychoPlayersPerExtraArmour", () -> IcFlags.psychoPlayersPerExtraArmour),
    MAX_STAMINA(Page.NUMBERS, "stamina", Kind.INT, 1, 1000,
        "brinswathe setbrinspeed maxStamina", StaminaComponent::globalMaxStamina),
    RUN_SPEED(Page.NUMBERS, "stamina", Kind.FLOAT, 0.0F, 10.0F,
        "brinswathe setbrinspeed runSpeed", StaminaComponent::globalRunSpeed),
    REGEN_RATE(Page.NUMBERS, "stamina", Kind.INT, 0, 100,
        "brinswathe setbrinspeed regenRate", StaminaComponent::globalRegenRate),
    MUSIC_BOX_MAX_KILOBYTES(Page.NUMBERS, "music_box", Kind.INT,
        IcFlags.MUSIC_BOX_MIN_KILOBYTES, IcFlags.MUSIC_BOX_MAX_KILOBYTES,
        "brinswathe setconfig musicBoxMaxKilobytes", () -> IcFlags.musicBoxMaxKilobytes);

    private static final int MAX_ITEMS = 64;
    private final Page page;
    private final String section;
    private final Kind kind;
    private final float min;
    private final float max;
    private final String command;
    private final Supplier<Object> reader;

    AdminSetting(Page page, String section, Kind kind, String command, Supplier<Object> reader) {
        this(page, section, kind, 0.0F, 0.0F, command, reader);
    }

    AdminSetting(Page page, String section, Kind kind, float min, float max, String command, Supplier<Object> reader) {
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
    public static AdminSetting byId(String id) {
        for (AdminSetting setting : values()) {
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
