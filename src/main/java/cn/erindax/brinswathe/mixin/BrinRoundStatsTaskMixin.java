package cn.erindax.brinswathe.mixin;

import cn.erindax.brinswathe.musicbox.BrinRoundStats;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.doctor4t.wathe.cca.PlayerMoodComponent;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PlayerMoodComponent.class)
public abstract class BrinRoundStatsTaskMixin {
    @WrapOperation(
        method = "serverTick",
        at = @At(
            value = "INVOKE",
            target = "Ldev/doctor4t/wathe/cca/PlayerMoodComponent$TrainTask;isFulfilled(Lnet/minecraft/world/entity/player/Player;)Z"
        )
    )
    private boolean brinCountRoundTask(
        PlayerMoodComponent.TrainTask task,
        Player player,
        Operation<Boolean> original
    ) {
        boolean fulfilled = original.call(task, player);
        if (fulfilled) BrinRoundStats.recordTask(player);
        return fulfilled;
    }
}
