package cn.erindax.brinswathe.mixin;

import cn.erindax.brinswathe.BrinRoleWeights;
import com.mojang.brigadier.context.CommandContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "dev.doctor4t.wathe.command.GameSettingsCommand", remap = false)
public class BrinGameSettingsWeightsMixin {
    @Inject(method = "lambda$register$4", at = @At("HEAD"), cancellable = true)
    private static void brinSetWeights(CommandContext<?> context, CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(BrinRoleWeights.applyFromCommand(context));
    }
}
