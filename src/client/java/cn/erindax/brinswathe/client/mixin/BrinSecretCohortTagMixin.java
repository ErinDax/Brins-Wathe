package cn.erindax.brinswathe.client.mixin;

import cn.erindax.brinswathe.client.BrinSecretMurderClient;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.client.gui.RoleNameRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Environment(EnvType.CLIENT)
@Mixin(RoleNameRenderer.class)
public abstract class BrinSecretCohortTagMixin {
    @WrapOperation(
        method = "renderHud",
        at = @At(
            value = "INVOKE",
            target = "Ldev/doctor4t/wathe/cca/GameWorldComponent;canUseKillerFeatures(Lnet/minecraft/world/entity/player/Player;)Z",
            ordinal = 0
        )
    )
    private static boolean brinSecretMurderCohortTag(GameWorldComponent game, Player target, Operation<Boolean> original) {
        if (BrinSecretMurderClient.isLocalSecretKiller()) return BrinSecretMurderClient.isCohort(target.getUUID());
        return original.call(game, target);
    }
}
