package cn.autoforged.brinswathe.mixin;

import cn.autoforged.brinswathe.component.MorticianComponent;
import dev.doctor4t.wathe.entity.PlayerBodyEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class MorticianDisguiseMixin {
    /**
     * The fake corpse sits inside the mortician's own hitbox. Leaving it clickable would swallow every
     * knife, bullet and item interaction meant for the player hiding underneath it.
     */
    @Inject(method = "isPickable", at = @At("HEAD"), cancellable = true)
    private void brinIgnoreDisguiseBodyPicking(CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof PlayerBodyEntity body && MorticianComponent.isDisguiseBody(body)) {
            cir.setReturnValue(false);
        }
    }

    /**
     * Any movement cancels the disguise, so a passer-by nudging the invisible mortician would end it for
     * them. Freezing the shove in both directions also stops people bumping into thin air.
     */
    @Inject(method = "isPushable", at = @At("HEAD"), cancellable = true)
    private void brinAnchorDisguisedMortician(CallbackInfoReturnable<Boolean> cir) {
        if (brinIsDisguised()) cir.setReturnValue(false);
    }

    @Inject(method = "pushEntities", at = @At("HEAD"), cancellable = true)
    private void brinSkipDisguisedMorticianPush(CallbackInfo ci) {
        if (brinIsDisguised()) ci.cancel();
    }

    @Unique
    private boolean brinIsDisguised() {
        if (!((Object) this instanceof Player player)) return false;
        MorticianComponent component = MorticianComponent.KEY.get(player);
        return component != null && component.isDisguised();
    }
}
