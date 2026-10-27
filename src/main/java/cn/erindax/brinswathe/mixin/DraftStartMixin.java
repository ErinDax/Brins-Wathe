package cn.erindax.brinswathe.mixin;

import cn.erindax.brinswathe.Draft;
import cn.erindax.brinswathe.SecretMurder;
import dev.doctor4t.wathe.api.GameMode;
import dev.doctor4t.wathe.api.MapEffect;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameFunctions.class)
public abstract class DraftStartMixin {
    @Inject(method = "startGame", at = @At("HEAD"), cancellable = true)
    private static void brinDraftBeforeStart(
        ServerLevel world,
        GameMode gameMode,
        MapEffect mapEffect,
        int time,
        CallbackInfo ci
    ) {
        if (SecretMurder.interceptStart(world, gameMode, mapEffect, time)
            || Draft.interceptStart(world, gameMode, mapEffect, time)) {
            ci.cancel();
        }
    }
}
