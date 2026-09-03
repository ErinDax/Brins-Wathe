package cn.erindax.brinswathe.mixin;

import cn.erindax.brinswathe.BrinRoles;
import cn.erindax.brinswathe.component.SniperComponent;
import cn.erindax.brinswathe.config.BrinConfig;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameFunctions.class)
public abstract class SniperKillTrackingMixin {
    @Inject(
        method = "killPlayer(Lnet/minecraft/world/entity/player/Player;ZLnet/minecraft/world/entity/player/Player;Lnet/minecraft/resources/ResourceLocation;)V",
        at = @At("RETURN")
    )
    private static void brinSniperConsumeShotFlag(
        Player victim,
        boolean spawnBody,
        Player attacker,
        ResourceLocation deathReason,
        CallbackInfo callback
    ) {
        if (attacker == null || attacker.level().isClientSide) return;
        SniperComponent sniper = SniperComponent.KEY.get(attacker);
        if (sniper == null) return;
        sniper.consumeShotKillPending();
        if (attacker.equals(victim)) return;
        if (GameFunctions.isPlayerAliveAndSurvival(victim)) return;
        GameWorldComponent game = GameWorldComponent.KEY.get(attacker.level());
        if (!game.isRole(attacker, BrinRoles.SNIPER)) return;
        sniper.addRecoveryProgress(BrinConfig.sniperKillTaskProgress(), BrinConfig.sniperTasksToReset());
    }
}
