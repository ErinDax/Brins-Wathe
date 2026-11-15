package cn.erindax.brinswathe.config;

import com.google.gson.JsonElement;
import java.util.HashMap;
import java.util.Map;
import org.jetbrains.annotations.Nullable;

public enum Tunable {
    LIVING_CHAT_RANGE("general.living_chat_range", 12, 1),
    LOW_MOOD_DEATH_SECONDS("general.low_mood_death_seconds", 60, 1),
    FUNERAL_SLOWDOWN_PERCENT("general.funeral_slowdown_percent", 50, 0, 90),
    PSYCHO_ARMOUR_RANGE("general.psycho_armour_range", 24, 1),

    TASK_REWARD_MIN("rewards.task_min", 50, 1),
    TASK_REWARD_MAX("rewards.task_max", 80, 1),
    KILLER_TEAM_TASK_REWARD("rewards.killer_team_task", 5),
    TASKMASTER_TASK_REWARD("rewards.taskmaster_task", 50),
    KILL_KILLER_REWARD("rewards.kill_killer", 50),
    SHIELD_BREAK_REWARD("rewards.shield_break", 50),
    BAD_GUESSER_TEAM_REWARD("rewards.bad_guesser_team", 50),

    EARLY_FALL_SECONDS("punishment.early_fall_seconds", 45, 1),
    SWAP_GRACE_SECONDS("punishment.swap_grace_seconds", 10),

    DRAFT_TURN_SECONDS("draft.turn_seconds", 15, 1),
    DRAFT_OFFER_SIZE("draft.offer_size", 3, 1, 5),

    RPS_RANGE("rps.range", 12, 1, 64),
    RPS_INVITE_SECONDS("rps.invite_seconds", 15, 1),
    RPS_PICK_SECONDS("rps.pick_seconds", 20, 1),
    RPS_RESULT_SECONDS("rps.result_seconds", 5, 1),

    ARCHIVIST_SKILL_COST("roles.archivist.skill_cost", Kind.COUNT, 125),
    ARCHIVIST_SKILL_COOLDOWN_SECONDS("roles.archivist.skill_cooldown_seconds", Kind.COUNT, 30),
    ARCHIVIST_WITNESS_GLOW_SECONDS("roles.archivist.witness_glow_seconds", 10),

    AVENGER_SKILL_DURATION_SECONDS("roles.avenger.skill_duration_seconds", Kind.COUNT, 20),
    AVENGER_INSTINCT_SECONDS("roles.avenger.instinct_seconds", Kind.COUNT, 6),

    BERSERKER_LOCKPICK_PRICE("roles.berserker.lockpick_price", 50),
    BERSERKER_CROWBAR_PRICE("roles.berserker.crowbar_price", 25),
    BERSERKER_TASK_REWARD("roles.berserker.task_reward", 50, 1),
    BERSERKER_INITIAL_BALANCE("roles.berserker.initial_balance", Kind.COUNT, 0),
    BERSERKER_SHOP_PSYCHO_MODE("roles.berserker.shop_prices.psycho_mode", Kind.COUNT, 800),

    BOMBER_GRENADE_PRICE("roles.bomber.grenade_price", Kind.COUNT, 200),
    BOMBER_BOMB_PRICE("roles.bomber.bomb_price", Kind.COUNT, 75),
    BOMBER_MINE_PRICE("roles.bomber.mine_price", Kind.COUNT, 125),
    BOMBER_BOMB_FUSE_SECONDS("roles.bomber.bomb_fuse_seconds", Kind.COUNT, 45),
    BOMBER_BOMB_WARNING_SECONDS("roles.bomber.bomb_warning_seconds", Kind.COUNT, 30),
    BOMBER_BOMB_EXPLOSION_SIZE("roles.bomber.bomb_explosion_size", Kind.COUNT, 2),
    BOMBER_MINE_STUN_SECONDS("roles.bomber.mine_stun_seconds", Kind.COUNT, 3),
    BOMBER_BOMB_PURCHASE_COOLDOWN_SECONDS("roles.bomber.bomb_purchase_cooldown_seconds", Kind.COUNT, 60),

    BONEHARVESTER_BONE_KNIFE_PRICE("roles.boneharvester.bone_knife_price", Kind.COUNT, 175),
    BONEHARVESTER_BONE_KNIFE_COOLDOWN_SECONDS("roles.boneharvester.bone_knife_cooldown_seconds", Kind.COUNT, 200),
    BONEHARVESTER_CORPSE_DELAY_SECONDS("roles.boneharvester.corpse_delay_seconds", 20),
    BONEHARVESTER_SHOP_KNIFE("roles.boneharvester.shop_prices.knife", Kind.COUNT, 100),
    BONEHARVESTER_SHOP_PSYCHO_MODE("roles.boneharvester.shop_prices.psycho_mode", Kind.COUNT, 300),

    COMPENSATOR_SWITCH_REWARD("roles.compensator.switch_reward", 150),
    COMPENSATOR_INITIAL_BALANCE("roles.compensator.initial_balance", Kind.COUNT, 0),

    CONDUCTOR_ARMOR_COST("roles.conductor.armor_cost", 200),
    CONDUCTOR_ARMOR_COOLDOWN_SECONDS("roles.conductor.armor_cooldown_seconds", 100),
    CONDUCTOR_MAX_ARMOR_LAYERS("roles.conductor.max_armor_layers", 3),

    COWBOY_SKILL_COST("roles.cowboy.skill_cost", Kind.COUNT, 500),
    COWBOY_DUEL_TIMEOUT_SECONDS("roles.cowboy.duel_timeout_seconds", Kind.COUNT, 90),
    COWBOY_DUEL_COUNTDOWN_SECONDS("roles.cowboy.duel_countdown_seconds", Kind.COUNT, 3),
    COWBOY_DUEL_MUSIC_VOLUME("roles.cowboy.duel_music_volume", Kind.PERCENT, 100),
    COWBOY_DUEL_ANNOUNCE_MESSAGE("roles.cowboy.duel_announce_message", "牛仔发起了决斗：{cowboy} 对决 {target}！"),
    COWBOY_ARENA_A_X("roles.cowboy.arena_a_x", Kind.SIGNED, 0),
    COWBOY_ARENA_A_Y("roles.cowboy.arena_a_y", Kind.SIGNED, 0),
    COWBOY_ARENA_A_Z("roles.cowboy.arena_a_z", Kind.SIGNED, 0),
    COWBOY_ARENA_B_X("roles.cowboy.arena_b_x", Kind.SIGNED, 0),
    COWBOY_ARENA_B_Y("roles.cowboy.arena_b_y", Kind.SIGNED, 0),
    COWBOY_ARENA_B_Z("roles.cowboy.arena_b_z", Kind.SIGNED, 0),
    COWBOY_ARENA_SPECTATOR_X("roles.cowboy.arena_spectator_x", Kind.SIGNED, 0),
    COWBOY_ARENA_SPECTATOR_Y("roles.cowboy.arena_spectator_y", Kind.SIGNED, 0),
    COWBOY_ARENA_SPECTATOR_Z("roles.cowboy.arena_spectator_z", Kind.SIGNED, 0),

    CONSPIRATOR_SKILL_COST("roles.conspirator.skill_cost", Kind.COUNT, 150),
    CONSPIRATOR_SKILL_COOLDOWN_SECONDS("roles.conspirator.skill_cooldown_seconds", Kind.COUNT, 200),

    EXECUTIONER_CONVERSION_BALANCE("roles.executioner.conversion_balance", 200),
    EXECUTIONER_REVIVE_INVISIBILITY_SECONDS("roles.executioner.revive_invisibility_seconds", 30),

    GAMBLER_SKILL_COST("roles.gambler.skill_cost", Kind.COUNT, 100),
    GAMBLER_WIN_BONUS("roles.gambler.win_bonus", 200),
    GAMBLER_LOSS_PENALTY("roles.gambler.loss_penalty", 75),
    GAMBLER_LOCKPICK_PRICE("roles.gambler.lockpick_price", 75),
    GAMBLER_TASK_REWARD("roles.gambler.task_reward", 50, 1),
    GAMBLER_INITIAL_BALANCE("roles.gambler.initial_balance", Kind.COUNT, 0),

    GRAPPLER_SKILL_COOLDOWN_SECONDS("roles.grappler.skill_cooldown_seconds", Kind.COUNT, 90),
    GRAPPLER_SKILL_DURATION_SECONDS("roles.grappler.skill_duration_seconds", Kind.COUNT, 2),
    GRAPPLER_HOOK_RANGE("roles.grappler.hook_range", 16, 1, 32),
    GRAPPLER_SHOP_KNIFE("roles.grappler.shop_prices.knife", Kind.COUNT, 100),
    GRAPPLER_SHOP_PSYCHO_MODE("roles.grappler.shop_prices.psycho_mode", Kind.COUNT, 300),

    HALF_SPIRIT_SKILL_COOLDOWN_SECONDS("roles.half_spirit.skill_cooldown_seconds", Kind.COUNT, 120),
    HALF_SPIRIT_SHOP_KNIFE("roles.half_spirit.shop_prices.knife", Kind.COUNT, 100),
    HALF_SPIRIT_SHOP_PSYCHO_MODE("roles.half_spirit.shop_prices.psycho_mode", Kind.COUNT, 300),

    ILLUSIONIST_SKILL_COOLDOWN_SECONDS("roles.illusionist.skill_cooldown_seconds", Kind.COUNT, 120),
    ILLUSIONIST_SKILL_DURATION_SECONDS("roles.illusionist.skill_duration_seconds", Kind.COUNT, 10),
    ILLUSIONIST_CLONE_KILL_BLIND_SECONDS("roles.illusionist.clone_kill_blind_seconds", 5),
    ILLUSIONIST_SHOP_KNIFE("roles.illusionist.shop_prices.knife", Kind.COUNT, 100),
    ILLUSIONIST_SHOP_PSYCHO_MODE("roles.illusionist.shop_prices.psycho_mode", Kind.COUNT, 300),

    INSANE_KILLER_REVIVE_COOLDOWN_SECONDS("roles.insane_killer.revive_cooldown_seconds", 120),
    INSANE_KILLER_REVIVE_GLOW_SECONDS("roles.insane_killer.revive_glow_seconds", 15),
    INSANE_KILLER_REVIVED_LIFE_SECONDS("roles.insane_killer.revived_life_seconds", 30, 1),

    INSPECTOR_SKILL_COST("roles.inspector.skill_cost", Kind.COUNT, 200),
    INSPECTOR_SKILL_DURATION_SECONDS("roles.inspector.skill_duration_seconds", Kind.COUNT, 10),

    MEDIUM_SKILL_COST("roles.medium.skill_cost", Kind.COUNT, 200),
    MEDIUM_SKILL_COOLDOWN_SECONDS("roles.medium.skill_cooldown_seconds", Kind.COUNT, 120),
    MEDIUM_SKILL_DURATION_SECONDS("roles.medium.skill_duration_seconds", Kind.COUNT, 8),

    MIMIC_TASK_REWARD("roles.mimic.task_reward", 25, 1),
    MIMIC_LOCKPICK_PRICE("roles.mimic.lockpick_price", 50),
    MIMIC_DELUSION_VIAL_PRICE("roles.mimic.delusion_vial_price", 30),
    MIMIC_FIRECRACKER_PRICE("roles.mimic.firecracker_price", 5),
    MIMIC_NOTE_PRICE("roles.mimic.note_price", 5),
    MIMIC_REVOLVER_PRICE("roles.mimic.revolver_price", 300),
    MIMIC_MICEYES_PRICE("roles.mimic.miceyes_price", 350),

    MORPHLING_MORPH_SECONDS("roles.morphling.morph_seconds", 120, 1),

    MORTICIAN_SKILL_COOLDOWN_SECONDS("roles.mortician.skill_cooldown_seconds", Kind.COUNT, 100),
    MORTICIAN_SKILL_DURATION_SECONDS("roles.mortician.skill_duration_seconds", Kind.COUNT, 5),
    MORTICIAN_SHOP_KNIFE("roles.mortician.shop_prices.knife", Kind.COUNT, 100),
    MORTICIAN_SHOP_PSYCHO_MODE("roles.mortician.shop_prices.psycho_mode", Kind.COUNT, 300),

    MUZZLER_SILENCE_COST("roles.muzzler.silence_cost", 100),
    MUZZLER_SILENCE_COOLDOWN_SECONDS("roles.muzzler.silence_cooldown_seconds", 60),
    MUZZLER_SILENCE_DURATION_SECONDS("roles.muzzler.silence_duration_seconds", 30, 1),
    MUZZLER_SILENCE_KILL_REWARD("roles.muzzler.silence_kill_reward", 50),

    NECROMANCER_REVIVE_GLOW_SECONDS("roles.necromancer.revive_glow_seconds", 15),
    NECROMANCER_REFRESH_PRICE("roles.necromancer.refresh_price", 350),
    NECROMANCER_KNIFE_PRICE("roles.necromancer.knife_price", 200),
    NECROMANCER_GRENADE_PRICE("roles.necromancer.grenade_price", 350),
    NECROMANCER_FIRECRACKER_PRICE("roles.necromancer.firecracker_price", 10),
    NECROMANCER_LOCKPICK_PRICE("roles.necromancer.lockpick_price", 50),
    NECROMANCER_CROWBAR_PRICE("roles.necromancer.crowbar_price", 25),
    NECROMANCER_BODY_BAG_PRICE("roles.necromancer.body_bag_price", 200),
    NECROMANCER_NOTE_PRICE("roles.necromancer.note_price", 10),

    NIGHTMARE_FORCED_SLEEP_TASK_COOLDOWN_SECONDS("roles.nightmare.forced_sleep_task_cooldown_seconds", Kind.COUNT, 200),
    NIGHTMARE_SKILL_COOLDOWN_SECONDS("roles.nightmare.skill_cooldown_seconds", Kind.COUNT, 60),
    NIGHTMARE_MAX_SHIELD_LAYERS("roles.nightmare.max_shield_layers", 2),
    NIGHTMARE_INITIAL_BALANCE("roles.nightmare.initial_balance", Kind.COUNT, 0),

    NOISEMAKER_MARK_COOLDOWN_SECONDS("roles.noisemaker.mark_cooldown_seconds", 30),
    NOISEMAKER_FIRECRACKER_PRICE("roles.noisemaker.firecracker_price", 75),
    NOISEMAKER_SHOUT_PRICE("roles.noisemaker.shout_price", 100),
    NOISEMAKER_SHOUT_COOLDOWN_SECONDS("roles.noisemaker.shout_cooldown_seconds", 40),
    NOISEMAKER_SHOUT_CREDIT_COOLDOWN_SECONDS("roles.noisemaker.shout_credit_cooldown_seconds", 120),
    NOISEMAKER_SHOUT_GLOW_SECONDS("roles.noisemaker.shout_glow_seconds", 30),

    PENITENT_IDENTITY_HINT_PRICE("roles.penitent.identity_hint_price", Kind.COUNT, 150),
    PENITENT_ANTIDOTE_PRICE("roles.penitent.antidote_price", Kind.COUNT, 100),
    PENITENT_STARTING_SHIELD_LAYERS("roles.penitent.starting_shield_layers", Kind.COUNT, 0),
    PENITENT_TARGET_KILLS("roles.penitent.target_kills", 2, 1),
    PENITENT_LARGE_GAME_TARGET_KILLS("roles.penitent.large_game_target_kills", 3, 1),
    PENITENT_LARGE_GAME_MIN_PLAYERS("roles.penitent.large_game_min_players", 9, 1),
    PENITENT_TASK_REWARD("roles.penitent.task_reward", 50, 1),
    PENITENT_INITIAL_BALANCE("roles.penitent.initial_balance", Kind.COUNT, 50),
    PENITENT_SHOP_KNIFE("roles.penitent.shop_prices.knife", Kind.COUNT, 100),

    PUPPETEER_SKILL_COOLDOWN_SECONDS("roles.puppeteer.skill_cooldown_seconds", Kind.COUNT, 200),
    PUPPETEER_SKILL_DURATION_SECONDS("roles.puppeteer.skill_duration_seconds", Kind.COUNT, 60),
    PUPPETEER_RETURN_COOLDOWN_SECONDS("roles.puppeteer.return_cooldown_seconds", Kind.COUNT, 60),
    PUPPETEER_SELF_DESTRUCT_PRICE("roles.puppeteer.self_destruct_price", Kind.COUNT, 350),
    PUPPETEER_SELF_DESTRUCT_RANGE("roles.puppeteer.self_destruct_range", Kind.COUNT, 2),
    PUPPETEER_PARTICLE_DELAY_SECONDS("roles.puppeteer.particle_delay_seconds", Kind.COUNT_OR_OFF, 45),
    PUPPETEER_EXPOSED_GLOW_SECONDS("roles.puppeteer.exposed_glow_seconds", 10),
    PUPPETEER_KILLED_SLOWNESS_SECONDS("roles.puppeteer.killed_slowness_seconds", 15),
    PUPPETEER_SHOP_KNIFE("roles.puppeteer.shop_prices.knife", Kind.COUNT, 150),

    SNIPER_SKILL_COOLDOWN_SECONDS("roles.sniper.skill_cooldown_seconds", Kind.COUNT, 100000),
    SNIPER_KILLS_TO_RESET("roles.sniper.kills_to_reset", Kind.POSITIVE_OR_OFF, -1),
    SNIPER_TASKS_TO_RESET("roles.sniper.tasks_to_reset", Kind.COUNT, 20),
    SNIPER_GLOW_SECONDS("roles.sniper.glow_seconds", Kind.COUNT, 10),
    SNIPER_MAX_RANGE("roles.sniper.max_range", 300, 1, 1024),
    SNIPER_OPENING_TASKS("roles.sniper.opening_tasks", 2, 1),
    SNIPER_SHOP_REVOLVER("roles.sniper.shop_prices.revolver", Kind.COUNT, 225),

    SPY_CORRECT_COOLDOWN_SECONDS("roles.spy.correct_cooldown_seconds", 25),
    SPY_WRONG_COOLDOWN_SECONDS("roles.spy.wrong_cooldown_seconds", 120),
    SPY_MAX_WRONG_GUESSES("roles.spy.max_wrong_guesses", 2, 1),
    SPY_REQUIRED_DIVISOR("roles.spy.required_divisor", 2, 1),
    SPY_REQUIRED_REDUCTION("roles.spy.required_reduction", 1),

    STALKER_SKILL_COOLDOWN_SECONDS("roles.stalker.skill_cooldown_seconds", Kind.COUNT, 90),
    STALKER_AIM_RANGE("roles.stalker.aim_range", 8, 1, 128),

    STUNT_DOUBLE_SKILL_COOLDOWN_SECONDS("roles.stunt_double.skill_cooldown_seconds", Kind.COUNT, 30),
    STUNT_DOUBLE_SKILL_DURATION_SECONDS("roles.stunt_double.skill_duration_seconds", Kind.COUNT, 30),

    SUBVERTER_TIMEOUT_SECONDS("roles.subverter.subvert_timeout_seconds", 30, 1),
    SUBVERTER_GLOW_SECONDS("roles.subverter.subverted_glow_seconds", 3),
    SUBVERTER_REWARD("roles.subverter.subverted_reward", 100),

    TERRORIST_EXPLOSION_RANGE("roles.terrorist.explosion_range", Kind.COUNT, 5),
    TERRORIST_EXPLOSION_SURVIVAL_KILLS("roles.terrorist.explosion_survival_kills", Kind.COUNT, 4),
    TERRORIST_INITIAL_BALANCE("roles.terrorist.initial_balance", Kind.COUNT, 0),

    TRAPPER_SKILL_COST("roles.beast_trapper.skill_cost", Kind.COUNT, 25),
    TRAPPER_TRAP_LIMIT("roles.beast_trapper.trap_limit", Kind.COUNT, 1),
    TRAPPER_EXTRA_TRAP_PRICE("roles.beast_trapper.extra_trap_price", Kind.COUNT, 150),
    TRAPPER_TRAP_CATCH_REWARD("roles.beast_trapper.trap_catch_reward", Kind.COUNT, 50),
    TRAPPER_TRAP_LIFETIME_SECONDS("roles.beast_trapper.trap_lifetime_seconds", 210, 1),
    TRAPPER_SHOP_KNIFE("roles.beast_trapper.shop_prices.knife", Kind.COUNT, 100),

    VALENTINA_SKILL_DURATION_SECONDS("roles.valentina.skill_duration_seconds", Kind.COUNT, 10),
    VALENTINA_MAX_EYES("roles.valentina.max_eyes", 2, 1),
    VALENTINA_TASKS_PER_EYE("roles.valentina.tasks_per_eye", 4, 1),
    VALENTINA_FORESIGHT_COST("roles.valentina.foresight_cost", 2, 1),

    VIGILANTE_SKILL_COST("roles.vigilante.skill_cost", Kind.COUNT, 200),
    VIGILANTE_SKILL_COOLDOWN_SECONDS("roles.vigilante.skill_cooldown_seconds", Kind.COUNT, 90),
    VIGILANTE_DETECT_RANGE("roles.vigilante.detect_range", 2.5D, 0.5D, 64.0D),

    VOODOO_NIGHT_VISION_PRICE("roles.voodoo.night_vision_price", 300),
    VOODOO_NIGHT_VISION_SECONDS("roles.voodoo.night_vision_seconds", 60, 1),

    WATCHMAN_SKILL_COST("roles.watchman.skill_cost", Kind.COUNT, 150),
    WATCHMAN_SKILL_COOLDOWN_SECONDS("roles.watchman.skill_cooldown_seconds", Kind.COUNT, 30),
    WATCHMAN_SURVIVAL_SECONDS("roles.watchman.survival_seconds", 60, 1),

    ZHANGSHI_SKILL_COOLDOWN_SECONDS("roles.zhangshi.skill_cooldown_seconds", Kind.COUNT, 90),
    ZHANGSHI_SKILL_DURATION_SECONDS("roles.zhangshi.skill_duration_seconds", Kind.COUNT, 10),
    ZHANGSHI_XUEZI_PRICE("roles.zhangshi.xuezi_price", Kind.COUNT, 150),
    ZHANGSHI_XUEZI_COOLDOWN_SECONDS("roles.zhangshi.xuezi_cooldown_seconds", Kind.COUNT, 200),
    ZHANGSHI_XUEZI_SHIELD_LAYERS("roles.zhangshi.xuezi_shield_layers", Kind.COUNT, 1),
    ZHANGSHI_RUN_SPEED("roles.zhangshi.run_speed", Kind.SPEED, 2.0D),
    ZHANGSHI_XUEZI_END_DELAY_SECONDS("roles.zhangshi.xuezi_end_delay_seconds", 5, 1),
    ZHANGSHI_SHOP_PSYCHO_MODE("roles.zhangshi.shop_prices.psycho_mode", Kind.COUNT, 300),

    ZHUIMU_SKILL_COST("roles.zhuimu.skill_cost", Kind.COUNT, 350),
    ZHUIMU_STRUGGLE_PRESSES("roles.zhuimu.struggle_presses", Kind.COUNT, 20),
    ZHUIMU_AXE_HITS("roles.zhuimu.axe_hits", 3, 1),
    ZHUIMU_AXE_COOLDOWN_SECONDS("roles.zhuimu.axe_cooldown_seconds", 0.5D, 0.0D, 60.0D);

    private static final int LIMIT = 1_000_000;
    private static final String ROLE_PREFIX = "roles.";
    private static final Map<String, Tunable> BY_PATH = new HashMap<>();

    static {
        for (Tunable tunable : values()) BY_PATH.put(tunable.path, tunable);
    }

    private final String path;
    private final String[] parents;
    private final String key;
    private final Kind kind;
    private final double fallback;
    private final String fallbackText;
    private final double min;
    private final double max;

    Tunable(String path, int fallback) {
        this(path, fallback, 0, LIMIT);
    }

    Tunable(String path, int fallback, int min) {
        this(path, fallback, min, LIMIT);
    }

    Tunable(String path, int fallback, int min, int max) {
        this(path, Kind.RANGE, fallback, null, min, max);
    }

    Tunable(String path, double fallback, double min, double max) {
        this(path, Kind.DECIMAL, fallback, null, min, max);
    }

    Tunable(String path, Kind kind, double fallback) {
        this(path, kind, fallback, null, 0.0D, kind.max);
    }

    Tunable(String path, String fallback) {
        this(path, Kind.TEXT, 0.0D, fallback, 0.0D, 0.0D);
    }

    Tunable(String path, Kind kind, double fallback, @Nullable String fallbackText, double min, double max) {
        int split = path.lastIndexOf('.');
        this.path = path;
        this.parents = path.substring(0, split).split("\\.");
        this.key = path.substring(split + 1);
        this.kind = kind;
        this.fallback = fallback;
        this.fallbackText = fallbackText;
        this.min = min;
        this.max = max;
    }

    @Nullable
    public static Tunable find(@Nullable String roleId, String token) {
        if (roleId != null) {
            Tunable scoped = BY_PATH.get(ROLE_PREFIX + roleId + "." + token);
            if (scoped != null) return scoped;
        }
        return BY_PATH.get(token);
    }

    public int intValue() {
        return (int) Math.round(ModConfig.number(this));
    }

    public double doubleValue() {
        return ModConfig.number(this);
    }

    public int ticks() {
        return (int) Math.round(ModConfig.number(this) * 20.0D);
    }

    public String text() {
        return this.kind == Kind.TEXT ? ModConfig.text(this) : format(ModConfig.number(this));
    }

    @Nullable
    static Tunable role(@Nullable String roleId, String key) {
        return BY_PATH.get(ROLE_PREFIX + roleId + "." + key);
    }

    static double[] numbers() {
        Tunable[] tunables = values();
        double[] numbers = new double[tunables.length];
        for (Tunable tunable : tunables) numbers[tunable.ordinal()] = tunable.fallback;
        return numbers;
    }

    static String[] texts() {
        Tunable[] tunables = values();
        String[] texts = new String[tunables.length];
        for (Tunable tunable : tunables) texts[tunable.ordinal()] = tunable.fallbackText;
        return texts;
    }

    String[] parents() {
        return this.parents;
    }

    String key() {
        return this.key;
    }

    double fallback() {
        return this.fallback;
    }

    boolean textual() {
        return this.kind == Kind.TEXT;
    }

    boolean decimal() {
        return this.kind == Kind.DECIMAL || this.kind == Kind.SPEED;
    }

    boolean scoped() {
        return this.kind.requirement != null;
    }

    double read(JsonElement element) {
        if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isNumber()) throw invalid();
        if (this.kind.digits != null) return digits(element.getAsString());
        double value = element.getAsDouble();
        if (!Double.isFinite(value) || value < this.min || value > this.max) throw invalid();
        if (this.kind == Kind.RANGE && value != Math.rint(value)) throw invalid();
        return this.kind == Kind.PERCENT ? Math.round(value) : value;
    }

    String readText(JsonElement element) {
        if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isString()) throw invalid();
        return element.getAsString();
    }

    private double digits(String raw) {
        if (!raw.matches(this.kind.digits)) throw invalid();
        try {
            long value = Long.parseLong(raw);
            if (value > Integer.MAX_VALUE || value < Integer.MIN_VALUE) throw new NumberFormatException();
            return value;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(label() + " is too large");
        }
    }

    private IllegalArgumentException invalid() {
        return new IllegalArgumentException(label() + " must be " + requirement());
    }

    private String label() {
        return scoped() ? this.parents[1] + "." + this.key : this.path;
    }

    private String requirement() {
        if (this.kind.requirement != null) return this.kind.requirement;
        return (this.kind == Kind.DECIMAL ? "a number from " : "an integer from ")
            + format(this.min) + " to " + format(this.max);
    }

    private static String format(double value) {
        return value == Math.rint(value) ? Long.toString((long) value) : Double.toString(value);
    }

    private enum Kind {
        RANGE(null, null, 0.0D),
        DECIMAL(null, null, 0.0D),
        COUNT("\\d+", "a non-negative integer", 0.0D),
        SIGNED("-?\\d+", "an integer", 0.0D),
        COUNT_OR_OFF("-1|\\d+", "-1 or a non-negative integer", 0.0D),
        POSITIVE_OR_OFF("-1|[1-9]\\d*", "-1 or a positive integer", 0.0D),
        PERCENT(null, "0 to 100", 100.0D),
        SPEED(null, "a number from 0.0 to 10.0", 10.0D),
        TEXT(null, "a string", 0.0D);

        private final String digits;
        private final String requirement;
        private final double max;

        Kind(@Nullable String digits, @Nullable String requirement, double max) {
            this.digits = digits;
            this.requirement = requirement;
            this.max = max;
        }
    }
}
