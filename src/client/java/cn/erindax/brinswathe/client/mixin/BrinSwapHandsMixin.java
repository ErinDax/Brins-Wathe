package cn.erindax.brinswathe.client.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Environment(EnvType.CLIENT)
@Mixin(value = KeyMapping.class, priority = 2000)
public abstract class BrinSwapHandsMixin {
    @Shadow
    private boolean isDown;
    @Shadow
    private int clickCount;
    @Shadow
    private InputConstants.Key key;
    @WrapMethod(method = "isDown")
    private boolean brinAllowSwapHandsDown(Operation<Boolean> original) {
        if (brinIsUnrestrictedInMatch()) return this.isDown;
        return original.call();
    }
    @WrapMethod(method = "consumeClick")
    private boolean brinAllowSwapHandsClick(Operation<Boolean> original) {
        if (!brinIsUnrestrictedInMatch()) return original.call();
        if (this.clickCount == 0) return false;
        this.clickCount--;
        return true;
    }
    @WrapMethod(method = "matches")
    private boolean brinAllowSwapHandsMatches(int keyCode, int scanCode, Operation<Boolean> original) {
        if (!brinIsUnrestrictedInMatch()) return original.call(keyCode, scanCode);
        if (keyCode == InputConstants.UNKNOWN.getValue()) {
            return this.key.getType() == InputConstants.Type.SCANCODE && this.key.getValue() == scanCode;
        }
        return this.key.getType() == InputConstants.Type.KEYSYM && this.key.getValue() == keyCode;
    }
    @Unique
    private boolean brinIsUnrestrictedInMatch() {
        Minecraft client = Minecraft.getInstance();
        if (client == null || client.options == null) return false;
        Object self = this;
        return self == client.options.keySwapOffhand
            || self == client.options.keyChat
            || self == client.options.keyCommand;
    }
}
