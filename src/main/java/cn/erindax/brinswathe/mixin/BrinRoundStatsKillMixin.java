package cn.erindax.brinswathe.mixin;

import cn.erindax.brinswathe.musicbox.BrinRoundStats;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = GameFunctions.class, priority = 2100)
public abstract class BrinRoundStatsKillMixin {
    @WrapMethod(method = "killPlayer(Lnet/minecraft/world/entity/player/Player;ZLnet/minecraft/world/entity/player/Player;Lnet/minecraft/resources/ResourceLocation;)V")
    private static void brinCountRoundKill(
        Player victim,
        boolean spawnBody,
        Player killer,
        ResourceLocation deathReason,
        Operation<Void> original
    ) {
        BrinRoundStats.KillContext context = BrinRoundStats.beforeKill(victim, killer);
        try {
            original.call(victim, spawnBody, killer, deathReason);
        } finally {
            BrinRoundStats.afterKill(victim, context);
        }
    }
}
