package cn.erindax.brinswathe.client.mixin;

import cn.erindax.brinswathe.client.GuesserNames;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(EditBox.class)
public abstract class GuesserTabCompleteMixin {
    @Shadow
    private String suggestion;

    @Shadow
    private int maxLength;

    @Shadow
    public abstract String getValue();

    @Shadow
    public abstract void setValue(String text);

    @Shadow
    public abstract void moveCursorToEnd(boolean select);

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void brinCompleteGuesserRole(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (keyCode != GLFW.GLFW_KEY_TAB) return;
        if (!GuesserNames.isRoleWidget(this)) return;
        String completion = this.suggestion;
        if (completion == null || completion.isEmpty()) return;
        this.setValue(this.getValue() + completion);
        this.moveCursorToEnd(false);
        cir.setReturnValue(true);
    }

    @Inject(method = "renderWidget", at = @At("HEAD"))
    private void brinSuggestGuesserRoleName(GuiGraphics graphics, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (!GuesserNames.isRoleWidget(this)) return;
        if (this.suggestion != null && !this.suggestion.isEmpty()) return;
        this.suggestion = GuesserNames.completion(this.getValue(), this.maxLength);
    }
}
