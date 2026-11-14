package cn.erindax.brinswathe.config;

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

    ARCHIVIST_WITNESS_GLOW_SECONDS("roles.archivist.witness_glow_seconds", 10),

    BERSERKER_LOCKPICK_PRICE("roles.berserker.lockpick_price", 50),
    BERSERKER_CROWBAR_PRICE("roles.berserker.crowbar_price", 25),
    BERSERKER_TASK_REWARD("roles.berserker.task_reward", 50, 1),

    BONEHARVESTER_CORPSE_DELAY_SECONDS("roles.boneharvester.corpse_delay_seconds", 20),

    COMPENSATOR_SWITCH_REWARD("roles.compensator.switch_reward", 150),

    CONDUCTOR_ARMOR_COST("roles.conductor.armor_cost", 200),
    CONDUCTOR_ARMOR_COOLDOWN_SECONDS("roles.conductor.armor_cooldown_seconds", 100),
    CONDUCTOR_MAX_ARMOR_LAYERS("roles.conductor.max_armor_layers", 3),

    EXECUTIONER_CONVERSION_BALANCE("roles.executioner.conversion_balance", 200),
    EXECUTIONER_REVIVE_INVISIBILITY_SECONDS("roles.executioner.revive_invisibility_seconds", 30),

    GAMBLER_WIN_BONUS("roles.gambler.win_bonus", 200),
    GAMBLER_LOSS_PENALTY("roles.gambler.loss_penalty", 75),
    GAMBLER_LOCKPICK_PRICE("roles.gambler.lockpick_price", 75),
    GAMBLER_TASK_REWARD("roles.gambler.task_reward", 50, 1),

    GRAPPLER_HOOK_RANGE("roles.grappler.hook_range", 16, 1, 32),

    ILLUSIONIST_CLONE_KILL_BLIND_SECONDS("roles.illusionist.clone_kill_blind_seconds", 5),

    INSANE_KILLER_REVIVE_COOLDOWN_SECONDS("roles.insane_killer.revive_cooldown_seconds", 120),
    INSANE_KILLER_REVIVE_GLOW_SECONDS("roles.insane_killer.revive_glow_seconds", 15),
    INSANE_KILLER_REVIVED_LIFE_SECONDS("roles.insane_killer.revived_life_seconds", 30, 1),

    MIMIC_TASK_REWARD("roles.mimic.task_reward", 25, 1),
    MIMIC_LOCKPICK_PRICE("roles.mimic.lockpick_price", 50),
    MIMIC_DELUSION_VIAL_PRICE("roles.mimic.delusion_vial_price", 30),
    MIMIC_FIRECRACKER_PRICE("roles.mimic.firecracker_price", 5),
    MIMIC_NOTE_PRICE("roles.mimic.note_price", 5),
    MIMIC_REVOLVER_PRICE("roles.mimic.revolver_price", 300),
    MIMIC_MICEYES_PRICE("roles.mimic.miceyes_price", 350),

    MORPHLING_MORPH_SECONDS("roles.morphling.morph_seconds", 120, 1),

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

    NIGHTMARE_MAX_SHIELD_LAYERS("roles.nightmare.max_shield_layers", 2),

    NOISEMAKER_MARK_COOLDOWN_SECONDS("roles.noisemaker.mark_cooldown_seconds", 30),
    NOISEMAKER_FIRECRACKER_PRICE("roles.noisemaker.firecracker_price", 75),
    NOISEMAKER_SHOUT_PRICE("roles.noisemaker.shout_price", 100),
    NOISEMAKER_SHOUT_COOLDOWN_SECONDS("roles.noisemaker.shout_cooldown_seconds", 40),
    NOISEMAKER_SHOUT_CREDIT_COOLDOWN_SECONDS("roles.noisemaker.shout_credit_cooldown_seconds", 120),
    NOISEMAKER_SHOUT_GLOW_SECONDS("roles.noisemaker.shout_glow_seconds", 30),

    PENITENT_TARGET_KILLS("roles.penitent.target_kills", 2, 1),
    PENITENT_LARGE_GAME_TARGET_KILLS("roles.penitent.large_game_target_kills", 3, 1),
    PENITENT_LARGE_GAME_MIN_PLAYERS("roles.penitent.large_game_min_players", 9, 1),
    PENITENT_TASK_REWARD("roles.penitent.task_reward", 50, 1),

    PUPPETEER_EXPOSED_GLOW_SECONDS("roles.puppeteer.exposed_glow_seconds", 10),
    PUPPETEER_KILLED_SLOWNESS_SECONDS("roles.puppeteer.killed_slowness_seconds", 15),

    SNIPER_MAX_RANGE("roles.sniper.max_range", 300, 1, 1024),
    SNIPER_OPENING_TASKS("roles.sniper.opening_tasks", 2, 1),

    SPY_CORRECT_COOLDOWN_SECONDS("roles.spy.correct_cooldown_seconds", 25),
    SPY_WRONG_COOLDOWN_SECONDS("roles.spy.wrong_cooldown_seconds", 120),
    SPY_MAX_WRONG_GUESSES("roles.spy.max_wrong_guesses", 2, 1),
    SPY_REQUIRED_DIVISOR("roles.spy.required_divisor", 2, 1),
    SPY_REQUIRED_REDUCTION("roles.spy.required_reduction", 1),

    STALKER_AIM_RANGE("roles.stalker.aim_range", 8, 1, 128),

    SUBVERTER_TIMEOUT_SECONDS("roles.subverter.subvert_timeout_seconds", 30, 1),
    SUBVERTER_GLOW_SECONDS("roles.subverter.subverted_glow_seconds", 3),
    SUBVERTER_REWARD("roles.subverter.subverted_reward", 100),

    TRAPPER_TRAP_LIFETIME_SECONDS("roles.beast_trapper.trap_lifetime_seconds", 210, 1),

    VALENTINA_MAX_EYES("roles.valentina.max_eyes", 2, 1),
    VALENTINA_TASKS_PER_EYE("roles.valentina.tasks_per_eye", 4, 1),
    VALENTINA_FORESIGHT_COST("roles.valentina.foresight_cost", 2, 1),

    VIGILANTE_DETECT_RANGE("roles.vigilante.detect_range", 2.5D, 0.5D, 64.0D),

    VOODOO_NIGHT_VISION_PRICE("roles.voodoo.night_vision_price", 300),
    VOODOO_NIGHT_VISION_SECONDS("roles.voodoo.night_vision_seconds", 60, 1),

    ZHANGSHI_XUEZI_END_DELAY_SECONDS("roles.zhangshi.xuezi_end_delay_seconds", 5, 1),

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
    private final double fallback;
    private final double min;
    private final double max;
    private final boolean decimal;

    Tunable(String path, int fallback) {
        this(path, fallback, 0, LIMIT);
    }

    Tunable(String path, int fallback, int min) {
        this(path, fallback, min, LIMIT);
    }

    Tunable(String path, int fallback, int min, int max) {
        this(path, fallback, min, max, false);
    }

    Tunable(String path, double fallback, double min, double max) {
        this(path, fallback, min, max, true);
    }

    Tunable(String path, double fallback, double min, double max, boolean decimal) {
        int split = path.lastIndexOf('.');
        this.path = path;
        this.parents = path.substring(0, split).split("\\.");
        this.key = path.substring(split + 1);
        this.fallback = fallback;
        this.min = min;
        this.max = max;
        this.decimal = decimal;
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
        return (int) Math.round(ModConfig.tunable(this));
    }

    public double doubleValue() {
        return ModConfig.tunable(this);
    }

    public int ticks() {
        return (int) Math.round(ModConfig.tunable(this) * 20.0D);
    }

    public String text() {
        return format(ModConfig.tunable(this));
    }

    static double[] defaults() {
        Tunable[] tunables = values();
        double[] defaults = new double[tunables.length];
        for (Tunable tunable : tunables) defaults[tunable.ordinal()] = tunable.fallback;
        return defaults;
    }

    String path() {
        return this.path;
    }

    String[] parents() {
        return this.parents;
    }

    String key() {
        return this.key;
    }

    boolean decimal() {
        return this.decimal;
    }

    @Nullable
    String roleId() {
        return this.parents.length == 2 && "roles".equals(this.parents[0]) ? this.parents[1] : null;
    }

    boolean accepts(double value) {
        if (!Double.isFinite(value) || value < this.min || value > this.max) return false;
        return this.decimal || value == Math.rint(value);
    }

    String requirement() {
        return (this.decimal ? "a number from " : "an integer from ") + format(this.min) + " to " + format(this.max);
    }

    private static String format(double value) {
        return value == Math.rint(value) ? Long.toString((long) value) : Double.toString(value);
    }
}
