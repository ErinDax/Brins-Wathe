package cn.erindax.brinswathe;

import cn.erindax.brinswathe.component.SniperComponent;
import cn.erindax.brinswathe.config.ModConfig;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import org.BsXinQin.kinswathe.component.GameSafeComponent;

public final class Sniper {
    private Sniper() {
    }

    public static void handleAbility(ServerPlayer player, GameWorldComponent gameWorld) {
        if (!gameWorld.isRole(player, ModRoles.SNIPER)) return;
        if (GameSafeComponent.KEY.get(player.level()).isGameSafe) return;
        SniperComponent component = SniperComponent.KEY.get(player);
        if (component == null) return;

        if (component.isAiming()) {
            component.startShotCooldown(ModConfig.skillCooldownSeconds("sniper"));
            component.fire(player.getEyePosition(), player.getLookAngle());
            player.level().playSound(
                null,
                player.blockPosition(),
                SoundEvents.CROSSBOW_SHOOT,
                SoundSource.PLAYERS,
                2.0F,
                0.55F
            );
            return;
        }
        if (component.getCooldownTicks() > 0 || component.isAbilityActive()) return;
        if (!AbilityCost.spend(player, "sniper")) return;
        component.startAiming();
        player.playNotifySound(SoundEvents.SPYGLASS_USE, SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    public static void handleCancel(ServerPlayer player, GameWorldComponent gameWorld) {
        if (!gameWorld.isRole(player, ModRoles.SNIPER)) return;
        SniperComponent component = SniperComponent.KEY.get(player);
        if (component == null || !component.isAiming()) return;
        component.cancelAiming();
        player.playNotifySound(SoundEvents.SPYGLASS_STOP_USING, SoundSource.PLAYERS, 1.0F, 1.0F);
    }
}
