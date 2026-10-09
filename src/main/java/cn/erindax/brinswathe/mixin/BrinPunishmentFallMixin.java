package cn.erindax.brinswathe.mixin;

import cn.erindax.brinswathe.BrinPunishment;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameFunctions.class)
public abstract class BrinPunishmentFallMixin {
    @Inject(
        method = "killPlayer(Lnet/minecraft/world/entity/player/Player;ZLnet/minecraft/world/entity/player/Player;Lnet/minecraft/resources/ResourceLocation;)V",
        at = @At("RETURN")
    )
    private static void brinPunishEarlyFall(
        Player victim,
        boolean spawnBody,
        Player killer,
        ResourceLocation deathReason,
        CallbackInfo ci
    ) {
        BrinPunishment.onKilled(victim, killer, deathReason);
    }
}
