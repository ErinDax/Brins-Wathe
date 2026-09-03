package cn.erindax.brinswathe.client;

import cn.erindax.brinswathe.config.ModConfig;
import cn.erindax.brinswathe.config.ModConfig.ShopItem;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

public final class LanguageVariables {
    private LanguageVariables() {
    }

    public static String replace(String key, String translation) {
        if (translation == null || translation.indexOf('{') < 0) return translation;
        String roleId = configRoleId(roleId(key));
        if (roleId == null) return translation;
        Integer initialBalance = ModConfig.initialBalance(roleId);
        return translation
            .replace("{skill_cost}", Integer.toString(ModConfig.skillCost(roleId)))
            .replace("{skill_cooldown_seconds}", Integer.toString(ModConfig.skillCooldownSeconds(roleId)))
            .replace("{skill_duration_seconds}", Integer.toString(ModConfig.skillDurationSeconds(roleId)))
            .replace("{initial_balance}", Integer.toString(initialBalance == null ? 0 : initialBalance))
            .replace("{knife_price}", Integer.toString(ModConfig.shopPrice(roleId, ShopItem.KNIFE)))
            .replace("{revolver_price}", Integer.toString(ModConfig.shopPrice(roleId, ShopItem.REVOLVER)))
            .replace("{psycho_mode_price}", Integer.toString(ModConfig.shopPrice(roleId, ShopItem.PSYCHO_MODE)))
            .replace("{forced_sleep_task_cooldown_seconds}", Integer.toString(ModConfig.nightmareForcedSleepCooldownSeconds()))
            .replace("{bone_knife_price}", Integer.toString(ModConfig.boneKnifePrice()))
            .replace("{bone_knife_cooldown_seconds}", Integer.toString(ModConfig.boneKnifeCooldownSeconds()))
            .replace("{identity_hint_price}", Integer.toString(ModConfig.penitentIdentityHintPrice()))
            .replace("{antidote_price}", Integer.toString(ModConfig.penitentAntidotePrice()))
            .replace("{starting_shield_layers}", Integer.toString(ModConfig.penitentStartingShieldLayers()))
            .replace("{explosion_range}", Integer.toString(ModConfig.terroristExplosionRange()))
            .replace("{explosion_survival_kills}", Integer.toString(ModConfig.terroristExplosionSurvivalKills()))
            .replace("{trap_limit}", Integer.toString(ModConfig.trapperTrapLimit()))
            .replace("{xuezi_price}", Integer.toString(ModConfig.xueziPrice()))
            .replace("{xuezi_cooldown_seconds}", Integer.toString(ModConfig.xueziCooldownSeconds()))
            .replace("{xuezi_shield_layers}", Integer.toString(ModConfig.xueziShieldLayers()))
            .replace("{self_destruct_price}", Integer.toString(ModConfig.puppeteerSelfDestructPrice()))
            .replace("{return_cooldown_seconds}", Integer.toString(ModConfig.puppeteerReturnCooldownSeconds()))
            .replace("{grenade_price}", Integer.toString(ModConfig.bomberGrenadePrice()))
            .replace("{bomb_price}", Integer.toString(ModConfig.bomberBombPrice()))
            .replace("{bomb_fuse_seconds}", Integer.toString(ModConfig.bomberBombFuseSeconds()))
            .replace("{bomb_warning_seconds}", Integer.toString(ModConfig.bomberBombWarningSeconds()))
            .replace("{bomb_explosion_size}", Integer.toString(ModConfig.bomberBombExplosionSize()))
            .replace(
                "{bomb_purchase_cooldown_seconds}",
                Integer.toString(ModConfig.bomberBombPurchaseCooldownSeconds())
            )
            .replace("{instinct_seconds}", Integer.toString(ModConfig.avengerInstinctSeconds()))
            .replace("{glow_seconds}", Integer.toString(ModConfig.sniperGlowSeconds()))
            .replace("{tasks_to_reset}", Integer.toString(ModConfig.sniperTasksToReset()))
            .replace("{kill_task_progress}", Integer.toString(ModConfig.sniperKillTaskProgress()))
            .replace("{mine_price}", Integer.toString(ModConfig.bomberMinePrice()))
            .replace("{extra_trap_price}", Integer.toString(ModConfig.trapperExtraTrapPrice()))
            .replace("{trap_catch_reward}", Integer.toString(ModConfig.trapperCatchReward()))
            .replace("{duel_timeout_seconds}", Integer.toString(ModConfig.cowboyDuelTimeoutSeconds()))
            .replace("{duel_countdown_seconds}", Integer.toString(ModConfig.cowboyDuelCountdownSeconds()));
    }

    public static List<String> aliases(String key) {
        LinkedHashSet<String> aliases = new LinkedHashSet<>();
        aliases.add(key);
        if (key.startsWith("announcement.goal.")) {
            aliases.add("announcement.goals." + key.substring("announcement.goal.".length()));
        } else if (key.startsWith("announcement.goals.")) {
            aliases.add("announcement.goal." + key.substring("announcement.goals.".length()));
        }

        for (String alias : new ArrayList<>(aliases)) {
            int colonIndex = alias.lastIndexOf("brin:");
            if (colonIndex >= 0) {
                aliases.add(alias.substring(0, colonIndex) + "brin." + alias.substring(colonIndex + 5));
            }
            int dotIndex = alias.lastIndexOf("brin.");
            if (dotIndex >= 0) {
                aliases.add(alias.substring(0, dotIndex) + "brin:" + alias.substring(dotIndex + 5));
            }
        }

        aliases.remove(key);
        for (String alias : new ArrayList<>(aliases)) {
            if (alias.contains("beastcatcher")) {
                aliases.add(alias.replace("beastcatcher", "beast_trapper"));
            }
            if (alias.contains("beast_trapper")) {
                aliases.add(alias.replace("beast_trapper", "beastcatcher"));
            }
        }
        if (key.contains("beastcatcher")) {
            aliases.add(key.replace("beastcatcher", "beast_trapper"));
        }
        if (key.contains("beast_trapper")) {
            aliases.add(key.replace("beast_trapper", "beastcatcher"));
        }
        aliases.remove(key);
        return List.copyOf(aliases);
    }

    private static String configRoleId(String roleId) {
        if (roleId == null) return null;
        if ("beastcatcher".equals(roleId) || "trapper".equals(roleId)) return "beast_trapper";
        return roleId;
    }

    private static String roleId(String key) {
        int colonIndex = key.lastIndexOf("brin:");
        if (colonIndex >= 0) return key.substring(colonIndex + 5);
        int dotIndex = key.lastIndexOf("brin.");
        if (dotIndex >= 0) return key.substring(dotIndex + 5);
        return null;
    }
}
