package cn.autoforged.brinswathe.mixin;

import cn.autoforged.brinswathe.component.IllusionistComponent;
import dev.doctor4t.wathe.entity.PlayerBodyEntity;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class IllusionistModelDamageMixin {
    @Inject(method = "hurt", at = @At("HEAD"), cancellable = true)
    private void brinHandleIllusionModelDamage(DamageSource source, float amount,
                                                CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof ServerPlayer controlledPlayer) {
            IllusionistComponent component = IllusionistComponent.KEY.get(controlledPlayer);
            if (component.controlledCloneId != null) {
                Entity sourceEntity = source.getEntity();
                component.endSkill();
                IllusionistComponent.applyCloneKillBlindness(sourceEntity);
                cir.setReturnValue(true);
                return;
            }
        }
        if (!((Object) this instanceof PlayerBodyEntity body)) return;
        boolean clone = IllusionistComponent.isClone(body);
        boolean bodyProxy = IllusionistComponent.isBodyProxy(body);
        if (!clone && !bodyProxy) return;

        Entity sourceEntity = source.getEntity();
        if (!(sourceEntity instanceof LivingEntity)) {
            cir.setReturnValue(false);
            return;
        }

        Player owner = body.level().getPlayerByUUID(body.getPlayerUuid());
        if (owner == null) {
            body.discard();
            cir.setReturnValue(false);
            return;
        }

        IllusionistComponent component = IllusionistComponent.KEY.get(owner);
        if (clone) {
            IllusionistComponent.applyCloneKillBlindness(sourceEntity);
            component.handleCloneKilled(body);
            cir.setReturnValue(true);
            return;
        }

        component.endSkill();
        Player killer = sourceEntity instanceof Player player ? player : owner;
        GameFunctions.killPlayer(owner, false, killer);
        cir.setReturnValue(true);
    }
}
