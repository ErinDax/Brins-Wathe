package cn.erindax.brinswathe.mixin;

import cn.erindax.brinswathe.BrinWelcome;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.util.AnnounceWelcomePayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import org.agmas.harpymodloader.modded_murder.ModdedMurderGameMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ModdedMurderGameMode.class)
public abstract class BrinWelcomeByRoleMixin {
    @WrapOperation(
        method = "initializeGame",
        at = @At(
            value = "INVOKE",
            target = "Lnet/fabricmc/fabric/api/networking/v1/ServerPlayNetworking;send(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/network/protocol/common/custom/CustomPacketPayload;)V"
        )
    )
    private void brinSendWelcomeByRole(
        ServerPlayer player,
        CustomPacketPayload payload,
        Operation<Void> original,
        @Local(argsOnly = true) GameWorldComponent game
    ) {
        if (payload instanceof AnnounceWelcomePayload welcome
            && BrinWelcome.sendAssigned(player, game, welcome.killers(), welcome.targets())) {
            return;
        }
        original.call(player, payload);
    }
}
