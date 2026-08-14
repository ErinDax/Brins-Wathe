package cn.autoforged.brinswathe.mixin;

import cn.autoforged.brinswathe.component.PuppeteerControlComponent;
import dev.doctor4t.wathe.game.GameConstants;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameFunctions.class)
public abstract class PuppetKillPlayerMixin {
    @Inject(
        method = "killPlayer(Lnet/minecraft/world/entity/player/Player;ZLnet/minecraft/world/entity/player/Player;)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private static void brinProtectControlledPuppet(Player victim, boolean dropItems, Player killer,
                                                     CallbackInfo ci) {
        if (killer != null && killer != victim && brinEndPuppetControl(victim, killer)) ci.cancel();
    }

    @Inject(
        method = "killPlayer(Lnet/minecraft/world/entity/player/Player;ZLnet/minecraft/world/entity/player/Player;Lnet/minecraft/resources/ResourceLocation;)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private static void brinProtectControlledPuppet(Player victim, boolean dropItems, Player killer,
                                                     ResourceLocation reason, CallbackInfo ci) {
        // Poison and the train are afflictions on the real body, so they have to reach the puppeteer even
        // while the possession would otherwise soak up the death.
        if (GameConstants.DeathReasons.POISON.equals(reason)
            || GameConstants.DeathReasons.FELL_OUT_OF_TRAIN.equals(reason)) return;
        if (brinEndPuppetControl(victim, killer)) ci.cancel();
    }

    private static boolean brinEndPuppetControl(Player victim, Player killer) {
        if (!(victim instanceof ServerPlayer puppeteer)) return false;
        PuppeteerControlComponent component = PuppeteerControlComponent.KEY.get(puppeteer);
        if (component == null || !component.isControlling()) return false;
        component.handlePuppetKilled(puppeteer, killer);
        return true;
    }
}
