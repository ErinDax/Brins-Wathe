package cn.erindax.brinswathe.mixin;

import cn.erindax.brinswathe.BrinModifiers;
import cn.erindax.brinswathe.BrinNoelleAccess;
import dev.doctor4t.wathe.api.WatheRoles;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.PlayerShopComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(dev.doctor4t.wathe.game.GameFunctions.class)
public abstract class BrinGuesserKillMixin {
    private static final ResourceLocation VOODOO_DEATH =
        ResourceLocation.fromNamespaceAndPath("noellesroles", "voodoo");

    @Inject(
        method = "killPlayer(Lnet/minecraft/world/entity/player/Player;ZLnet/minecraft/world/entity/player/Player;Lnet/minecraft/resources/ResourceLocation;)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private static void brinGuesserSpecialCases(
        Player victim,
        boolean spawnBody,
        Player killer,
        ResourceLocation deathReason,
        CallbackInfo ci
    ) {
        if (victim.level().isClientSide || killer == null) return;
        if (!VOODOO_DEATH.equals(deathReason)) return;
        if (!BrinModifiers.hasModifier(killer, BrinModifiers.GUESSER)) return;

        GameWorldComponent game = GameWorldComponent.KEY.get(victim.level());
        if (game.isRole(victim, WatheRoles.VIGILANTE)) {
            ci.cancel();
            return;
        }
        if (!BrinNoelleAccess.isRole(game, victim, BrinNoelleAccess.NOISEMAKER_ID)) return;
        PlayerShopComponent targetShop = PlayerShopComponent.KEY.get(victim);
        if (targetShop == null || targetShop.balance <= 100) return;
        targetShop.balance -= 200;
        targetShop.sync();
        BrinNoelleAccess.giveBartenderArmor(victim);
        PlayerShopComponent guesserShop = PlayerShopComponent.KEY.get(killer);
        if (guesserShop != null) {
            guesserShop.addToBalance(50);
            guesserShop.sync();
        }
        ci.cancel();
    }
}
