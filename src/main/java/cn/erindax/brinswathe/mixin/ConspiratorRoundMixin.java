package cn.erindax.brinswathe.mixin;

import cn.erindax.brinswathe.BrinConspirator;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.gamemode.MurderGameMode;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MurderGameMode.class)
public abstract class ConspiratorRoundMixin {
    @WrapOperation(
        method = "tickServerGameLoop",
        at = @At(
            value = "INVOKE",
            target = "Ldev/doctor4t/wathe/cca/GameWorldComponent;isInnocent(Lnet/minecraft/world/entity/player/Player;)Z"
        )
    )
    private boolean brinConspiratorIsNotPassenger(GameWorldComponent game, Player player, Operation<Boolean> original) {
        if (BrinConspirator.isConspirator(game, player)) return false;
        return original.call(game, player);
    }
}
