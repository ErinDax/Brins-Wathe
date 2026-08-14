package cn.autoforged.brinswathe.mixin;

import cn.autoforged.brinswathe.CompensatorPassive;
import cn.autoforged.brinswathe.CowboyDuel;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameFunctions.class)
public abstract class CompensatorKillMixin {
    @Inject(
        method = "killPlayer(Lnet/minecraft/world/entity/player/Player;ZLnet/minecraft/world/entity/player/Player;Lnet/minecraft/resources/ResourceLocation;)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private static void brinCompensatorPassive(
        Player victim,
        boolean spawnBody,
        Player attacker,
        ResourceLocation deathReason,
        CallbackInfo ci
    ) {
        if (CowboyDuel.isActive()) return;
        if (CompensatorPassive.tryHandle(victim, spawnBody, attacker, deathReason)) {
            ci.cancel();
        }
    }
}
