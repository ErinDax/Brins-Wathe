package cn.erindax.brinswathe.mixin;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "dev.doctor4t.wathe.Wathe", remap = false)
public abstract class BrinIcSupporterCommandMixin {
    @Inject(method = "isSupporter", at = @At("HEAD"), cancellable = true)
    private static void brinIcAlwaysSupporter(Player player, CallbackInfoReturnable<Boolean> cir) {
        if (player != null) {
            cir.setReturnValue(true);
        }
    }
    @Inject(method = "executeSupporterCommand", at = @At("HEAD"), cancellable = true)
    private static void brinIcAllowSupporterCommands(CommandSourceStack source, Runnable runnable,
                                                   CallbackInfoReturnable<Integer> cir) {
        ServerPlayer player = source.getPlayer();
        if (player == null || !player.getClass().equals(ServerPlayer.class)) {
            cir.setReturnValue(0);
            return;
        }
        runnable.run();
        cir.setReturnValue(1);
    }
}
