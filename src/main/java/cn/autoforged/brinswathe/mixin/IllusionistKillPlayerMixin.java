package cn.autoforged.brinswathe.mixin;

import cn.autoforged.brinswathe.component.IllusionistComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameFunctions.class)
public abstract class IllusionistKillPlayerMixin {
    @Inject(
        method = "killPlayer(Lnet/minecraft/world/entity/player/Player;ZLnet/minecraft/world/entity/player/Player;)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private static void brinProtectControlledClone(Player victim, boolean dropItems, Player killer,
                                                    CallbackInfo ci) {
        if (brinEndControlledClone(victim, killer)) ci.cancel();
    }

    @Inject(
        method = "killPlayer(Lnet/minecraft/world/entity/player/Player;ZLnet/minecraft/world/entity/player/Player;Lnet/minecraft/resources/ResourceLocation;)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private static void brinProtectControlledClone(Player victim, boolean dropItems, Player killer,
                                                    ResourceLocation reason, CallbackInfo ci) {
        if (brinEndControlledClone(victim, killer)) ci.cancel();
    }

    private static boolean brinEndControlledClone(Player victim, Player killer) {
        IllusionistComponent component = IllusionistComponent.KEY.get(victim);
        if (component.controlledCloneId == null) return false;
        component.endSkill();
        IllusionistComponent.applyCloneKillBlindness(killer);
        return true;
    }
}
