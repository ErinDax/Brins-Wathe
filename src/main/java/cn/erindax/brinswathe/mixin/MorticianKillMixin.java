package cn.erindax.brinswathe.mixin;

import cn.erindax.brinswathe.component.MorticianComponent;
import dev.doctor4t.wathe.game.GameConstants;
import dev.doctor4t.wathe.game.GameFunctions;
import dev.doctor4t.wathe.index.WatheItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameFunctions.class)
public abstract class MorticianKillMixin {
    @Inject(
        method = "killPlayer(Lnet/minecraft/world/entity/player/Player;ZLnet/minecraft/world/entity/player/Player;Lnet/minecraft/resources/ResourceLocation;)V",
        at = @At("RETURN")
    )
    private static void brinMorticianAmbushKnife(
        Player victim,
        boolean spawnBody,
        Player attacker,
        ResourceLocation deathReason,
        CallbackInfo callback
    ) {
        if (attacker == null || attacker == victim || victim.level().isClientSide) return;
        if (!GameFunctions.isPlayerEliminated(victim)) return;
        if (!GameConstants.DeathReasons.KNIFE.equals(deathReason)) return;
        if (!(attacker instanceof ServerPlayer serverAttacker)) return;
        MorticianComponent component = MorticianComponent.KEY.get(serverAttacker);
        if (component == null || !component.markAmbushKnifeKill()) return;
        serverAttacker.getCooldowns().removeCooldown(WatheItems.KNIFE);
    }
}
