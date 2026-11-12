package cn.erindax.brinswathe.mixin;

import cn.erindax.brinswathe.Zhuimu;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GameWorldComponent.class)
public abstract class ZhuimuFallGuardMixin {
    @WrapWithCondition(
        method = "serverTick()V",
        at = @At(
            value = "INVOKE",
            target = "Ldev/doctor4t/wathe/game/GameFunctions;killPlayer(Lnet/minecraft/world/entity/player/Player;ZLnet/minecraft/world/entity/player/Player;Lnet/minecraft/resources/ResourceLocation;)V"
        )
    )
    private boolean brinKeepZhuimuPassenger(Player victim, boolean spawnBody, Player killer, ResourceLocation deathReason) {
        return Zhuimu.allowsFallDeath(victim);
    }
}
