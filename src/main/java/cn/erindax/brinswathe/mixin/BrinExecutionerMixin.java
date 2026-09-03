package cn.erindax.brinswathe.mixin;

import cn.erindax.brinswathe.BrinExecutioner;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = GameFunctions.class, priority = 2000)
public abstract class BrinExecutionerMixin {
    @WrapMethod(method = "killPlayer(Lnet/minecraft/world/entity/player/Player;ZLnet/minecraft/world/entity/player/Player;Lnet/minecraft/resources/ResourceLocation;)V")
    private static void brinExecutionerKill(
        Player victim,
        boolean spawnBody,
        Player killer,
        ResourceLocation deathReason,
        Operation<Void> original
    ) {
        if (victim.level().isClientSide) {
            original.call(victim, spawnBody, killer, deathReason);
            return;
        }
        BrinExecutioner.KillFrame frame = BrinExecutioner.beginKill(victim);
        try {
            original.call(victim, spawnBody, killer, deathReason);
        } finally {
            BrinExecutioner.endKill(victim, killer, frame);
        }
    }
}
