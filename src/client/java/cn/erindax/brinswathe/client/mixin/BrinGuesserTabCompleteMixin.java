package cn.erindax.brinswathe.client.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.EditBox;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(EditBox.class)
public abstract class BrinGuesserTabCompleteMixin {
    @Unique
    private static final String GUESSER_ROLE_WIDGET = "org.agmas.noellesroles.client.ui.guesser.GuesserRoleWidget";

    @Shadow
    private String suggestion;

    @Shadow
    public abstract String getValue();

    @Shadow
    public abstract void setValue(String text);

    @Shadow
    public abstract void moveCursorToEnd(boolean select);

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void brinCompleteGuesserRole(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (keyCode != GLFW.GLFW_KEY_TAB) return;
        if (!GUESSER_ROLE_WIDGET.equals(this.getClass().getName())) return;
        String completion = this.suggestion;
        if (completion == null || completion.isEmpty()) return;
        this.setValue(this.getValue() + completion);
        this.moveCursorToEnd(false);
        cir.setReturnValue(true);
    }
}
