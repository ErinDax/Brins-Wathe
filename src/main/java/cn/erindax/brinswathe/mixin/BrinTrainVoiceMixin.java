package cn.erindax.brinswathe.mixin;

import cn.erindax.brinswathe.BrinNoelleAccess;
import cn.erindax.brinswathe.component.ResurrectedComponent;
import cn.erindax.brinswathe.voice.BrinVoiceChatPlugin;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = GameFunctions.class, remap = false)
public abstract class BrinTrainVoiceMixin {
    @Inject(
        method = "killPlayer(Lnet/minecraft/world/entity/player/Player;ZLnet/minecraft/world/entity/player/Player;Lnet/minecraft/resources/ResourceLocation;)V",
        at = @At("HEAD")
    )
    private static void brinMarkInsaneKillNoDrop(
        Player victim,
        boolean spawnBody,
        Player killer,
        ResourceLocation deathReason,
        CallbackInfo ci
    ) {
        if (victim.level().isClientSide || killer == null) return;
        GameWorldComponent game = GameWorldComponent.KEY.get(victim.level());
        if (!BrinNoelleAccess.isRole(game, killer, BrinNoelleAccess.INSANE_KILLER_ID)) return;
        ResurrectedComponent component = ResurrectedComponent.KEY.get(victim);
        if (component != null) component.noDropOnDeath = true;
    }

    @Inject(
        method = "killPlayer(Lnet/minecraft/world/entity/player/Player;ZLnet/minecraft/world/entity/player/Player;Lnet/minecraft/resources/ResourceLocation;)V",
        at = @At("RETURN")
    )
    private static void brinJoinTrainVoice(
        Player victim,
        boolean spawnBody,
        Player killer,
        ResourceLocation deathReason,
        CallbackInfo ci
    ) {
        if (victim.level().isClientSide) return;
        if (GameFunctions.isPlayerAliveAndSurvival(victim)) return;
        BrinVoiceChatPlugin.addPlayer(victim.getUUID());
        ResurrectedComponent component = ResurrectedComponent.KEY.get(victim);
        if (component != null) component.noDropOnDeath = false;
    }

    @Inject(method = "resetPlayer", at = @At("RETURN"))
    private static void brinLeaveTrainVoice(ServerPlayer player, CallbackInfo ci) {
        BrinVoiceChatPlugin.resetPlayer(player.getUUID());
    }
}
