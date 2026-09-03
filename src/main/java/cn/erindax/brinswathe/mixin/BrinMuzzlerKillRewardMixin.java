package cn.erindax.brinswathe.mixin;

import cn.erindax.brinswathe.component.MuzzlerAbilityComponent;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.PlayerShopComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.aussiebox.starexpress.StarryExpressRoles;
import org.aussiebox.starexpress.cca.SilenceComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameFunctions.class)
public abstract class BrinMuzzlerKillRewardMixin {
    @Inject(
        method = "killPlayer(Lnet/minecraft/world/entity/player/Player;ZLnet/minecraft/world/entity/player/Player;Lnet/minecraft/resources/ResourceLocation;)V",
        at = @At("HEAD"),
        remap = false
    )
    private static void brinPayMuzzlerForSilencedKill(
        Player victim,
        boolean spawnBody,
        Player killer,
        ResourceLocation deathReason,
        CallbackInfo ci
    ) {
        if (victim.level().isClientSide) return;
        SilenceComponent silence = SilenceComponent.KEY.get(victim);
        if (silence == null || !silence.isSilenced() || silence.getSilencer() == null) return;
        Player silencer = victim.level().getPlayerByUUID(silence.getSilencer());
        if (silencer == null) return;
        GameWorldComponent game = GameWorldComponent.KEY.get(silencer.level());
        if (!game.isRole(silencer, StarryExpressRoles.MUZZLER)) return;
        PlayerShopComponent shop = PlayerShopComponent.KEY.get(silencer);
        if (shop == null) return;
        shop.addToBalance(MuzzlerAbilityComponent.SILENCE_KILL_REWARD);
        shop.sync();
    }
}
