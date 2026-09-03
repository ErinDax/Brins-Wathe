package cn.erindax.brinswathe;

import cn.erindax.brinswathe.component.TrapperComponent;
import cn.erindax.brinswathe.entity.TrapperFangs;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.projectile.EvokerFangs;

public final class TrapperTraps {
    private TrapperTraps() {
    }

    public static boolean place(ServerPlayer player, boolean ignoreLimit) {
        TrapperComponent component = TrapperComponent.KEY.get(player);
        if (component == null) return false;
        if (!ignoreLimit && !component.canPlaceTrap()) return false;

        EvokerFangs trapEntity = new EvokerFangs(
            player.level(),
            player.getX(),
            player.getY(),
            player.getZ(),
            player.getYRot() * Mth.DEG_TO_RAD,
            Integer.MAX_VALUE,
            player
        );
        trapEntity.setSilent(true);
        ((TrapperFangs) trapEntity).brin$setTrapperTrap(true);
        trapEntity.addTag("brin_trapper_trap");
        if (!player.level().addFreshEntity(trapEntity)) return false;

        component.setTrap(trapEntity.getUUID(), ignoreLimit);
        player.level().playSound(
            null, player.blockPosition(), SoundEvents.CHAIN_PLACE, SoundSource.PLAYERS, 1.0F, 1.0F);
        return true;
    }
}
