package cn.autoforged.brinswathe.mixin;

import cn.autoforged.brinswathe.BrinRoles;
import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.PlayerMoodComponent;
import dev.doctor4t.wathe.cca.PlayerShopComponent;
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

    @Inject(method = "setMood(F)V", at = @At("HEAD"))
    private void brinCaptureTaskBalance(float mood, CallbackInfo callback) {
        this.brin$taskReward = 0;
        this.brin$restoreTaskTimer = false;
        if (this.player.level().isClientSide || mood <= this.getMood()) return;

        // This mixin is applied first, so the timer is still untouched by the mods that zero it below.
        if (brinIsStarstruck(this.player)) {
            this.brin$taskTimerBefore = this.nextTaskTimer;
            this.brin$restoreTaskTimer = true;
        }

        GameWorldComponent game = GameWorldComponent.KEY.get(this.player.level());
        if (game.isRole(this.player, BrinRoles.BERSERKER)
            || game.isRole(this.player, BrinRoles.GAMBLER)) {
            this.brin$taskReward = 50;
        } else if (game.isRole(this.player, BrinRoles.PENITENT)) {
            this.brin$taskReward = 20;
        }
        if (this.brin$taskReward > 0) {
            this.brin$taskBalanceBefore = PlayerShopComponent.KEY.get(this.player).balance;
        }
    }

    /**
     * Star Express hands the starstruck a fresh task the instant the last one is done by resetting the
     * countdown from inside {@code setMood}. Only that reset is undone here; the ability cooldown it also
     * grants is left alone.
     */
    @Inject(method = "setMood(F)V", at = @At("RETURN"))
    private void brinApplyTaskReward(float mood, CallbackInfo callback) {
        if (this.brin$restoreTaskTimer) {
            this.brin$restoreTaskTimer = false;
            if (this.nextTaskTimer <= 0 && this.brin$taskTimerBefore > 0) {
                this.nextTaskTimer = this.brin$taskTimerBefore;
            }
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
