package cn.erindax.brinswathe.client.mixin;

import cn.erindax.brinswathe.client.GuesserNames;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.util.FormattedCharSequence;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

public final class GuesserNameMixin {
    private GuesserNameMixin() {
    }

    @Environment(EnvType.CLIENT)
    @Mixin(Screen.class)
    public abstract static class Submit {
        @Inject(method = "keyPressed", at = @At("HEAD"))
        private void brinSubmitGuesserRoleName(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
            if (keyCode != GLFW.GLFW_KEY_ENTER && keyCode != GLFW.GLFW_KEY_KP_ENTER) return;
            if (((Screen) (Object) this).getFocused() instanceof EditBox box && GuesserNames.isRoleWidget(box)) {
                GuesserNames.submit(box);
            }
        }
    }

    @Environment(EnvType.CLIENT)
    @Mixin(CommandSuggestions.class)
    public abstract static class Listing {
        @Shadow
        @Final
        EditBox input;

        @Shadow
        @Final
        private List<FormattedCharSequence> commandUsage;

        @WrapMethod(method = "renderUsage")
        private void brinListGuesserRoleNames(GuiGraphics graphics, Operation<Void> original) {
            if (!this.commandUsage.isEmpty() || !GuesserNames.isRoleWidget(this.input)) {
                original.call(graphics);
                return;
            }
            this.commandUsage.addAll(GuesserNames.lines(this.input.getValue()));
            try {
                original.call(graphics);
            } finally {
                this.commandUsage.clear();
            }
        }
    }
}
