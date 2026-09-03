package cn.erindax.brinswathe.mixin;

import cn.erindax.brinswathe.BrinPushed;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class PlayerPushTrackerMixin {
    @Inject(method = "push(Lnet/minecraft/world/entity/Entity;)V", at = @At("HEAD"))
    private void brinMarkPlayerShove(Entity other, CallbackInfo ci) {
        if ((Object) this instanceof Player player && other instanceof Player) {
            BrinPushed.mark(player);
        }
    }
}
