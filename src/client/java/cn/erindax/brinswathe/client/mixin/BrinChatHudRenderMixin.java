package cn.erindax.brinswathe.client.mixin;

import cn.erindax.brinswathe.client.BrinChatHudGate;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ChatComponent;
import org.spongepowered.asm.mixin.Mixin;

@Environment(EnvType.CLIENT)
@Mixin(value = ChatComponent.class, priority = 2000)
public abstract class BrinChatHudRenderMixin {
    @WrapMethod(method = "render")
    private void brinAlwaysRenderChat(
        GuiGraphics graphics,
        int tickCount,
        int mouseX,
        int mouseY,
        boolean focused,
        Operation<Void> original
    ) {
        BrinChatHudGate.enter();
        try {
            original.call(graphics, tickCount, mouseX, mouseY, focused);
        } finally {
            BrinChatHudGate.exit();
        }
    }
}
