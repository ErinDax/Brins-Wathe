package cn.erindax.brinswathe.mixin;

import cn.erindax.brinswathe.BrinNoelleAccess;
import cn.erindax.brinswathe.BrinRoles;
import cn.erindax.brinswathe.component.SniperComponent;
import cn.erindax.brinswathe.config.BrinConfig;
import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.PlayerMoodComponent;
import dev.doctor4t.wathe.cca.PlayerShopComponent;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = PlayerMoodComponent.class, priority = 500)
public abstract class BrinTaskRewardMixin {
    @Unique
    private static final ResourceLocation BRIN_STARSTRUCK_ID =
        ResourceLocation.fromNamespaceAndPath("starexpress", "starstruck");

    @Shadow
    @Final
    private Player player;

    @Shadow
    @Mutable
    private int nextTaskTimer;

    @Shadow
    public abstract float getMood();

    @Unique
    private int brin$taskBalanceBefore;

    @Unique
    private int brin$taskReward;

    @Unique
    private int brin$taskTimerBefore;

    @Unique
    private boolean brin$restoreTaskTimer;

    @Unique
    private boolean brin$taskCompleted;

    @Unique
    private Map<UUID, Integer> brin$killerBalancesBefore;

    @Inject(method = "setMood(F)V", at = @At("HEAD"))
    private void brinCaptureTaskBalance(float mood, CallbackInfo callback) {
        this.brin$taskReward = 0;
        this.brin$restoreTaskTimer = false;
        this.brin$taskCompleted = false;
        this.brin$killerBalancesBefore = null;
        if (this.player.level().isClientSide || mood <= this.getMood()) return;
        this.brin$taskCompleted = true;

        if (brinIsStarstruck(this.player)) {
            this.brin$taskTimerBefore = this.nextTaskTimer;
            this.brin$restoreTaskTimer = true;
        }

        GameWorldComponent game = GameWorldComponent.KEY.get(this.player.level());
        Role role = game.getRole(this.player);
        if (role != null && "licensed_villain".equals(role.identifier().getPath())
            && "kinswathe".equals(role.identifier().getNamespace())) {
            this.nextTaskTimer = 0;
        }
        if (game.isRole(this.player, BrinRoles.BERSERKER)
            || game.isRole(this.player, BrinRoles.GAMBLER)) {
            this.brin$taskReward = 50;
        } else if (game.isRole(this.player, BrinRoles.PENITENT)) {
            this.brin$taskReward = 20;
        } else if (role != null && role.getMoodType() == Role.MoodType.REAL) {
            if (BrinNoelleAccess.isRole(game, this.player, BrinNoelleAccess.MIMIC_ID)) {
                this.brin$taskReward = 25;
            } else {
                this.brin$taskReward = 50 + this.player.getRandom().nextInt(31);
            }
        } else if (role != null && !game.isInnocent(this.player) && role.canUseKiller()) {
            this.brin$killerBalancesBefore = new HashMap<>();
            for (UUID killerId : game.getAllKillerTeamPlayers()) {
                Player killer = this.player.level().getPlayerByUUID(killerId);
                if (killer == null) continue;
                PlayerShopComponent shop = PlayerShopComponent.KEY.get(killer);
                if (shop != null) this.brin$killerBalancesBefore.put(killerId, shop.balance);
            }
        }
        if (this.brin$taskReward > 0) {
            this.brin$taskBalanceBefore = PlayerShopComponent.KEY.get(this.player).balance;
        }
    }

    @Inject(method = "setMood(F)V", at = @At("RETURN"))
    private void brinApplyTaskReward(float mood, CallbackInfo callback) {
        if (this.brin$restoreTaskTimer) {
            this.brin$restoreTaskTimer = false;
            if (this.nextTaskTimer <= 0 && this.brin$taskTimerBefore > 0) {
                this.nextTaskTimer = this.brin$taskTimerBefore;
            }
        }
        if (this.brin$taskCompleted) {
            this.brin$taskCompleted = false;
            GameWorldComponent game = GameWorldComponent.KEY.get(this.player.level());
            if (game.isRole(this.player, BrinRoles.SNIPER)) {
                SniperComponent sniper = SniperComponent.KEY.get(this.player);
                if (sniper != null) sniper.recordTask(BrinConfig.sniperTasksToReset());
            }
        }
        if (this.brin$killerBalancesBefore != null) {
            for (Map.Entry<UUID, Integer> entry : this.brin$killerBalancesBefore.entrySet()) {
                Player killer = this.player.level().getPlayerByUUID(entry.getKey());
                if (killer == null) continue;
                PlayerShopComponent shop = PlayerShopComponent.KEY.get(killer);
                if (shop == null) continue;
                shop.setBalance(entry.getValue() + 5);
                killer.displayClientMessage(
                    Component.literal("杀手完成任务，全队 +5 金币").withStyle(ChatFormatting.GOLD),
                    true
                );
            }
            this.brin$killerBalancesBefore = null;
        }
        if (this.brin$taskReward <= 0) return;
        PlayerShopComponent shop = PlayerShopComponent.KEY.get(this.player);
        shop.setBalance(this.brin$taskBalanceBefore + this.brin$taskReward);
        this.brin$taskReward = 0;
    }
    @Unique
    private static boolean brinIsStarstruck(Player player) {
        Role role = GameWorldComponent.KEY.get(player.level()).getRole(player);
        return role != null && BRIN_STARSTRUCK_ID.equals(role.identifier());
    }
}
