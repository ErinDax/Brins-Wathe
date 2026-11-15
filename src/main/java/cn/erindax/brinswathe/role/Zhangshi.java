package cn.erindax.brinswathe.role;

import cn.erindax.brinswathe.ModRoles;
import cn.erindax.brinswathe.component.ZhangshiComponent;
import cn.erindax.brinswathe.config.ModConfig;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import org.BsXinQin.kinswathe.component.AbilityPlayerComponent;

public final class Zhangshi {
    private Zhangshi() {
    }

    public static void handleSpeed(ServerPlayer player, GameWorldComponent gameWorld,
                                   AbilityPlayerComponent ability) {
        if (!gameWorld.isRole(player, ModRoles.ZHANGSHI)) return;
        if (ability.cooldown > 0) return;
        ZhangshiComponent component = ZhangshiComponent.KEY.get(player);
        if (component == null || !component.activateNormal()) return;
        ability.setAbilityCooldown(ModConfig.skillCooldownSeconds("zhangshi"));
        player.playNotifySound(SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0F, 1.0F);
    }
}
