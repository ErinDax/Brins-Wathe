package cn.autoforged.brinswathe.client;

import cn.autoforged.brinswathe.config.BrinConfig;
import cn.autoforged.brinswathe.config.BrinConfig.ShopItem;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

public final class BrinLanguageVariables {
    private BrinLanguageVariables() {
    }

    public static String replace(String key, String translation) {
        if (translation == null || translation.indexOf('{') < 0) return translation;
        String roleId = roleId(key);
        if (roleId == null) return translation;
        Integer initialBalance = BrinConfig.initialBalance(roleId);
        return translation
            .replace("{skill_cost}", Integer.toString(BrinConfig.skillCost(roleId)))
            .replace("{skill_cooldown_seconds}", Integer.toString(BrinConfig.skillCooldownSeconds(roleId)))
            .replace("{skill_duration_seconds}", Integer.toString(BrinConfig.skillDurationSeconds(roleId)))
            .replace("{initial_balance}", Integer.toString(initialBalance == null ? 0 : initialBalance))
            .replace("{knife_price}", Integer.toString(BrinConfig.shopPrice(roleId, ShopItem.KNIFE)))
            .replace("{revolver_price}", Integer.toString(BrinConfig.shopPrice(roleId, ShopItem.REVOLVER)))
            .replace("{psycho_mode_price}", Integer.toString(BrinConfig.shopPrice(roleId, ShopItem.PSYCHO_MODE)))
            .replace("{forced_sleep_task_cooldown_seconds}", Integer.toString(BrinConfig.nightmareForcedSleepCooldownSeconds()))
            .replace("{bone_knife_price}", Integer.toString(BrinConfig.boneKnifePrice()))
            .replace("{bone_knife_cooldown_seconds}", Integer.toString(BrinConfig.boneKnifeCooldownSeconds()))
            .replace("{identity_hint_price}", Integer.toString(BrinConfig.penitentIdentityHintPrice()))
            .replace("{antidote_price}", Integer.toString(BrinConfig.penitentAntidotePrice()))
            .replace("{starting_shield_layers}", Integer.toString(BrinConfig.penitentStartingShieldLayers()))
            .replace("{explosion_range}", Integer.toString(BrinConfig.terroristExplosionRange()))
            .replace("{explosion_survival_kills}", Integer.toString(BrinConfig.terroristExplosionSurvivalKills()))
            .replace("{trap_limit}", Integer.toString(BrinConfig.trapperTrapLimit()))
            .replace("{xuezi_price}", Integer.toString(BrinConfig.xueziPrice()))
            .replace("{xuezi_cooldown_seconds}", Integer.toString(BrinConfig.xueziCooldownSeconds()))
            .replace("{xuezi_shield_layers}", Integer.toString(BrinConfig.xueziShieldLayers()))
            .replace("{self_destruct_price}", Integer.toString(BrinConfig.puppeteerSelfDestructPrice()))
            .replace("{return_cooldown_seconds}", Integer.toString(BrinConfig.puppeteerReturnCooldownSeconds()))
            .replace("{grenade_price}", Integer.toString(BrinConfig.bomberGrenadePrice()))
            .replace("{bomb_price}", Integer.toString(BrinConfig.bomberBombPrice()))
            .replace("{bomb_fuse_seconds}", Integer.toString(BrinConfig.bomberBombFuseSeconds()))
            .replace("{bomb_warning_seconds}", Integer.toString(BrinConfig.bomberBombWarningSeconds()))
            .replace("{bomb_explosion_size}", Integer.toString(BrinConfig.bomberBombExplosionSize()))
            .replace(
                "{bomb_purchase_cooldown_seconds}",
                Integer.toString(BrinConfig.bomberBombPurchaseCooldownSeconds())
            )
            .replace("{instinct_seconds}", Integer.toString(BrinConfig.avengerInstinctSeconds()))
            .replace("{glow_seconds}", Integer.toString(BrinConfig.sniperGlowSeconds()))
            .replace("{duel_timeout_seconds}", Integer.toString(BrinConfig.cowboyDuelTimeoutSeconds()))
            .replace("{duel_countdown_seconds}", Integer.toString(BrinConfig.cowboyDuelCountdownSeconds()));
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
        return List.copyOf(aliases);
    }

    private static String roleId(String key) {
        int colonIndex = key.lastIndexOf("brin:");
        if (colonIndex >= 0) return key.substring(colonIndex + 5);
        int dotIndex = key.lastIndexOf("brin.");
        if (dotIndex >= 0) return key.substring(dotIndex + 5);
        return null;
    }
}
