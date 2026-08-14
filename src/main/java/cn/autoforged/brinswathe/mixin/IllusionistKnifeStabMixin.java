package cn.autoforged.brinswathe.mixin;

import java.util.UUID;

import cn.autoforged.brinswathe.component.IllusionistComponent;
import dev.doctor4t.wathe.api.WatheGameModes;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.entity.PlayerBodyEntity;
import dev.doctor4t.wathe.game.GameConstants;
import dev.doctor4t.wathe.game.GameFunctions;
import dev.doctor4t.wathe.index.WatheItems;
import dev.doctor4t.wathe.index.WatheSounds;
import dev.doctor4t.wathe.util.KnifeStabPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KnifeStabPayload.Receiver.class)
public abstract class IllusionistKnifeStabMixin {
    @Inject(
        method = "receive(Ldev/doctor4t/wathe/util/KnifeStabPayload;Lnet/fabricmc/fabric/api/networking/v1/ServerPlayNetworking$Context;)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void brinHandleIllusionKnife(KnifeStabPayload payload,
                                          ServerPlayNetworking.Context context,
                                          CallbackInfo ci) {
        ServerPlayer attacker = context.player();
        Entity target = attacker.serverLevel().getEntity(payload.target());
        PlayerBodyEntity controlled = brinControlledClone(attacker);
        Entity stabOrigin = controlled != null ? controlled : attacker;

        if (target instanceof PlayerBodyEntity body) {
            boolean clone = IllusionistComponent.isClone(body);
            boolean bodyProxy = IllusionistComponent.isBodyProxy(body);
            if (!clone && !bodyProxy) return;
            if (body.distanceTo(stabOrigin) > 3.0F) {
                ci.cancel();
                return;
            }

            body.playSound(WatheSounds.ITEM_KNIFE_STAB, 1.0F, 1.0F);
            attacker.swing(InteractionHand.MAIN_HAND);
            brinApplyKnifeCooldown(attacker);

            Player owner = body.level().getPlayerByUUID(body.getPlayerUuid());
            if (owner != null) {
                IllusionistComponent component = IllusionistComponent.KEY.get(owner);
                if (clone) {
                    IllusionistComponent.applyCloneKillBlindness(attacker);
                    component.handleCloneKilled(body);
                } else {
                    component.endSkill();
                    GameFunctions.killPlayer(owner, true, attacker, GameConstants.DeathReasons.KNIFE);
                }
            } else {
                body.discard();
            }
            ci.cancel();
            return;
        }

        if (controlled == null) return;

        if (target instanceof Player victim
            && victim != attacker
            && victim.distanceTo(controlled) <= 3.0F) {
            GameFunctions.killPlayer(victim, true, attacker, GameConstants.DeathReasons.KNIFE);
            victim.playSound(WatheSounds.ITEM_KNIFE_STAB, 1.0F, 1.0F);
            attacker.swing(InteractionHand.MAIN_HAND);
            brinApplyKnifeCooldown(attacker);
        }
        ci.cancel();
    }

    private static PlayerBodyEntity brinControlledClone(ServerPlayer attacker) {
        IllusionistComponent component = IllusionistComponent.KEY.get(attacker);
        UUID controlledId = component.controlledCloneId;
        if (controlledId == null) return null;
        return attacker.serverLevel().getEntity(controlledId) instanceof PlayerBodyEntity body
            ? body
            : null;
    }

    private static void brinApplyKnifeCooldown(ServerPlayer attacker) {
        if (!attacker.isCreative()
            && GameWorldComponent.KEY.get(attacker.level()).getGameMode() != WatheGameModes.LOOSE_ENDS) {
            attacker.getCooldowns().addCooldown(
                WatheItems.KNIFE,
                GameConstants.ITEM_COOLDOWNS.get(WatheItems.KNIFE)
            );
        }
    }
}
