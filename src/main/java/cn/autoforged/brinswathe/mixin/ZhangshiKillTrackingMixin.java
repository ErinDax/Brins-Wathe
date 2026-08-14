package cn.autoforged.brinswathe.mixin;

import cn.autoforged.brinswathe.BrinRoles;
import cn.autoforged.brinswathe.component.ZhangshiComponent;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameFunctions.class)
public abstract class ZhangshiKillTrackingMixin {
    @Inject(
        method = "killPlayer(Lnet/minecraft/world/entity/player/Player;ZLnet/minecraft/world/entity/player/Player;Lnet/minecraft/resources/ResourceLocation;)V",
        at = @At("RETURN")
    )
    private static void brinZhangshiTrackKill(
        Player victim,
        boolean spawnBody,
        Player attacker,
        ResourceLocation deathReason,
        CallbackInfo callback
    ) {
        if (attacker == null || attacker == victim || victim.level().isClientSide
            || !GameFunctions.isPlayerEliminated(victim)) return;
        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(attacker.level());
        if (!gameWorld.isRunning() || !gameWorld.isRole(attacker, BrinRoles.ZHANGSHI)) return;
        ZhangshiComponent component = ZhangshiComponent.KEY.get(attacker);
        if (component != null) component.recordKill();
    }
}
