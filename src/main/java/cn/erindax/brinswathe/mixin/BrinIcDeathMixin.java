package cn.erindax.brinswathe.mixin;

import cn.erindax.brinswathe.BrinNoelleAccess;
import cn.erindax.brinswathe.CowboyDuel;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.api.WatheRoles;
import dev.doctor4t.wathe.api.event.AllowPlayerDeath;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.PlayerShopComponent;
import dev.doctor4t.wathe.game.GameConstants;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GameFunctions.class)
public abstract class BrinIcDeathMixin {
    @Unique
    private static final ResourceLocation IGNITED =
        ResourceLocation.fromNamespaceAndPath("stupid_express", "ignited");

    @WrapOperation(
        method = "killPlayer(Lnet/minecraft/world/entity/player/Player;ZLnet/minecraft/world/entity/player/Player;Lnet/minecraft/resources/ResourceLocation;)V",
        at = @At(
            value = "INVOKE",
            target = "Ldev/doctor4t/wathe/api/event/AllowPlayerDeath;allowDeath(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/resources/ResourceLocation;)Z"
        )
    )
    private static boolean brinIcShieldAndBounty(
        AllowPlayerDeath invoker,
        Player victim,
        Player killer,
        ResourceLocation deathReason,
        Operation<Boolean> original
    ) {
        if (victim.level().isClientSide || CowboyDuel.isActive()) {
            return original.call(invoker, victim, killer, deathReason);
        }

        GameWorldComponent game = GameWorldComponent.KEY.get(victim.level());
        int armor = BrinNoelleAccess.bartenderArmor(victim);
        boolean pierce = armor > 0 && brinPiercesShield(deathReason);
        if (pierce) BrinNoelleAccess.setBartenderArmor(victim, 0);

        if (killer != null && game.isRole(victim, WatheRoles.KILLER)) {
            Role killerRole = game.getRole(killer);
            if (killerRole != null && !killerRole.canUseKiller()) {
                brinAddGold(killer, 50);
            }
        }

        boolean allowed = original.call(invoker, victim, killer, deathReason);
        if (pierce && !allowed) {
            BrinNoelleAccess.setBartenderArmor(victim, armor);
        } else if (!pierce && killer != null && armor > BrinNoelleAccess.bartenderArmor(victim)) {
            brinAddGold(killer, 50);
        }
        return allowed;
    }

    @Unique
    private static boolean brinPiercesShield(ResourceLocation deathReason) {
        return GameConstants.DeathReasons.BAT.equals(deathReason) || IGNITED.equals(deathReason);
    }

    @Unique
    private static void brinAddGold(Player player, int amount) {
        PlayerShopComponent shop = PlayerShopComponent.KEY.get(player);
        if (shop == null) return;
        shop.addToBalance(amount);
        shop.sync();
    }
}
