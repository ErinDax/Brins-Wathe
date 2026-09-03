package cn.erindax.brinswathe.mixin;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.commands.EmoteCommands;
import net.minecraft.server.commands.MsgCommand;
import net.minecraft.server.commands.SayCommand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.mojang.brigadier.CommandDispatcher;

public final class BrinCancelChatCommands {
    private BrinCancelChatCommands() {
    }

    @Mixin(EmoteCommands.class)
    public static class MeCommandMixin {
        @Inject(method = "register(Lcom/mojang/brigadier/CommandDispatcher;)V", at = @At("HEAD"), cancellable = true)
        private static void brinCancelRegister(CommandDispatcher<CommandSourceStack> dispatcher, CallbackInfo ci) {
            ci.cancel();
        }
    }

    @Mixin(MsgCommand.class)
    public static class MsgCommandMixin {
        @Inject(method = "register(Lcom/mojang/brigadier/CommandDispatcher;)V", at = @At("HEAD"), cancellable = true)
        private static void brinCancelRegister(CommandDispatcher<CommandSourceStack> dispatcher, CallbackInfo ci) {
            ci.cancel();
        }
    }

    @Mixin(SayCommand.class)
    public static class SayCommandMixin {
        @Inject(method = "register(Lcom/mojang/brigadier/CommandDispatcher;)V", at = @At("HEAD"), cancellable = true)
        private static void brinCancelRegister(CommandDispatcher<CommandSourceStack> dispatcher, CallbackInfo ci) {
            ci.cancel();
        }
    }
}
