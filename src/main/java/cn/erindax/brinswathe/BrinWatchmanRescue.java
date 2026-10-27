package cn.erindax.brinswathe;

import cn.erindax.brinswathe.component.BombComponent;
import cn.erindax.brinswathe.component.TrapperComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.world.entity.player.Player;
import org.BsXinQin.kinswathe.component.PlayerEffectComponent;
import org.jetbrains.annotations.Nullable;

public final class BrinWatchmanRescue {
    public static final int LEFTOVER_STUN_TICKS = 20 * 60;

    private BrinWatchmanRescue() {
    }

    @Nullable
    public static TrapperComponent trapperOf(Player target) {
        for (Player possibleTrapper : target.level().players()) {
            TrapperComponent trapper = TrapperComponent.KEY.get(possibleTrapper);
            if (trapper != null && trapper.isTrapped(target.getUUID())) return trapper;
        }
        return null;
    }

    public static boolean hasBombToDefuse(Player target) {
        BombComponent bomb = BombComponent.KEY.get(target);
        return bomb != null && bomb.canBeDefused();
    }

    public static boolean hasLeftoverStun(Player target) {
        PlayerEffectComponent stun = PlayerEffectComponent.KEY.get(target);
        return stun != null && stun.stunTicks > LEFTOVER_STUN_TICKS;
    }

    public static boolean canRescue(Player target) {
        if (target == null || !GameFunctions.isPlayerAliveAndSurvival(target)) return false;
        return trapperOf(target) != null || hasBombToDefuse(target) || hasLeftoverStun(target);
    }
}
